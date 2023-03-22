package com.yashkasera.streamchat.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.databinding.FragmentMentionsBinding
import io.getstream.chat.android.ui.mention.list.viewmodel.MentionListViewModel
import io.getstream.chat.android.ui.mention.list.viewmodel.bindView


class MentionsFragment : BaseFragment() {
    private lateinit var binding: FragmentMentionsBinding
    val viewModel: MentionListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMentionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        viewModel.bindView(binding.mentionsListView, viewLifecycleOwner)
        binding.mentionsListView.setMentionSelectedListener {
            findNavController().navigate(MentionsFragmentDirections.actionMentionsFragmentToChatFragment(it.cid))
        }
    }

    override fun setObservers() {}
}