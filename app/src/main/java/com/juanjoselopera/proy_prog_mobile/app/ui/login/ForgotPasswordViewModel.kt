package com.juanjoselopera.proy_prog_mobile.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.ForgotPasswordUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val error: String? = null,
    val isEmailSent: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendResetEmail(email: String) {
        if (_uiState.value.isLoading) return

        val emailError = when {
            email.isBlank() -> "El email no puede estar vacío"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }

        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        forgotPasswordUseCase(email).onEach { resource ->
            when (resource) {
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null, emailError = null) }
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, isEmailSent = true) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = resource.message) }
                is Resource.Finished -> _uiState.update { it.copy(isLoading = false) }
            }
        }.launchIn(viewModelScope)
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
