package com.yashkasera.streamchat.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.data.Result
import com.yashkasera.streamchat.databinding.FragmentGuestBinding


class GuestFragment : BaseFragment() {
    private lateinit var binding: FragmentGuestBinding
    private val loginViewModel by lazy { ViewModelProvider(this)[LoginViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        binding.initUi()
    }

    private fun FragmentGuestBinding.initUi() {
        login.setOnClickListener { findNavController().popBackStack() }
        val onDataChanged = {
            loginViewModel.loginDataChanged(
                username = etUsername.text.toString(),
                name = etName.text.toString()
            )
        }
        etUsername.doAfterTextChanged { onDataChanged() }
        etName.doAfterTextChanged { onDataChanged() }
        loginGuest.setOnClickListener {
            loginViewModel.loginGuest(
                etUsername.text.toString(),
                etName.text.toString()
            )
        }
    }

    override fun setObservers() {
        loginViewModel.loginFormState.observe(viewLifecycleOwner) { loginFormState ->
            if (loginFormState == null) {
                return@observe
            }
            binding.loginGuest.isEnabled = loginFormState.isDataValid
            binding.ltUsername.error = when {
                loginFormState.usernameError != null -> getString(loginFormState.usernameError)
                else -> null
            }
            binding.ltName.error = when {
                loginFormState.nameError != null -> getString(loginFormState.nameError)
                else -> null
            }
        }
        loginViewModel.loginResult.observe(viewLifecycleOwner) {
            val loginResult = it ?: return@observe
            binding.loading.isVisible = loginResult is Result.Loading
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