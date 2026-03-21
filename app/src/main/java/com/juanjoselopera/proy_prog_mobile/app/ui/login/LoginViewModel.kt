package com.juanjoselopera.proy_prog_mobile.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanjoselopera.proy_prog_mobile.app.domain.usecase.FirebaseLoginUseCase
import com.juanjoselopera.proy_prog_mobile.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: FirebaseLoginUseCase
): ViewModel() {
    private val _loginState = MutableLiveData<Resource<Boolean>>()
    val loginState: LiveData<Resource<Boolean>> = _loginState

    fun login(email: String, password: String) {
        loginUseCase(email, password).onEach { state ->
            _loginState.value = state
        }.launchIn(viewModelScope)
    }
}