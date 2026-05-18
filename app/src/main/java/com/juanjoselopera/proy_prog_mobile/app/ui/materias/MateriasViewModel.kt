package com.juanjoselopera.proy_prog_mobile.app.ui.materias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.model.Subject
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MateriasViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val subjects: StateFlow<List<Subject>> = subjectRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String, iconIndex: Int, colorIndex: Int) {
        viewModelScope.launch {
            subjectRepository.addSubject(Subject(name = name, iconIndex = iconIndex, colorIndex = colorIndex))
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch { subjectRepository.updateSubject(subject) }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch { subjectRepository.deleteSubject(subjectId) }
    }
}
