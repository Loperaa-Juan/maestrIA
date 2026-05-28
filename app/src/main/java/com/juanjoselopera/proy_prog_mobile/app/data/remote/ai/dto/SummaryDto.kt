package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto

data class SummaryRequestDto(
    val note: String,
    val model: String
)

data class SummaryResponseDto(
    val summary: String
)
