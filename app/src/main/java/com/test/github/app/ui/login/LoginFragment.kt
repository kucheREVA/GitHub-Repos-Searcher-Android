package com.test.github.app.ui.login

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.test.github.app.R
import com.test.github.app.ui.utils.showErrorDialog
import com.test.github.domain.model.Result
import kotlinx.android.synthetic.main.login_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
class LoginFragment : Fragment(R.layout.login_fragment) {

    companion object {
        private const val OAUTH_PROVIDER_TYPE = "github.com"
    }

    private val loginViewModel: LoginViewModel by viewModel()

    private val splashViewModel: SplashViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel()
    }

    private fun initListeners() {
        btnSignIn.setOnClickListener { proceedFirebaseAuth() }
    }

    private fun observeViewModel() {
        with(splashViewModel) {
            eventLiveData.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Result.State.UNAUTHORIZED -> showSignInButton()
                    is Result.Success -> goToMain()
                    is Result.Failure -> showError(it.errorData.message)
                }
            })
        }
        with(loginViewModel) {
            loginLiveData.observe(viewLifecycleOwner, Observer {
                when (it) {
                    is Result.Success -> goToMain()
                    is Result.State.UNAUTHORIZED -> showSignInButton()
                    is Result.Failure -> showError(it.errorData.message) {
                        requireActivity().finish()
                    }
                }
            })
        }
    }

    private fun goToMain() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToNavBarFragment())
    }

    private fun showSignInButton() {
        btnSignIn.isEnabled = true
        btnSignIn.visibility = View.VISIBLE
        progressCheckState.hide()
    }

    private fun proceedFirebaseAuth() {
        btnSignIn.isEnabled = false
        progressLogin.show()
        FirebaseAuth.getInstance().pendingAuthResult?.run {
            addOnSuccessListener { loginViewModel.onAuthSuccess(it) }
            addOnFailureListener { showError(it.message) }
        } ?: startSignInActivity()
    }

    private fun startSignInActivity() {
        val provider = OAuthProvider.newBuilder(OAUTH_PROVIDER_TYPE)
        FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(requireActivity(), provider.build())
            .apply {
                addOnSuccessListener { loginViewModel.onAuthSuccess(it) }
                addOnFailureListener { showError(it.message) }
            }
    }

    private fun showError(message: String?, onDismiss: () -> Unit? = {}) {
        btnSignIn.isEnabled = false
        progressLogin.hide()
        MaterialAlertDialogBuilder(requireContext())
            .setOnDismissListener { onDismiss() }
            .showErrorDialog(message)
    }
}