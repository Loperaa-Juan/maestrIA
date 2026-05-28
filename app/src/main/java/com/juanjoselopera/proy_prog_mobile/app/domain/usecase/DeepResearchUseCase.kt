package com.juanjoselopera.proy_prog_mobile.app.domain.usecase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.DeepResearchResult
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AiRepository
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import javax.inject.Inject

class DeepResearchUseCase @Inject constructor(
    private val repository: AiRepository
) {
    suspend operator fun invoke(
        model: String,
        topic: String? = null,
        note: String? = null
    ): Resource<DeepResearchResult> =
        repository.deepResearch(model, topic, note)
}
