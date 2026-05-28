package com.juanjoselopera.proy_prog_mobile.app.ui.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Note
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    val subjectCount: StateFlow<Int> = subjectRepository.getSubjects()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val noteCount: StateFlow<Int> = noteRepository.getNotes(null)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Los últimos 5 apuntes ordenados del más reciente al más antiguo
    val recentNotes: StateFlow<List<Note>> = noteRepository.getNotes(null)
        .map { notes -> notes.sortedByDescending { it.createdAt }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Materias necesarias para obtener el color de acento de cada apunte
    val subjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
