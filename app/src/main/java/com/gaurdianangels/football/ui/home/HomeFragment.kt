package com.gaurdianangels.football.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gaurdianangels.football.R
import com.gaurdianangels.football.databinding.HomeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.home_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = HomeFragmentBinding.bind(view)

        val title = binding.appTitle

        title.setOnLongClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToLoginFragment())
            false
        }

        binding.moreUpcomingTV.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToAddUpcomingMatchFragment())
        }

    }
}