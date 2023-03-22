package com.yashkasera.streamchat.ui.dialogs

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yashkasera.streamchat.AppObjectController
import com.yashkasera.streamchat.adapter.MemberAdapter
import com.yashkasera.streamchat.databinding.BottomsheetSearchRecyclerBinding
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BottomSheetSearchRecyclerFragment : BottomSheetDialogFragment() {
    private lateinit var binding: BottomsheetSearchRecyclerBinding
    private lateinit var searchType: SearchType
    private val memberAdapter = MemberAdapter()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomsheetSearchRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchType = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU)
            arguments?.getSerializable(SEARCH_TYPE, SearchType::class.java) ?: SearchType.MEMBERS
        else
            arguments?.getSerializable(SEARCH_TYPE) as SearchType
        init()
    }

    private var query: MutableStateFlow<String?> = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    fun init() {
        binding.etSearch.doAfterTextChanged {
            query.value = it?.toString()
        }
        lifecycleScope.launch {
            query.debounce(1000).collect {
                if (it.isNullOrEmpty()) return@collect
                when (searchType) {
                    SearchType.CHANNELS -> TODO()
                    SearchType.MEMBERS -> searchUsers(it)
                }
            }
        }
    }

    private fun searchUsers(query: String) = lifecycleScope.launch {
        val request = QueryUsersRequest(
            filter = Filters.autocomplete("id", query),
            offset = 0,
            limit = 10,
        )
        val usersRequest = AppObjectController.client.queryUsers(request).await()
        binding.recyclerView.adapter = memberAdapter.apply {
            showEndIcon = false
        }
        val users = usersRequest.data()
        memberAdapter.submitList(users)
    }

    fun setItemClickListener(listener: (user: User) -> Unit) {
        memberAdapter.onItemClick = listener
    }


    companion object {
        private const val SEARCH_TYPE = "search_type"
        fun newInstance(searchType: SearchType): BottomSheetSearchRecyclerFragment {
            val bundle = Bundle()
            bundle.putSerializable(SEARCH_TYPE, searchType)
            return BottomSheetSearchRecyclerFragment().apply {
                arguments = bundle
            }
        }
    }
}

enum class SearchType {
    CHANNELS, MEMBERS
}