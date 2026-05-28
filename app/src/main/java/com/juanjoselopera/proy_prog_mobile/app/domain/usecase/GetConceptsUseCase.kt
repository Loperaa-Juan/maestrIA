package com.juanjoselopera.proy_prog_mobile.app.domain.usecase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.ConceptItem
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AiRepository
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import javax.inject.Inject

class GetConceptsUseCase @Inject constructor(
    private val repository: AiRepository
) {
    suspend operator fun invoke(note: String, model: String): Resource<List<ConceptItem>> =
        repository.getConcepts(note, model)
}
