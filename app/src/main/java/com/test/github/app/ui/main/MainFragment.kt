package com.test.github.app.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.test.github.app.R
import com.test.github.app.ui.utils.showErrorDialog
import com.test.github.app.ui.utils.textInputAsFlow
import com.test.github.app.ui.widget.PaginationScrollListener
import com.test.github.app.ui.widget.ReposAdapter
import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.Model
import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.model.SearchModel
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class MainFragment : Fragment(R.layout.main_fragment) {

    private val viewModel: MainViewModel by viewModel()

    private val adapter = ReposAdapter {
        when (it) {
            is RepoItem -> openLink(it)
        }
    }

    private var isLoading = false
    private var currentPage = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(appBarSearch)
    }

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
        menuHistory.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToHistoryFragment())
        }
        etSearch.textInputAsFlow()
            .drop(1)
            .debounce(1_000)
            .mapLatest { it.toString() }
            .onEach { viewModel.clearRepos(); currentPage = 0 }
            .onEach { viewModel.offerNewRepos(it, currentPage) }
            .launchIn(lifecycleScope)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        with(viewModel) {
            eventLiveData.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Result.Success -> validateSuccess(it.successData)
                    is Result.Failure -> showError(it.errorData)
                    is Result.Loading -> addStubsToAdapter(it.loadingData as ReposModel)
                    is Result.State.LOADING -> progress.show()
                    is Result.State.LOADED -> progress.hide()
                    is Result.State.EMPTY -> showStub()
                }
            })
        }
    }

    private fun loadNextPage(query: String) {
        lifecycleScope.launch {
            viewModel.offerNewRepos(query, currentPage)
        }
    }

    private fun validateSuccess(model: Model) {
        when (model) {
            is ReposModel -> updateList(model)
            is SearchModel -> currentPage = model.page
        }
    }

    private fun addStubsToAdapter(result: ReposModel) {
        stub.visibility = View.GONE
        lstRepos.visibility = View.VISIBLE
        progress.show()

        adapter.addStubs(result.repos)
    }

    private fun updateList(result: ReposModel) {
        isLoading = false
        stub.visibility = View.GONE
        lstRepos.visibility = View.VISIBLE
        progress.hide()

        adapter.setData(result.repos)
    }

    private fun showStub() {
        stub.visibility = View.VISIBLE
        lstRepos.visibility = View.GONE
        adapter.clearData()
    }

    private fun openLink(item: RepoItem) {
        viewModel.updateHistory(item)
        Intent(Intent.ACTION_VIEW, Uri.parse(item.repoUrl)).run {
            startActivity(this)
        }
    }

    private fun showError(exception: Throwable?, onDismiss: () -> Unit? = {}) {
        progress.hide()
        showStub()
        MaterialAlertDialogBuilder(requireContext())
            .setOnDismissListener { onDismiss() }
            .showErrorDialog(exception)
    }
}