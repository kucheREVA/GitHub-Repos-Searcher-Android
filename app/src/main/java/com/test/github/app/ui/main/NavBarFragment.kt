package com.test.github.app.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.test.github.app.R
import kotlinx.android.synthetic.main.nav_bar_fragment.*

class NavBarFragment : Fragment(R.layout.nav_bar_fragment) {

    //TODO Here I have a problem, for some reasons bottom nav bar
    // was always hidden by nav host fragment, I tried many things
    // and decide to implement navigation to history fragment in other way
    // cause it was already monday morning, and I don't have enough time.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavMenu.setupWithNavController(findNavController())
    }
}