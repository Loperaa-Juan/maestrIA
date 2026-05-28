package com.juanjoselopera.proy_prog_mobile.app.ui.AI.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.QAItem
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.GetQuestionsUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionsUiState(
    val isLoading: Boolean = false,
    val questions: List<QAItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class QuestionsViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionsUiState())
    val uiState: StateFlow<QuestionsUiState> = _uiState.asStateFlow()

    private var isRunning = false

    fun load(note: String, model: String) {
        if (isRunning) return
        isRunning = true
        _uiState.value = QuestionsUiState(isLoading = true)

        viewModelScope.launch {
            when (val res = getQuestionsUseCase(note, model)) {
                is Resource.Success -> _uiState.value = QuestionsUiState(questions = res.data)
                is Resource.Error -> _uiState.value = QuestionsUiState(error = res.message)
                else -> Unit
            }
            isRunning = false
        }
    }
}
