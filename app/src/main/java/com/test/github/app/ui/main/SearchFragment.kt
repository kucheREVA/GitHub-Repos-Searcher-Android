package com.test.github.app.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.test.github.app.R
import com.test.github.app.ui.utils.hideKeyboard
import com.test.github.app.ui.utils.showErrorDialog
import com.test.github.app.ui.utils.showMessageDialog
import com.test.github.app.ui.utils.textInputAsFlow
import com.test.github.app.ui.widget.PaginationScrollListener
import com.test.github.app.ui.widget.ReposAdapter
import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.Model
import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.model.SearchModel
import com.test.github.domain.usecase.SimpleResult
import kotlinx.android.synthetic.main.search_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class SearchFragment : Fragment(R.layout.search_fragment) {

    private val viewModel: SearchViewModel by viewModel()

    private val adapter = ReposAdapter {
        when (it) {
            is RepoItem -> openLink(it)
        }
    }

    private var isLoading = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(requireContext())

        lstRepos.adapter = adapter
        lstRepos.layoutManager = linearLayoutManager
        lstRepos.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            override fun loadMoreItems() {
                isLoading = true
                loadNextPage(etSearch.text.toString())
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun isLastPage(): Boolean {
                return false
            }

        })

        etSearch.setOnEditorActionListener { textView, actionId, keyEvent ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    viewModel.clearRepos()
                    viewModel.offerNewRepos(textView.text.toString())
                    true
                }
                else -> false
            }
        }

        etSearch.textInputAsFlow()
            .drop(1)
            .filter { it.isNullOrBlank() }
            .mapLatest { it.toString() }
            .onEach { viewModel.clearRepos() }
            .onEach { viewModel.offerNewRepos(it) }
            .onEach { }
            .launchIn(lifecycleScope)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        with(viewModel) {
            eventLiveData.observe(viewLifecycleOwner, Observer {
                if (!it.hasBeenHandled) {
                    if (it is Result.Success) {
                        validateSuccess(it.successData)
                    } else {
                        it.hasBeenHandled = true
                        handleSearchStates(it)
                    }
                }
            })
        }
    }

    private fun handleSearchStates(state: SimpleResult) {
        when (state) {
            is Result.Failure -> {
                showNoDataStub()
                state.errorData.run { Timber.e(this) }
                showError(state.errorData.message)
            }
            is Result.Loading -> addStubsToAdapter(state.loadingData as ReposModel)
            is Result.State.LOADING -> progress.show()
            is Result.State.LOADED -> progress.hide()
            is Result.State.EMPTY -> showNoDataStub { noReposFound() }
            is Result.State.COOL_DOWN_START -> coolDownStart()
            is Result.State.COOL_DOWN_RELEASED -> stopLoading()
            is Result.State.LIMIT_REACHED -> {
                adapter.removeStubs()
                MaterialAlertDialogBuilder(requireContext())
                    .showMessageDialog(
                        getString(R.string.search_no_more_repos_dialog_title),
                        getString(R.string.search_no_more_repos_dialog_message)
                    )
            }
        }
    }

    private fun loadNextPage(query: String) {
        lifecycleScope.launch {
            viewModel.offerNewRepos(query)
        }
    }

    private fun validateSuccess(model: Model) {
        when (model) {
            is ReposModel -> updateList(model)
            is SearchModel -> viewModel.updateCurrentPage(model.page)
        }
    }

    private fun addStubsToAdapter(result: ReposModel) {
        stub.visibility = View.GONE
        lstRepos.visibility = View.VISIBLE
        progress.show()

        adapter.addStubs(result.repos)
    }

    private fun updateList(result: ReposModel) {
        stopLoading()
        stub.visibility = View.GONE
        lstRepos.visibility = View.VISIBLE

        if (etSearch.isFocused) {
            etSearch.hideKeyboard()
            etSearch.clearFocus()
        }

        adapter.setData(result.repos)
    }

    private fun showNoDataStub(action: () -> Unit? = {}) {
        stub.visibility = View.VISIBLE
        lstRepos.visibility = View.GONE
        adapter.clearData()
        action()
    }

    private fun removeStubs() {
        stopLoading()
        adapter.removeStubs()
    }

    private fun noReposFound() {
        stopLoading()
        Toast.makeText(
            requireContext(),
            R.string.search_screen_no_repos_found_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openLink(item: RepoItem) {
        viewModel.updateHistory(item)
        Intent(Intent.ACTION_VIEW, Uri.parse(item.repoUrl)).run {
            startActivity(this)
        }
    }

    private fun coolDownStart() {
        viewModel.countDownDelay()
        removeStubs()
        showError(getString(R.string.search_fragment_error_limit_reached))
    }

    private fun stopLoading() {
        progress.hide()
        isLoading = false
    }

    private fun showError(message: String?, onDismiss: () -> Unit? = {}) {
        stopLoading()
        MaterialAlertDialogBuilder(requireContext())
            .setOnDismissListener { onDismiss() }
            .showErrorDialog(message)
    }
}