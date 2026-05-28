package com.juanjoselopera.proy_prog_mobile.app.domain.repository

import com.juanjoselopera.proy_prog_mobile.app.domain.model.ConceptItem
import com.juanjoselopera.proy_prog_mobile.app.domain.model.DeepResearchResult
import com.juanjoselopera.proy_prog_mobile.app.domain.model.QAItem
import com.juanjoselopera.proy_prog_mobile.app.util.Resource

interface AiRepository {
    suspend fun getQuestions(note: String, model: String): Resource<List<QAItem>>
    suspend fun getConcepts(note: String, model: String): Resource<List<ConceptItem>>
    suspend fun getSummary(note: String, model: String): Resource<String>
    suspend fun extractText(imageBytes: ByteArray, mimeType: String, model: String): Resource<String>
    suspend fun deepResearch(
        model: String,
        topic: String?,
        imageBytes: ByteArray?,
        mimeType: String?
    ): Resource<DeepResearchResult>
}
