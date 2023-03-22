package com.yashkasera.streamchat.ui.starred

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class StarredViewModel : ViewModel() {

    private val _state: MutableLiveData<State> = MutableLiveData(State())
    val state: LiveData<State> = _state

    private val _errorEvents: MutableLiveData<Event<Unit>> = MutableLiveData()
    val errorEvents: LiveData<Event<Unit>> = _errorEvents

    @OptIn(InternalStreamChatApi::class)
    private val scope = CoroutineScope(DispatcherProvider.Main)
    private var job: Job? = null
    private val logger = StreamLog.getLogger("Chat:SearchViewModel")

    init {
        _state.value = State(
            results = emptyList(),
            canLoadMore = true,
            isLoading = true,
            isLoadingMore = false,
        )
        searchMessages()
    }

    public fun loadMore() {
        job?.cancel()

        val currentState = _state.value!!
        if (currentState.canLoadMore && !currentState.isLoading && !currentState.isLoadingMore) {
            _state.value = currentState.copy(isLoadingMore = true)
            searchMessages()
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private fun searchMessages() {
        job = scope.launch {
            val currentState = _state.value!!
            val result = searchMessages(offset = currentState.results.size)
            if (result.isSuccess) {
                handleSearchMessageSuccess(result.data())
            } else {
                handleSearchMessagesError(result.error())
            }
        }
    }

    private fun handleSearchMessageSuccess(messages: List<Message>) {
        logger.d { "Found messages: ${messages.size}" }
        val currentState = _state.value!!
        _state.value = currentState.copy(
            results = currentState.results + messages,
            isLoading = false,
            isLoadingMore = false,
            canLoadMore = messages.size == QUERY_LIMIT
        )
    }

    private fun handleSearchMessagesError(chatError: ChatError) {
        logger.d { "Error searching messages: ${chatError.message}" }
        _state.value = _state.value!!.copy(
            isLoading = false,
            isLoadingMore = false,
            canLoadMore = true,
        )
        _errorEvents.value = Event(Unit)
    }

    private suspend fun searchMessages(offset: Int): Result<List<Message>> {
        logger.d { "Searching for with offset: $offset" }
        val currentUser = requireNotNull(ChatClient.instance().getCurrentUser())
        return ChatClient.instance()
            .searchMessages(
                channelFilter = Filters.`in`("members", listOf(currentUser.id)),
                messageFilter = Filters.contains("star", currentUser.id),
                offset = offset,
                limit = QUERY_LIMIT,
            )
            .await()
            .map { it.messages }
    }

    data class State(
        val canLoadMore: Boolean = true,
        val results: List<Message> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
    )

    private companion object {
        private const val QUERY_LIMIT = 30
    }
}