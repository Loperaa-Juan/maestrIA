package com.juanjoselopera.proy_prog_mobile.app.ui.apuntes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApuntesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    val selectedSubjectId: StateFlow<String?> = _selectedSubjectId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _selectedSubjectId
        .flatMapLatest { subjectId -> noteRepository.getNotes(subjectId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterBySubject(subjectId: String?) {
        _selectedSubjectId.value = subjectId
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

    fun updateNote(note: Note) {
        viewModelScope.launch { noteRepository.updateNote(note) }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch { noteRepository.deleteNote(noteId) }
    }
}
