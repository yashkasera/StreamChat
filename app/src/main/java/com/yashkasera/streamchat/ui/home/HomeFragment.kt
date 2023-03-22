package com.yashkasera.streamchat.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.databinding.FragmentHomeBinding
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory


class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModelFactory: ChannelListViewModelFactory
    val viewModel: ChannelListViewModel by viewModels { viewModelFactory }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", listOf(getUserId()))
        )
        viewModelFactory = ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)
        viewModel.bindView(binding.channelListView, this)
        binding.channelListView.setChannelItemClickListener { channel ->
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChatFragment(channel.cid))
        }
        binding.btnCreateChannel.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCreateChannelFragment())
        }
    }

    override fun setObservers() {

    }
}