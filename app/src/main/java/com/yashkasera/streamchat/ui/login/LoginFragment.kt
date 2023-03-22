package com.yashkasera.streamchat.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.data.Result
import com.yashkasera.streamchat.databinding.FragmentLoginBinding


class LoginFragment : BaseFragment() {
    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel by lazy { ViewModelProvider(this)[LoginViewModel::class.java] }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        binding.initUi()
    }

    private fun FragmentLoginBinding.initUi() {
        etUsername.doAfterTextChanged {
            loginViewModel.loginDataChanged(
                username = etUsername.text.toString(),
                password = etPassword.text.toString()
            )
        }

        etPassword.apply {
            doOnTextChanged { _, _, _, _ ->
                loginViewModel.loginDataChanged(
                    username = etUsername.text.toString(),
                    password = etPassword.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            etUsername.text.toString(),
                            etPassword.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loginViewModel.login(etUsername.text.toString(), etPassword.text.toString())
            }

            loginGuest.setOnClickListener { findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToGuestFragment()) }
        }
    }

    override fun setObservers() {
        loginViewModel.loginFormState.observe(viewLifecycleOwner) {
            val loginState = it ?: return@observe
            binding.login.isEnabled = loginState.isDataValid
            if (loginState.usernameError != null)
                binding.ltUsername.error = getString(loginState.usernameError)
            else
                binding.ltUsername.error = null
            if (loginState.passwordError != null)
                binding.ltPassword.error = getString(loginState.passwordError)
            else
                binding.ltPassword.error = null
        }
        loginViewModel.loginResult.observe(viewLifecycleOwner) {
            val loginResult = it ?: return@observe
            binding.loading.isVisible = loginResult is Result.Loading
            binding.login.isEnabled = loginResult !is Result.Loading
            when (loginResult) {
                is Result.Error -> {
                    (requireActivity() as LoginActivity).showLoginFailed(
                        loginResult.exception.localizedMessage ?: loginResult.exception.message ?: "Error"
                    )
                }
                is Result.Success -> {
                    (requireActivity() as LoginActivity).updateUiWithUser()
                }
                is Result.Loading -> Unit
            }
        }
    }
}