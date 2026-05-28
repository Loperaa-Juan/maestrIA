package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai

import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.ConceptsRequestDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.ConceptsResponseDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.DeepResearchResponseDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.ExtractTextResponseDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.QuestionsRequestDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.QuestionsResponseDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.SummaryRequestDto
import com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto.SummaryResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AiApiService {

    @POST("api/questions")
    suspend fun getQuestions(@Body body: QuestionsRequestDto): QuestionsResponseDto

    @POST("api/concepts")
    suspend fun getConcepts(@Body body: ConceptsRequestDto): ConceptsResponseDto

    @POST("api/summary")
    suspend fun getSummary(@Body body: SummaryRequestDto): SummaryResponseDto

    @Multipart
    @POST("api/extract-text")
    suspend fun extractText(
        @Part image: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): ExtractTextResponseDto

    @Multipart
    @POST("api/deep-research")
    suspend fun deepResearch(
        @Part("model") model: RequestBody,
        @Part("topic") topic: RequestBody?,
        @Part image: MultipartBody.Part?
    ): DeepResearchResponseDto
}
