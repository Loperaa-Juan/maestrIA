package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto

import com.google.gson.annotations.SerializedName

data class QuestionsRequestDto(
    val note: String,
    val model: String
)

data class QuestionsResponseDto(
    val questions: List<QAItemDto>
)

data class QAItemDto(
    val question: String,
    val answer: String
)
