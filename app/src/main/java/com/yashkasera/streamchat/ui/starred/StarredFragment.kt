package com.yashkasera.streamchat.ui.starred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.databinding.FragmentStarredBinding
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.ui.search.list.SearchResultListView

class StarredFragment : BaseFragment() {
    private lateinit var binding: FragmentStarredBinding
    private val starredViewModel: StarredViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStarredBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        starredViewModel.bindView(binding.searchResultListView, viewLifecycleOwner)
        binding.searchResultListView.setSearchResultSelectedListener {
            findNavController().navigate(StarredFragmentDirections.actionStarredFragmentToChatFragment(it.cid))
        }
    }

    override fun setObservers() {
    }
}

private fun StarredViewModel.bindView(view: SearchResultListView, lifecycleOwner: LifecycleOwner) {
    state.observe(lifecycleOwner) { state ->
        when {
            state.isLoading -> {
                view.showLoading()
            }
            else -> {
                view.showMessages("", state.results)
                view.setPaginationEnabled(!state.isLoadingMore && state.results.isNotEmpty())
            }
        }
    }
    errorEvents.observe(
        lifecycleOwner,
        EventObserver {
            view.showError()
        }
    )
    view.setLoadMoreListener(::loadMore)
}

