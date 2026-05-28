package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto

data class DeepResearchResponseDto(
    val research: String,
    val sources: List<SourceItemDto>
)

data class SourceItemDto(
    val title: String,
    val uri: String
)
