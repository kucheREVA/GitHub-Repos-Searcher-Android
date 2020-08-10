package com.test.github.app.ui.widget

import androidx.recyclerview.widget.DiffUtil
import com.test.github.domain.item.SearchItem

class DiffCallback(
    private val oldList: List<SearchItem>,
    private val newList: List<SearchItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].itemId == newList[newItemPosition].itemId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}