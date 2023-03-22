package com.yashkasera.streamchat

import android.accounts.AuthenticatorException
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yashkasera.streamchat.data.model.LoggedInUser
import com.yashkasera.streamchat.data.model.UserType
import com.yashkasera.streamchat.ui.login.LoginActivity
import com.yashkasera.streamchat.util.EMPTY
import com.yashkasera.streamchat.util.PrefManager
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import java.text.DateFormat

object AppObjectController {
    lateinit var streamChatApplication: StreamChatApplication

    val gson: Gson by lazy {
        GsonBuilder().serializeNulls().setDateFormat(DateFormat.LONG).setPrettyPrinting().setLenient().create()
    }

    val client by lazy {
        ChatClient.Builder(BuildConfig.STREAM_API_KEY, streamChatApplication.applicationContext)
            .logLevel(if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING).withPlugin(
                StreamOfflinePluginFactory(
                    Config(
                        userPresence = true, persistenceEnabled = true, useSequentialEventHandler = true
                    ), streamChatApplication.applicationContext
                )
            ).debugRequests(BuildConfig.DEBUG).build()
    }

    fun init(streamChatApplication: StreamChatApplication) {
        this.streamChatApplication = streamChatApplication
    }

    suspend fun initUser(user: LoggedInUser): Result<ConnectionData> {
        if (client.getCurrentUser() != null)
            return Result.success(ConnectionData(client.getCurrentUser()!!, client.getConnectionId()!!))
        if (user.type == UserType.USER && user.token == null) {
            logout()
            return Result.error(AuthenticatorException("User token is null"))
        }
        val result: Result<ConnectionData> = if (user.type == UserType.USER) {
            client.connectUser(
                user = User(
                    id = user.userId,
                    name = LoggedInUser.getOrNull()?.displayName ?: "",
                    role = user.type.name.lowercase()
                ),
                token = if (BuildConfig.DEBUG)
                    client.devToken(user.userId)
                else
                    user.token ?: EMPTY
            ).await()
        } else
            client.connectGuestUser(
                userId = user.userId,
                username = user.displayName
            ).await()
        if (result.isSuccess) {
            LoggedInUser.set(
                user.copy(
                    userId = result.data().user.id,
                    connectionId = result.data().connectionId
                )
            )
        }
        return result
    }

    suspend fun logout() {
        PrefManager.clear()
        client.disconnect(true).await()
        streamChatApplication.startActivity(Intent(streamChatApplication, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}