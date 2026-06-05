package com.juanjoselopera.proy_prog_mobile.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.NoteRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.ProfileRepository
import com.juanjoselopera.proy_prog_mobile.app.domain.repository.SubjectRepository
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    noteRepository: NoteRepository,
    subjectRepository: SubjectRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val noteCount: StateFlow<Int> = noteRepository.getNotes(null)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val subjectCount: StateFlow<Int> = subjectRepository.getSubjects()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Estados de guardado. null = inactivo (sin acción en curso).
    private val _nameState = MutableStateFlow<Resource<Unit>?>(null)
    val nameState: StateFlow<Resource<Unit>?> = _nameState.asStateFlow()

    private val _photoState = MutableStateFlow<Resource<Unit>?>(null)
    val photoState: StateFlow<Resource<Unit>?> = _photoState.asStateFlow()

    fun saveName(name: String) {
        viewModelScope.launch {
            _nameState.value = Resource.Loading
            _nameState.value = when (val result = profileRepository.updateDisplayName(name)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                else -> Resource.Finished
            }
        }
    }

    fun savePhoto(bytes: ByteArray) {
        viewModelScope.launch {
            _photoState.value = Resource.Loading
            _photoState.value = when (val result = profileRepository.updateProfilePhoto(bytes)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                else -> Resource.Finished
            }
        }
    }

    /** Limpia el estado tras consumirlo en la UI (evita re-emitir al rotar). */
    fun clearNameState() { _nameState.value = null }
    fun clearPhotoState() { _photoState.value = null }
}
