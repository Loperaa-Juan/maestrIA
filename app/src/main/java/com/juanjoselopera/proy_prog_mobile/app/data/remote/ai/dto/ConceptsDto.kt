package com.juanjoselopera.proy_prog_mobile.app.data.remote.ai.dto

data class ConceptsRequestDto(
    val note: String,
    val model: String
)

data class ConceptsResponseDto(
    val concepts: List<ConceptItemDto>
)

data class ConceptItemDto(
    val term: String,
    val definition: String
)
