package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai

import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.ConceptsRequestDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.DeepResearchRequestDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.QuestionsRequestDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.SummaryRequestDto
import com.juanjoselopera.proy_prog_mobile.app.domain.model.ConceptItem
import com.juanjoselopera.proy_prog_mobile.app.domain.model.DeepResearchResult
import com.juanjoselopera.proy_prog_mobile.app.domain.model.QAItem
import com.juanjoselopera.proy_prog_mobile.app.domain.model.ResearchSource
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AiRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val api: AiApiService
) : AiRepository {

    override suspend fun getQuestions(note: String, model: String): Resource<List<QAItem>> =
        runCatching {
            val response = api.getQuestions(QuestionsRequestDto(note, model))
            response.questions.map { QAItem(it.question, it.answer) }
        }.toResource()

    override suspend fun getConcepts(note: String, model: String): Resource<List<ConceptItem>> =
        runCatching {
            val response = api.getConcepts(ConceptsRequestDto(note, model))
            response.concepts.map { ConceptItem(it.term, it.definition) }
        }.toResource()

    override suspend fun getSummary(note: String, model: String): Resource<String> =
        runCatching {
            api.getSummary(SummaryRequestDto(note, model)).summary
        }.toResource()

    override suspend fun extractText(imageBytes: ByteArray, mimeType: String, model: String): Resource<String> =
        runCatching {
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                "image.jpg",
                imageBytes.toRequestBody(mimeType.toMediaType())
            )
            val modelBody = model.toRequestBody("text/plain".toMediaType())
            api.extractText(imagePart, modelBody).text
        }.toResource()

    override suspend fun deepResearch(
        model: String,
        topic: String?,
        note: String?
    ): Resource<DeepResearchResult> =
        runCatching {
            val response = api.deepResearch(DeepResearchRequestDto(model = model, topic = topic, note = note))
            DeepResearchResult(
                research = response.research,
                sources = response.sources.map { ResearchSource(it.title, it.uri) }
            )
        }.toResource()

    private fun <T> Result<T>.toResource(): Resource<T> =
        fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                Resource.Error(e.message ?: "Error desconocido")
            }
        )
}
