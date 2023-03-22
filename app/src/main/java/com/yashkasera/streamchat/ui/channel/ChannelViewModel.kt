package com.yashkasera.streamchat.ui.channel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yashkasera.streamchat.AppObjectController
import com.yashkasera.streamchat.R
import com.yashkasera.streamchat.adapter.MemberAdapter
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.launch
import java.io.IOException

class ChannelViewModel : ViewModel() {
    private var members = mutableListOf<User>()
    val membersAdapter = MemberAdapter().apply {
        showEndIcon = true
        onEndItemClick = { user ->
            removeMember(user)
        }
    }
    private val _channelForm = MutableLiveData<CreateChannelFormState>()
    val channelFormState: LiveData<CreateChannelFormState> = _channelForm
    var channelName = ""
        set(value) {
            field = value
            updateFormState()
        }

    fun addMember(user: User) {
        if (members.contains(user)) return
        members.add(user)
        membersAdapter.submitList(members.sortedBy { it.name }.toSet().toMutableList())

    }

    private fun removeMember(user: User) {
        members.remove(user)
        membersAdapter.submitList(members.sortedBy { it.name }.toSet().toMutableList())
    }

    private fun updateFormState() {
        when {
            channelName.isEmpty() -> _channelForm.value = CreateChannelFormState(nameError = R.string.invalid_name)
            members.isEmpty() -> _channelForm.value = CreateChannelFormState(membersError = 1)
            else ->
                _channelForm.value = CreateChannelFormState(isDataValid = true)
        }
    }

    fun createChannel(userId: String) {
        viewModelScope.launch {
            val ids = members.map { it.id }.toMutableList().apply { add(userId) }.toSet()
            val res = AppObjectController.client.channel(
                channelType = "messaging",
                channelId = channelName,
            ).create(
                memberIds = ids.toList(),
                extraData = emptyMap()
            ).await()
            if (res.isSuccess) {
                _channelForm.postValue(CreateChannelFormState(result = Result.success("Channel Created")))
            } else {
                _channelForm.postValue(CreateChannelFormState(result = Result.failure(IOException(res.error().message))))
            }
        }
    }
}

data class CreateChannelFormState(
    val nameError: Int? = null,
    val membersError: Int? = null,
    val isDataValid: Boolean = false,
    val result: Result<String>? = null
)