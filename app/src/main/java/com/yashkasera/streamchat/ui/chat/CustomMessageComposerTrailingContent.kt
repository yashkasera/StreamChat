package com.yashkasera.streamchat.ui.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yashkasera.streamchat.databinding.ContentMessageTrailingBinding
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.message.composer.MessageComposerContext
import io.getstream.chat.android.ui.message.composer.content.MessageComposerContent

@ExperimentalStreamChatApi
class CustomMessageComposerTrailingContent : FrameLayout, MessageComposerContent {

    var action: TrailingAction = TrailingAction.RECORD
        set(value) {
            field = value
            when (value) {
                TrailingAction.SEND -> {
                    binding.btnSend.visibility = View.VISIBLE
                    binding.btnRecord.visibility = View.GONE
                }
                TrailingAction.RECORD -> {
                    binding.btnSend.visibility = View.GONE
                    binding.btnRecord.visibility = View.VISIBLE
                }
            }
        }
    var sendButtonClickListener: () -> Unit = {}

    var recordButtonClickListener: () -> Unit = {}

    private var binding: ContentMessageTrailingBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = ContentMessageTrailingBinding.inflate(LayoutInflater.from(context), this, true)
        binding.btnSend.setOnClickListener {
            sendButtonClickListener()
        }
        binding.btnRecord.setOnClickListener { recordButtonClickListener() }
    }

    override fun updateViewLayout(view: View?, params: ViewGroup.LayoutParams?) {
        super.updateViewLayout(view, params)
    }

    override fun attachContext(messageComposerContext: MessageComposerContext) {
    }

    override fun renderState(state: MessageComposerState) {
        // Render the state of the component
    }
}

enum class TrailingAction {
    SEND,
    RECORD
}