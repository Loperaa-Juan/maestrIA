package com.juanjoselopera.proy_prog_mobile.app.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.FirebaseSignUpUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEmailSent: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: FirebaseSignUpUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (_uiState.value.isLoading) return
        if (!validateFields(email, password, confirmPassword)) return

        signUpUseCase(email, password).onEach { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                is Resource.Success -> {
                    firebaseAuth.currentUser?.sendEmailVerification()
                    _uiState.update { it.copy(isLoading = false, isEmailSent = true) }
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

    private fun validateFields(email: String, password: String, confirmPassword: String): Boolean {
        val emailError = when {
            email.isBlank() -> "El email no puede estar vacío"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
            else -> null
        }

        val passwordError = validatePassword(password)
        val confirmPasswordError = validateConfirmPassword(password, confirmPassword)

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                error = null,
            )
        }
        return emailError == null && passwordError == null && confirmPasswordError == null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña no puede estar vacía"

        val violations = buildList {
            if (password.length < 8) add("mínimo 8 caracteres")
            if (!password.any { it.isUpperCase() }) add("una mayúscula")
            if (!password.any { it.isDigit() }) add("un número")
            if (!password.any { !it.isLetterOrDigit() }) add("un carácter especial")
        }

        return if (violations.isEmpty()) null
        else "Debe incluir " + violations.joinToString(", ")
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }
}
