package com.juanjoselopera.proy_prog_mobile.app.domain.usecase

import com.juanjoselopera.proy_prog_mobile.app.domain.model.QAItem
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.AiRepository
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val repository: AiRepository
) {
    suspend operator fun invoke(note: String, model: String): Resource<List<QAItem>> =
        repository.getQuestions(note, model)
}
