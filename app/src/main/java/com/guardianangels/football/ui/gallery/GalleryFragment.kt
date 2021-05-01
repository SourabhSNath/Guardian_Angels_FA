package com.guardianangels.football.ui.gallery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.guardianangels.football.R
import com.guardianangels.football.databinding.GalleryFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants.BUNDLE_GALLERY_IMAGE_DELETE_COMPLETE
import com.guardianangels.football.util.Constants.BUNDLE_GALLERY_IMAGE_UPLOAD_COMPLETE
import com.guardianangels.football.util.Constants.REQUEST_GALLERY_IMAGE_DELETE_COMPLETE_KEY
import com.guardianangels.football.util.Constants.REQUEST_GALLERY_IMAGE_UPLOAD_COMPLETE_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.gallery_fragment) {

    private val viewModel: GalleryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = GalleryFragmentBinding.bind(view)
        val navController = findNavController()

        setFragmentResultListener(REQUEST_GALLERY_IMAGE_UPLOAD_COMPLETE_KEY) { _, bundle ->
            val result = bundle.getBoolean(BUNDLE_GALLERY_IMAGE_UPLOAD_COMPLETE)
            if (result) {
                viewModel.getGalleryImages()
                Timber.d("Reload Gallery")
            }
        }

        setFragmentResultListener(REQUEST_GALLERY_IMAGE_DELETE_COMPLETE_KEY) { _, bundle ->
            val result = bundle.getBoolean(BUNDLE_GALLERY_IMAGE_DELETE_COMPLETE)
            if (result) {
                viewModel.getGalleryImages()
                Timber.d("Reload Gallery")
            }
        }

        val recyclerView = binding.recyclerview
        val adapter = GalleryImagesAdapter {
            navController.navigate(GalleryFragmentDirections.actionGalleryToPictureDisplayFragment(it))
        }
/*
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = staggeredGridLayoutManager*/
        recyclerView.adapter = adapter

        if (!viewModel.isUserLoggedIn) {
            binding.addButton.visibility = View.GONE
        }

        pickImages(navController, binding)

        var ascending = true
        binding.sortButton.setOnClickListener {
            ascending = !ascending
            if (ascending) Toast.makeText(requireContext(), "Sorting by new.", Toast.LENGTH_SHORT).show()
            else Toast.makeText(requireContext(), "Sorting by old.", Toast.LENGTH_SHORT).show()
            viewModel.getGalleryImages(ascending)
        }


        viewModel.pictureList.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (it.data.isNotEmpty()) {
                        adapter.submitList(it.data)
                    }
                }
                is NetworkState.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    Timber.d("$it, ${it.message}")
                    Snackbar.make(view, it.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Calls for storage access framework to pick the images
     */
    private fun pickImages(navController: NavController, binding: GalleryFragmentBinding) {

        val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            it?.let {
                if (it.isNotEmpty())
                    navController.navigate(GalleryFragmentDirections.actionGalleryToAddGalleryImagesFragment(it.toTypedArray()))
            }
        }
        binding.addButton.setOnClickListener {
            pickImages.launch("image/*")
        }
    }

}