package com.juanjoselopera.proy_prog_mobile.app.ui.AI.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.GetSummaryUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SummaryUiState(
    val isLoading: Boolean = false,
    val summary: String? = null,
    val error: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val getSummaryUseCase: GetSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    private var isRunning = false

    fun load(note: String, model: String) {
        if (isRunning) return
        isRunning = true
        _uiState.value = SummaryUiState(isLoading = true)

        viewModelScope.launch {
            when (val res = getSummaryUseCase(note, model)) {
                is Resource.Success -> _uiState.value = SummaryUiState(summary = res.data)
                is Resource.Error -> _uiState.value = SummaryUiState(error = res.message)
                else -> Unit
            }
            isRunning = false
        }
    }
}
