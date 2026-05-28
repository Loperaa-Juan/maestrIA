package com.juanjoselopera.proy_prog_mobile.app.ui.AI.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.ConceptItem
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.GetConceptsUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConceptsUiState(
    val isLoading: Boolean = false,
    val concepts: List<ConceptItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ConceptsViewModel @Inject constructor(
    private val getConceptsUseCase: GetConceptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConceptsUiState())
    val uiState: StateFlow<ConceptsUiState> = _uiState.asStateFlow()

    private var isRunning = false

    fun load(note: String, model: String) {
        if (isRunning) return
        isRunning = true
        _uiState.value = ConceptsUiState(isLoading = true)

        viewModelScope.launch {
            when (val res = getConceptsUseCase(note, model)) {
                is Resource.Success -> _uiState.value = ConceptsUiState(concepts = res.data)
                is Resource.Error -> _uiState.value = ConceptsUiState(error = res.message)
                else -> Unit
            }
            isRunning = false
        }
    }
}
