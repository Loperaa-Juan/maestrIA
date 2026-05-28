package com.juanjoselopera.proy_prog_mobile.app.ui.apuntes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.ExtractTextUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtractTextUiState(
    val isLoading: Boolean = false,
    val text: String? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class ExtractTextViewModel @Inject constructor(
    private val extractTextUseCase: ExtractTextUseCase,
    private val noteRepository: NoteRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtractTextUiState())
    val uiState: StateFlow<ExtractTextUiState> = _uiState.asStateFlow()

    val subjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun extract(imageBytes: ByteArray, mimeType: String, model: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val res = extractTextUseCase(imageBytes, mimeType, model)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, text = res.data) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = res.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveNote(title: String, subjectId: String, subjectName: String) {
        val content = _uiState.value.text ?: return
        viewModelScope.launch {
            noteRepository.addNote(
                Note(
                    title = title,
                    content = content,
                    subjectId = subjectId,
                    subjectName = subjectName,
                    createdAt = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
