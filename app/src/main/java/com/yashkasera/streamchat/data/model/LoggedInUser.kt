package com.yashkasera.streamchat.data.model

import com.yashkasera.streamchat.util.PrefManager

data class LoggedInUser(
    val userId: String,
    val displayName: String,
    var token: String? = null,
    var type: UserType = UserType.USER,
    var connectionId: String? = null
) {
    companion object {
        fun set(loggedInUser: LoggedInUser) {
            PrefManager.put("user", loggedInUser)
        }

        fun getOrNull(): LoggedInUser? {
            return PrefManager.getObject("user", LoggedInUser::class.java)
        }
    }
}

enum class UserType {
    USER, GUEST
}