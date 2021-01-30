package com.gaurdianangels.football.ui.players

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gaurdianangels.football.R
import com.gaurdianangels.football.databinding.AddPlayerFragmentBinding
import com.gaurdianangels.football.model.Player
import com.gaurdianangels.football.network.NetworkState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPlayerFragment : Fragment(R.layout.add_player_fragment) {

    private val viewModel: AddPlayerViewModel by viewModels()

    private var _binding: AddPlayerFragmentBinding? = null
    private val binding get() = _binding!!

    private companion object {
        private const val STORAGE_ACCESS_REQUEST = 314
        private const val OPEN_IMAGE_DOC_CODE = 69
    }

    private val pickImages = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.playerImage.setImageURI(it)
            viewModel.playerImageUri(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddPlayerFragmentBinding.bind(view)

        val playerNameTV = binding.playerNameET
        val playerTypeDropdown = binding.dropDownPlayerType
        val saveButton = binding.saveButton
        val selectImageButton = binding.addPlayerImageButton
        val playerImageView = binding.playerImage

        selectImageButton.setOnClickListener {
            pickImages.launch("image/*")
//            requestStorageReadPermission()
//            Intent(Intent.ACTION_OPEN_DOCUMENT).also {
//                it.addCategory(Intent.CATEGORY_OPENABLE)
//                it.type = "image/*"
//                startActivityForResult(it, OPEN_IMAGE_DOC_CODE)
//            }

        }


        val dropDownList = arrayListOf<String>(
            "Goal Keeper",
            "Defender",
            "Forward",
            "Midfielder",
            "Coach"
        )

        val dropDownListAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, dropDownList)
        playerTypeDropdown.apply {
            setText(dropDownList[1])
            setAdapter(dropDownListAdapter)
        }


        saveButton.setOnClickListener {
            val playerName = playerNameTV.text.toString()
            val playerType = playerTypeDropdown.text.toString()

            if (playerName.isNotBlank() && playerImageView.drawable != null) {
                val player = Player(playerName, playerType)
                addPlayer(player)
            } else {
                Snackbar.make(view, "Please fill in important details.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun addPlayer(player: Player) {
        viewModel.addPlayer(player)

        viewModel.playerAddedRef.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Log.d("TAG", "addPlayer: LOADING")
                is NetworkState.Success -> Log.d("TAG", "addPlayer: Success")
                is NetworkState.Failed -> Log.d("TAG", "addPlayer: ${it.message}")
            }
        }
    }

    /**
     * Callback for the result from requesting permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_ACCESS_REQUEST) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                showPermissionRequestDialog()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Request permission for the storage.
     */
    private fun requestStorageReadPermission() {

        val context = context ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            return
        }

        /* This will explain to the user why they need this permission after they reject the permission the first time. */
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showPermissionRequestDialog()
        }

        /* Request the permission */
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_ACCESS_REQUEST)
    }

    private fun showPermissionRequestDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("This permission is required to access player photos from the storage.")
            .setTitle("Permission Required")
            .setPositiveButton("Accept") { _, _ ->
                parentFragment?.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_ACCESS_REQUEST)
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}