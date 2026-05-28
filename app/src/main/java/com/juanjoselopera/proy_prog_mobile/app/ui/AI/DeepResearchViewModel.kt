package com.juanjoselopera.proy_prog_mobile.app.ui.AI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.DeepResearchResult
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.DeepResearchUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeepResearchUiState(
    val isLoading: Boolean = false,
    val result: DeepResearchResult? = null,
    val error: String? = null
)

@HiltViewModel
class DeepResearchViewModel @Inject constructor(
    private val deepResearchUseCase: DeepResearchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeepResearchUiState())
    val uiState: StateFlow<DeepResearchUiState> = _uiState.asStateFlow()

    private var isRunning = false

    fun research(model: String, topic: String?, imageBytes: ByteArray?, mimeType: String?) {
        if (isRunning) return
        isRunning = true
        _uiState.value = DeepResearchUiState(isLoading = true)

        viewModelScope.launch {
            when (val res = deepResearchUseCase(model, topic, imageBytes, mimeType)) {
                is Resource.Success -> _uiState.value = DeepResearchUiState(result = res.data)
                is Resource.Error -> _uiState.value = DeepResearchUiState(error = res.message)
                else -> Unit
            }
            isRunning = false
        }
    }

    fun reset() {
        isRunning = false
        _uiState.value = DeepResearchUiState()
    }
}
