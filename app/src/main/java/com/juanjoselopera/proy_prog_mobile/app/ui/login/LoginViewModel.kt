package com.juanjoselopera.proy_prog_mobile.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.FirebaseLoginUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: FirebaseLoginUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // Evita múltiples peticiones simultáneas si ya está cargando
        if (_uiState.value.isLoading) return

        if (!validateFields(email, password)) return

        loginUseCase(email, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = resource.message) }
                }
                is Resource.Finished -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun validateFields(email: String, password: String): Boolean {
        var isValid = true
        val emailError = if (email.isBlank()) {
            isValid = false
            "El email no puede estar vacío"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = false
            "Email inválido"
        } else null

        val passwordError = if (password.isBlank()) {
            isValid = false
            "La contraseña no puede estar vacía"
        } else if (password.length < 6) {
            isValid = false
            "La contraseña debe tener al menos 6 caracteres"
        } else null

        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, error = null) }
        return isValid
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
