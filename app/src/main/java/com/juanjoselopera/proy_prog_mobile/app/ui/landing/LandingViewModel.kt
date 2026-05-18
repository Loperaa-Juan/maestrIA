package com.juanjoselopera.proy_prog_mobile.app.ui.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
