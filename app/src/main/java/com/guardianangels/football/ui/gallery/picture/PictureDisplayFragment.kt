package com.guardianangels.football.ui.gallery.picture

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.guardianangels.football.R
import com.guardianangels.football.data.Picture
import com.guardianangels.football.databinding.GalleryPictureDisplayFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PictureDisplayFragment : Fragment(R.layout.gallery_picture_display_fragment) {

    private val args: PictureDisplayFragmentArgs by navArgs()
    private val viewModel: PictureDisplayViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = GalleryPictureDisplayFragmentBinding.bind(view)

        val picture = args.picture

        binding.imageView.load(picture.imageLink)
        binding.descriptionTV.text = picture.imageDescription

        if (!viewModel.isUserLoggedIn)
            binding.deleteButton.visibility = View.GONE
        else
            setupDeleteButton(binding, picture)


        val navController = findNavController()
        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }

        viewModel.deleteCompleteStatus.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    setFragmentResult(
                        Constants.REQUEST_GALLERY_IMAGE_DELETE_COMPLETE_KEY,
                        bundleOf(Constants.BUNDLE_GALLERY_IMAGE_DELETE_COMPLETE to true)
                    )
                    navController.popBackStack()
                }
                is NetworkState.Failed -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(view, "Failed to delete. ${it.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupDeleteButton(binding: GalleryPictureDisplayFragmentBinding, picture: Picture) {
        binding.deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("This picture will be permanently deleted.")
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Delete") { dialog, _ ->
                    viewModel.deletePicture(picture)
                    dialog.dismiss()
                }
                .show()
        }
    }
}