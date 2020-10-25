package eu.micer.tweety.presentation.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ViewBindingVH private constructor(val binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun createVH(
            parent: ViewGroup,
            block: (inflater: LayoutInflater, container: ViewGroup, attach: Boolean) -> ViewBinding
        ) = ViewBindingVH(block(LayoutInflater.from(parent.context), parent, false))
    }
}
