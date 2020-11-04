package com.test.github.app.ui.widget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.test.github.app.R
import com.test.github.domain.item.CooldownItem
import com.test.github.domain.item.RepoItem
import com.test.github.domain.item.SearchItem
import com.test.github.domain.item.StubItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_cool_down.view.*
import kotlinx.android.synthetic.main.item_repo.view.*

class ReposAdapter(private val onItemClick: ((SearchItem) -> Unit)) :
    RecyclerView.Adapter<SearchViewHolder>() {

    private val repos = mutableListOf<SearchItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return when (viewType) {
            ItemType.STUB.ordinal -> SearchViewHolder.StubViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_stub, parent, false)
            )
            ItemType.REPO.ordinal -> SearchViewHolder.RepoViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_repo, parent, false),
                onItemClick
            )
            ItemType.COOLDOWN.ordinal -> SearchViewHolder.CooldownHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_cool_down, parent, false)
            )
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount(): Int = repos.size

    override fun getItemViewType(position: Int): Int {
        return when (repos[position]) {
            is StubItem -> ItemType.STUB.ordinal
            is RepoItem -> ItemType.REPO.ordinal
            is CooldownItem -> ItemType.COOLDOWN.ordinal
        }
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        when (holder) {
            is SearchViewHolder.RepoViewHolder -> holder.bind(repos[position] as RepoItem)
            is SearchViewHolder.CooldownHolder -> holder.bind(repos[position] as CooldownItem)
        }
    }

    fun removeStubs() {
        repos.removeAll { it is StubItem }
        notifyItemRangeRemoved(repos.size, 30)
    }

    fun addStubs(stubs: List<SearchItem>) {
        repos.addAll(stubs)
        notifyItemRangeInserted(repos.size, stubs.size)
    }

    fun setData(newRepos: List<SearchItem>) {
        calculateDiff(repos, newRepos) {
            repos.clear()
            repos.addAll(it)
        }
    }

    fun clearData() {
        repos.clear()
        notifyDataSetChanged()
    }

    private inline fun calculateDiff(
        oldRepos: List<SearchItem>,
        newRepos: List<SearchItem>,
        action: (new: List<SearchItem>) -> Unit = { _ -> }
    ) {
        val diffCallback = DiffCallback(oldRepos, newRepos)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        action(newRepos)
        diffResult.dispatchUpdatesTo(this)
    }
}

enum class ItemType {
    STUB, REPO, COOLDOWN
}

sealed class SearchViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView), LayoutContainer {

    data class StubViewHolder(override val containerView: View) : SearchViewHolder(containerView)

    data class CooldownHolder(
        override val containerView: View
    ) : SearchViewHolder(containerView) {

        fun bind(item: CooldownItem) {
            with(containerView) {
                coolDownTitle?.text = item.coolDownValue
            }
        }
    }

    data class RepoViewHolder(
        override val containerView: View,
        val onItemClick: (RepoItem) -> Unit
    ) : SearchViewHolder(containerView) {

        fun bind(item: RepoItem) {
            with(containerView) {
                txtRepoName?.text = item.name
                txtRepoDescription?.text = item.description
                txtRepoStars?.text = "${item.starsCount}"
                setOnClickListener { onItemClick(item) }
            }
        }
    }
}

