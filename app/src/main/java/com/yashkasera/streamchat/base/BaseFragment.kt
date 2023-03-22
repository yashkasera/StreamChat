package com.yashkasera.streamchat.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.yashkasera.streamchat.data.model.LoggedInUser

abstract class BaseFragment : Fragment() {
    abstract fun init()
    abstract fun setObservers()

    fun getUser() = LoggedInUser.getOrNull() ?: throw Exception("User not logged in")

    fun getUserId() = getUser().userId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setObservers()
    }

    fun showSnackbar(view: View, text: String, actionText: String? = null, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) {
                action()
            }
        }
        snackbar.show()
    }
}