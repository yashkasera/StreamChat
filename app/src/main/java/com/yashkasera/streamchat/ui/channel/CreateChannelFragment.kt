package com.yashkasera.streamchat.ui.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yashkasera.streamchat.base.BaseFragment
import com.yashkasera.streamchat.databinding.FragmentCreateChannelBinding
import com.yashkasera.streamchat.ui.dialogs.BottomSheetSearchRecyclerFragment
import com.yashkasera.streamchat.ui.dialogs.SearchType

class CreateChannelFragment : BaseFragment() {
    private lateinit var binding: FragmentCreateChannelBinding
    private val viewModel by lazy {
        ViewModelProvider(this)[ChannelViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun init() {
        binding.btnAddMember.setOnClickListener {
            val searchDialog = BottomSheetSearchRecyclerFragment.newInstance(SearchType.MEMBERS)
            searchDialog.setItemClickListener { user ->
                viewModel.addMember(user)
                searchDialog.dismiss()
            }
            searchDialog.show(childFragmentManager, "Search")
        }
        binding.etGroupName.doAfterTextChanged {
            viewModel.channelName = it.toString()
        }
        binding.rvMembers.adapter = viewModel.membersAdapter
        binding.btnCreateChannel.setOnClickListener { viewModel.createChannel(getUserId()) }
    }

    override fun setObservers() {
        viewModel.channelFormState.observe(viewLifecycleOwner) {
            binding.btnCreateChannel.isEnabled = it.isDataValid
            binding.etGroupName.error = it.nameError?.let { it1 -> getString(it1) }
            it.result?.let { result ->
                if (result.isSuccess) {
                    findNavController().popBackStack()
                    Snackbar.make(
                        binding.root,
                        "Channel created successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Snackbar.make(
                        binding.root,
                        result.exceptionOrNull()?.localizedMessage ?: "Error creating channel",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}