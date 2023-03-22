package com.yashkasera.streamchat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yashkasera.streamchat.databinding.ItemChannelMemberBinding
import io.getstream.chat.android.client.models.User

class MemberAdapter : ListAdapter<User, MemberAdapter.MemberViewHolder>(MemberDiffUtil()) {
    class MemberDiffUtil : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    var onEndItemClick: ((User) -> Unit)? = null
    var showEndIcon = true
    var onItemClick: ((User) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemChannelMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(private val binding: ItemChannelMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.root.setOnClickListener {
                onItemClick?.invoke(user)
            }
            binding.tvName.text = user.name
            binding.tvUsername.text = user.id
            binding.btnRemove.isVisible = showEndIcon
            binding.btnRemove.setOnClickListener { onEndItemClick?.invoke(user) }
        }
    }
}