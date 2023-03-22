package com.yashkasera.streamchat.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.State.NavigateUp
import com.yashkasera.streamchat.AppObjectController
import com.yashkasera.streamchat.R
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.data.model.RecordingItem
import com.yashkasera.streamchat.databinding.FragmentChatBinding
import com.yashkasera.streamchat.ui.dialogs.DialogRecordAudio
import com.yashkasera.streamchat.util.showToast
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.common.state.CustomAction
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel
import io.getstream.chat.android.ui.message.composer.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.options.message.DefaultMessageOptionItemsFactory
import io.getstream.chat.android.ui.message.list.options.message.MessageOptionItem
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory


@OptIn(ExperimentalStreamChatApi::class)
class ChatFragment : BaseFragment() {
    private lateinit var binding: FragmentChatBinding

    private val args: ChatFragmentArgs by navArgs()
    private lateinit var factory: MessageListViewModelFactory
    private lateinit var messageListHeaderViewModel: MessageListHeaderViewModel
    private lateinit var messageListViewModel: MessageListViewModel
    private lateinit var messageComposerViewModel: MessageComposerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        val channelId = args.channelId
        factory = MessageListViewModelFactory(channelId)
        initMessageHeader()
        initMessageList()
        initMessageInput()
    }

    @OptIn(ExperimentalStreamChatApi::class)
    private fun initMessageInput() {
        messageComposerViewModel = ViewModelProvider(this, factory)[MessageComposerViewModel::class.java]
        messageComposerViewModel.bindView(binding.messageComposerView, this)
        val customMessageComposerTrailingContent = CustomMessageComposerTrailingContent(requireContext()).also { it ->
            it.action = TrailingAction.RECORD
            it.recordButtonClickListener = {
                val dialogRecordAudio = DialogRecordAudio.newInstance()
                dialogRecordAudio.setOnAudioRecordListener = { recordingItem ->
                    uploadFile(recordingItem)
                }
                dialogRecordAudio.show(childFragmentManager, "RecordAudioDialog")
            }
            it.sendButtonClickListener = {
                messageComposerViewModel.sendMessage(messageComposerViewModel.buildNewMessage())
            }
        }
        binding.messageComposerView.setTrailingContent(customMessageComposerTrailingContent)
        binding.messageComposerView.textInputChangeListener = { text ->
            customMessageComposerTrailingContent.action =
                if (text.isNotEmpty()) TrailingAction.SEND
                else TrailingAction.RECORD
            messageComposerViewModel.setMessageInput(text)
        }
    }

    private fun uploadFile(recordingItem: RecordingItem) {
        if (recordingItem.file == null) {
            showSnackbar(binding.root, "Unable to parse recording!")
            return
        }
        ChatClient.instance().channel(args.channelId).sendFile(recordingItem.file!!,
            object : ProgressCallback {
                override fun onError(error: ChatError) {
                    showToast(error.message ?: "Error occurred while uploading!")
                }

                override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
                    Log.d(
                        "ChatFragment.kt",
                        "YASH => onProgress:106 Progress ${bytesUploaded.div(totalBytes)}%- $bytesUploaded/$totalBytes"
                    )
                }

                override fun onSuccess(url: String?) {
                    AppObjectController.client.sendMessage(
                        "messaging", args.channelId, Message(
                            attachments = mutableListOf(
                                Attachment(
                                    authorName = getUser().displayName,
                                    url = url,
                                    type = "audio",
                                    uploadState = Attachment.UploadState.Success,
                                )
                            ),
                            cid = args.channelId
                        )
                    ).enqueue()
                }
            }).enqueue()
    }

    private fun initMessageHeader() {
        messageListHeaderViewModel = ViewModelProvider(this, factory)[MessageListHeaderViewModel::class.java]
        messageListHeaderViewModel.bindView(binding.messageListHeaderView, this)
    }

    private fun initMessageList() {
        messageListViewModel = ViewModelProvider(this, factory)[MessageListViewModel::class.java]
        binding.messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }
        binding.messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }
        binding.messageListView.setMessageOptionItemsFactory(object :
            DefaultMessageOptionItemsFactory(requireContext()) {
            override fun createMessageOptionItems(
                selectedMessage: Message,
                currentUser: User?,
                isInThread: Boolean,
                ownCapabilities: Set<String>,
                style: MessageListViewStyle
            ): List<MessageOptionItem> {
                val capabilities =
                    setOf(ChannelCapabilities.DELETE_OWN_MESSAGE, ChannelCapabilities.UPDATE_ANY_MESSAGE)
                val items =
                    super.createMessageOptionItems(selectedMessage, currentUser, isInThread, capabilities, style)
                        .toMutableList()
                items.firstOrNull { it.messageAction is Edit }?.let {
                    if (selectedMessage.user.id != currentUser?.id)
                        items.remove(it)
                }
                val list = selectedMessage.extraData.getOrDefault("star", listOf<String>()) as List<String>
                items.add(
                    0,
                    MessageOptionItem(
                        optionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star)!!,
                        optionText = if (list.contains(getUserId())) "Unstar Message" else "Star Message",
                        messageAction = CustomAction(
                            selectedMessage,
                            mapOf(Pair("type", "star"))
                        ),
                    )
                )
                return items
            }
        })
        binding.messageListView.setCustomActionHandler { message, extraProperties ->
            var text: String
            message.extraData.getOrDefault("star", mutableListOf<String>()).let {
                val list = it as List<String>
                if (list.contains(getUserId())) {
                    text = "Unstarred"
                    message.extraData["star"] = list.minus(getUserId())
                } else {
                    text = "Starred"
                    message.extraData["star"] = list.plus(getUserId())
                }
            }
            AppObjectController.client.partialUpdateMessage(message.id, message.extraData).enqueue {
                if (it.isSuccess) {
                    showSnackbar(binding.root, "Message $text successfully!")
                } else {
                    showSnackbar(binding.root, it.error().message ?: "Error occurred!")
                }
            }
        }
        messageListViewModel.bindView(binding.messageListView, this)
    }

    override fun setObservers() {
        messageListViewModel.mode.observe(viewLifecycleOwner) {
            when (it) {
                is MessageListViewModel.Mode.Thread -> {
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(it.parentMessage))
                }
                is MessageListViewModel.Mode.Normal -> {
                    messageComposerViewModel.leaveThread()
                }
            }
        }
        messageListViewModel.state.observe(this) { state ->
            if (state is NavigateUp) {
                findNavController().popBackStack()
            }
        }
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        binding.messageListHeaderView.setBackButtonClickListener(backHandler)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            backHandler()
        }
    }
}