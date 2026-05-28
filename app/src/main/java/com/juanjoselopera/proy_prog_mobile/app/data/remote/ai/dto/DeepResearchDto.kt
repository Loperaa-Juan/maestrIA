package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto

data class DeepResearchRequestDto(
    val model: String,
    val topic: String? = null,
    val note: String? = null
)

data class DeepResearchResponseDto(
    val research: String,
    val sources: List<SourceItemDto>
)

data class SourceItemDto(
    val title: String,
    val uri: String
)
