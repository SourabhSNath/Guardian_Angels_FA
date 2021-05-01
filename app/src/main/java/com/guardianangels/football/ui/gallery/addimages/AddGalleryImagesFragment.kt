package com.guardianangels.football.ui.gallery.addimages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.guardianangels.football.R
import com.guardianangels.football.databinding.AddGalleryImagesFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants.BUNDLE_GALLERY_IMAGE_UPLOAD_COMPLETE
import com.guardianangels.football.util.Constants.REQUEST_GALLERY_IMAGE_UPLOAD_COMPLETE_KEY
import com.guardianangels.football.util.getString
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AddGalleryImagesFragment : Fragment(R.layout.add_gallery_images_fragment) {

    private val args: AddGalleryImagesFragmentArgs by navArgs()
    private val viewModel: AddGalleryImagesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pictures = args.pictures
        val binding = AddGalleryImagesFragmentBinding.bind(view)
        binding.recyclerView.adapter = AddGalleryImagesAdapter(pictures.toList())

        binding.doneButton.setOnClickListener {
            viewModel.addGalleryImages(pictures.toList(), binding.imageDescriptionET.getString())
        }

        if (pictures.size == 1) {
            binding.optional.visibility = View.GONE
            binding.imageDescription.hint = "(Optional) Image Description"
        }

        val navController = findNavController()
        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }


        viewModel.addStatus.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkState.Success -> {
                    if (it.data) {
                        binding.progressBar.visibility = View.GONE
                        setFragmentResult(REQUEST_GALLERY_IMAGE_UPLOAD_COMPLETE_KEY, bundleOf(BUNDLE_GALLERY_IMAGE_UPLOAD_COMPLETE to true))
                        navController.popBackStack()
                    }
                }
                is NetworkState.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    Timber.d("${it.exception}, ${it.message}")
                    Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}