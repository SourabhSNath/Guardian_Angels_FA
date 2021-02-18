package com.gaurdianangels.football.ui.matches

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.gaurdianangels.football.R
import com.gaurdianangels.football.databinding.PlayerListFragmentBinding
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.ui.players.adapter.SectionedPlayerListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectTeamFragment : Fragment(R.layout.player_list_fragment) {

    private val viewModel: SelectedTeamViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = PlayerListFragmentBinding.bind(view)
        binding.optionsButton.visibility = View.GONE
        val addButton = binding.confirmButton
        val recyclerView = binding.playersListRecyclerView

        /*val sectionAdapter = SectionedPlayerListAdapter(viewLifecycleOwner) {
            // Do nothing, since it starts in multi_selection mode
        }

        recyclerView.apply {
            this.adapter = sectionAdapter

            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (sectionAdapter.getItemViewType(position) == -2) 1 else gridLayoutManager.spanCount
                }

            }
            layoutManager = gridLayoutManager
        }*/

        addButton.visibility = View.VISIBLE

//        observeViewModel(sectionAdapter)
    }

}

/*private fun observeViewModel(sectionAdapter: SectionedPlayerListAdapter) {
    viewModel.teamList.observe(viewLifecycleOwner) {
        when (it) {
            is NetworkState.Loading -> {
            }
            is NetworkState.Success -> sectionAdapter.submitList(it.data)
            is NetworkState.Failed -> {

            }
        }
    }*/

