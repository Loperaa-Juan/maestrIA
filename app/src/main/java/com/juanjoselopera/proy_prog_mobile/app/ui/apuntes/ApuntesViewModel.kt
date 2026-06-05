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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApuntesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val subjectRepository: SubjectRepository,
    private val extractTextUseCase: ExtractTextUseCase
) : ViewModel() {

    private val _extractedText = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val extractedText: SharedFlow<String> = _extractedText.asSharedFlow()

    private val _extractLoading = MutableStateFlow(false)
    val extractLoading: StateFlow<Boolean> = _extractLoading.asStateFlow()

    fun extractTextFromImage(imageBytes: ByteArray, mimeType: String, model: String) {
        viewModelScope.launch {
            _extractLoading.value = true
            when (val res = extractTextUseCase(imageBytes, mimeType, model)) {
                is Resource.Success -> _extractedText.emit(res.data)
                is Resource.Error -> _extractedText.emit("")
                else -> Unit
            }
            _extractLoading.value = false
        }
    }

    val subjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId: StateFlow<String?> = _selectedSubjectId.asStateFlow()

    private val _titleQuery = MutableStateFlow("")
    val titleQuery: StateFlow<String> = _titleQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _selectedSubjectId
        .flatMapLatest { subjectId -> noteRepository.getNotes(subjectId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Apuntes ya filtrados por materia y, además, por el título escrito en tiempo real.
    val filteredNotes: StateFlow<List<Note>> = combine(notes, _titleQuery) { notes, query ->
        val q = query.trim()
        if (q.isBlank()) notes
        else notes.filter { it.title.contains(q, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterBySubject(subjectId: String?) {
        _selectedSubjectId.value = subjectId
    }

    fun setTitleQuery(text: String) {
        _titleQuery.value = text
    }

    fun addNote(title: String, content: String, subjectId: String, subjectName: String) {
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
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (note.id.isEmpty()) {
                noteRepository.addNote(note)
            } else {
                noteRepository.updateNote(note)
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch { noteRepository.updateNote(note) }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch { noteRepository.deleteNote(noteId) }
    }
}
