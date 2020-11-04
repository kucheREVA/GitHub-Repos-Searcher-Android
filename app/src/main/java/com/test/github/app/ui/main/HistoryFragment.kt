package com.test.github.app.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.test.github.app.R
import com.test.github.app.ui.utils.showErrorDialog
import com.test.github.app.ui.widget.ReposAdapter
import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import kotlinx.android.synthetic.main.history_fragment.*
import kotlinx.android.synthetic.main.search_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment(R.layout.history_fragment) {

    private val viewModel: HistoryViewModel by viewModel()

    private val adapter = ReposAdapter {
        when (it) {
            is RepoItem -> openLink(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(appBarHistory)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lstHistory.adapter = adapter
        lstHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        with(viewModel) {
            historyLiveData.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Result.Success -> updateList(it.successData as ReposModel)
                    is Result.Failure -> showError(it.errorData.message)
                    is Result.State.EMPTY -> showStub()
                    is Result.State.LOADING -> progressHistory.show()
                }
            })
        }
    }

    private fun updateList(result: ReposModel) {
        historyStub.visibility = View.GONE
        lstHistory.visibility = View.VISIBLE
        progressHistory.hide()

        adapter.setData(result.repos)
    }

    private fun showStub() {
        historyStub.visibility = View.VISIBLE
        lstRepos.visibility = View.GONE
    }

    private fun openLink(item: RepoItem) {
        Intent(Intent.ACTION_VIEW, Uri.parse(item.repoUrl)).run {
            startActivity(this)
        }
    }

    private fun showError(message: String?, onDismiss: () -> Unit? = {}) {
        progressHistory.hide()
        showStub()
        MaterialAlertDialogBuilder(requireContext())
            .setOnDismissListener { onDismiss() }
            .showErrorDialog(message)
    }
}