package com.juanjoselopera.proy_prog_mobile.app.domain.model

data class DeepResearchResult(
    val research: String,
    val sources: List<ResearchSource>
)

data class ResearchSource(
    val title: String,
    val uri: String
)
