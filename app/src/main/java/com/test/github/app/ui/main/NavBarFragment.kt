package com.test.github.app.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.test.github.app.R
import com.test.github.app.ui.utils.KeepStateNavigator
import kotlinx.android.synthetic.main.nav_bar_fragment.*

class NavBarFragment : Fragment(R.layout.nav_bar_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_bar_host_fragment) as? NavHostFragment

        navHostFragment?.run {
            val navigator = KeepStateNavigator(
                requireContext(),
                childFragmentManager,
                R.id.nav_bar_host_fragment
            )

            navController.navigatorProvider.addNavigator(navigator)
            navController.setGraph(R.navigation.bottom_nav_graph)

            this@NavBarFragment.bottomNavMenu.setupWithNavController(navController)
        }
    }
}