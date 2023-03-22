package com.yashkasera.streamchat.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashkasera.streamchat.R
import com.yashkasera.streamchat.data.Result
import com.yashkasera.streamchat.data.model.LoggedInUser
import com.yashkasera.streamchat.data.repository.LoginRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<Result<LoggedInUser>>()
    val loginResult: LiveData<Result<LoggedInUser>> = _loginResult
    private val loginRepository: LoginRepository by lazy { LoginRepository() }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.postValue(Result.Loading(true))
            val res = loginRepository.login(username, password)
            _loginResult.postValue(res)
        }
    }

    fun loginGuest(username: String, name: String) {
        viewModelScope.launch {
            _loginResult.postValue(loginRepository.loginGuest(username, name))
        }
    }

    fun loginDataChanged(username: String, password: String? = null, name: String? = null) =
        _loginForm.postValue(
            when {
                !isUsernameValid(username) -> LoginFormState(usernameError = R.string.invalid_username)
                password != null && !isPasswordValid(password) -> LoginFormState(passwordError = R.string.invalid_password)
                name != null && name.length < 3 -> LoginFormState(passwordError = R.string.invalid_name)
                else -> LoginFormState(isDataValid = true)
            }
        )

    private fun isUsernameValid(username: String): Boolean =
        username.isNotBlank() && username.contains(" ").not()

    private fun isPasswordValid(password: String): Boolean =
        password.length > 5
}

data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val nameError: Int? = null,
    val isDataValid: Boolean = false
)