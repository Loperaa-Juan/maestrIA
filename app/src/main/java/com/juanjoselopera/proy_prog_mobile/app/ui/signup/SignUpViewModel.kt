package com.juanjoselopera.proy_prog_mobile.app.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.FirebaseSignUpUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: FirebaseSignUpUseCase
): ViewModel() {
    private val _signUpState = MutableLiveData<Resource<Boolean>>()
    val signUpState: LiveData<Resource<Boolean>> = _signUpState

    fun signUp(email: String, password: String) {
        signUpUseCase(email, password).onEach { state ->
            _signUpState.value = state
        }.launchIn(viewModelScope)
    }
}