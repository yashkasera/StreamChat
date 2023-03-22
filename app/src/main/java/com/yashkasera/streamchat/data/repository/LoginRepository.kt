package com.yashkasera.streamchat.data.repository

import com.yashkasera.streamchat.AppObjectController
import com.yashkasera.streamchat.BuildConfig
import com.yashkasera.streamchat.data.Result
import com.yashkasera.streamchat.data.model.LoggedInUser
import com.yashkasera.streamchat.data.model.UserType
import java.io.IOException

class LoginRepository {
    suspend fun login(username: String, password: String): Result<LoggedInUser> = try {
        if (BuildConfig.DEBUG) {
            val fakeLoggedInUser = LoggedInUser(
                userId = username,
                displayName = username,
                token = username,
                type = UserType.USER
            )
            val res = AppObjectController.initUser(fakeLoggedInUser)
            if (res.isSuccess)
                Result.Success(fakeLoggedInUser)
            else {
                Result.Error(IOException(res.error().message))
            }
        } else
            Result.Error(IOException("Sign in is disabled in production!"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun loginGuest(username: String, displayName: String): Result<LoggedInUser> {
        val loggedInUser = LoggedInUser(
            userId = username,
            displayName = displayName,
            type = UserType.GUEST
        )
        val res = AppObjectController.initUser(loggedInUser)
        return if (res.isSuccess)
            Result.Success(loggedInUser)
        else
            Result.Error(IOException(res.error().message))
    }
}