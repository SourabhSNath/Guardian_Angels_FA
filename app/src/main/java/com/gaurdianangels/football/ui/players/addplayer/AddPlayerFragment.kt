package com.gaurdianangels.football.ui.players.addplayer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.data.PlayerType
import com.gaurdianangels.football.databinding.AddPlayerFragmentBinding
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.util.Constants.BUNDLE_PLAYER_UPLOAD_COMPLETE
import com.gaurdianangels.football.util.Constants.COACH
import com.gaurdianangels.football.util.Constants.REQUEST_PLAYER_UPLOAD_COMPLETE_KEY
import com.gaurdianangels.football.util.Converters.Companion.getPlayerType
import com.gaurdianangels.football.util.Converters.Companion.getPlayerTypeString
import com.gaurdianangels.football.util.Converters.Companion.toEmptySafeFloat
import com.gaurdianangels.football.util.Converters.Companion.toEmptySafeInt
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import dagger.hilt.android.AndroidEntryPoint

@Suppress("USELESS_CAST") // For Casting binding.view to MaterialAutoCompleteTextView. Seems to not work without this.
@AndroidEntryPoint
class AddPlayerFragment : Fragment(R.layout.add_player_fragment) {

    companion object {
        private const val TAG = "AddPlayerFragment"
    }

    private val viewModel: AddPlayerViewModel by viewModels()
    private var _binding: AddPlayerFragmentBinding? = null
    private val binding: AddPlayerFragmentBinding get() = _binding!!

    private val args: AddPlayerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddPlayerFragmentBinding.bind(view)

        val playerImageView = binding.playerImage
        val playerTypeDropdown = binding.dropDownPlayerType as MaterialAutoCompleteTextView // Doesn't infer automatically

        /**
         * Check if a player was passed into this fragment, if it is then this is meant to be updated
         */
        val isUpdatable = args.player != null
        Log.d(TAG, "onViewCreated: player ID = ${args.player?.id}")

        Log.d(TAG, "onViewCreated: isUpdatable = $isUpdatable")
        if (isUpdatable) {
            setUpFieldsForEditMode(args.player!!, playerTypeDropdown)
        } else {
            setupPlayerTypeDropdown(playerTypeDropdown)
        }

        binding.backButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        /**
         * Calls for storage access framework to pick the images
         */
        val pickImages = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                playerImageView.setImageURI(it)
                viewModel.playerImageUri(it)
            }
        }

        binding.addPlayerImageButton.setOnClickListener {
            // Get images only from the storage
            pickImages.launch("image/*")
        }

        getPlayerStats(playerTypeDropdown) // To show the relevant fields when type is selected


        binding.doneButton.setOnClickListener {
            val playerName = binding.playerNameET.text.toString().trim()
            val type = playerTypeDropdown.text.toString()
            val playerNumber = binding.playerNumberET.text.toString()

            val isPlayerImageDrawableSelected = playerImageView.drawable != null

            // From Converters.kt
            val playerType = type.getPlayerType()
            val age = binding.ageET.text.toString().toEmptySafeInt()
            val weight = binding.weightET.text.toString().toEmptySafeFloat()
            val height = binding.heightEt.text.toString().toEmptySafeFloat()

            val games = binding.gamesET.text.toString().toEmptySafeInt()
            val saves = binding.savesET.text.toString().toEmptySafeInt()
            val cleanSheets = binding.cleanSheetET.text.toString().toEmptySafeInt()

            val winsOrGoals = binding.categorySpecificET.text.toString().toEmptySafeInt()

            if (playerName.isNotBlank() && playerNumber.isNotBlank() && type.isNotBlank() && isPlayerImageDrawableSelected) {

                val player = Player(
                    playerName = playerName, playerType = playerType, playerNumber = playerNumber,
                    playerAge = age, playerWeight = weight, playerHeight = height,
                    totalGames = games, totalSaves = saves, totalCleanSheets = cleanSheets
                )

                if (type.getPlayerType() == PlayerType.COACH) {
                    player.totalWins = winsOrGoals
                } else {
                    player.totalGoals = winsOrGoals
                }

                if (isUpdatable) {
                    Log.d(TAG, "onViewCreated: Passing to updatePlayer()")
                    player.id = args.player?.id
                    player.remoteUri = args.player?.remoteUri
                    updatePlayer(player)
                } else {
                    Log.d(TAG, "onViewCreated: Passing to addPlayer()")
                    addPlayer(player)
                }


            } else {
                if (!isPlayerImageDrawableSelected) {
                    showSnackbar(view, "Please Select Player Image.")
                } else if (type.isBlank()) {
                    showSnackbar(view, "Please Select the Player Type")
                } else {
                    showSnackbar(view, "Please fill in important details.")
                }
            }
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Add New Player to firestore
     */
    private fun addPlayer(player: Player) {
        viewModel.addPlayer(player)

        viewModel.playerAddedRef.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                is NetworkState.Success -> {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    setFragmentResult(REQUEST_PLAYER_UPLOAD_COMPLETE_KEY, bundleOf(BUNDLE_PLAYER_UPLOAD_COMPLETE to true))
                    resetFields(null)
                }
                is NetworkState.Failed -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Update the player on firestore
     */
    private fun updatePlayer(player: Player) {
        Log.d(TAG, "updatePlayer: pass to viewModel update player")
        viewModel.updatePlayer(player)

        viewModel.playerUpdatedResult.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                is NetworkState.Success -> {
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    setFragmentResult(REQUEST_PLAYER_UPLOAD_COMPLETE_KEY, bundleOf(BUNDLE_PLAYER_UPLOAD_COMPLETE to true))
                    findNavController().popBackStack()
                }
                is NetworkState.Failed -> {
                    Log.d(TAG, "updatePlayer: ${it.message}")
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * When editing player selected from Details Fragment
     */
    private fun setUpFieldsForEditMode(player: Player, playerTypeDropdown: MaterialAutoCompleteTextView) {
        setupPlayerTypeDropdown(playerTypeDropdown, player.playerType?.getPlayerTypeString(true))
        resetFields(player)
    }


    private fun resetFields(playerModel: Player?) {

        if (playerModel?.remoteUri != null) {
            binding.playerImage.load(playerModel.remoteUri)
        } else {
            binding.playerImage.setImageURI(null)
        }
        binding.playerNameET.setText(playerModel?.playerName)
        binding.playerNumberET.setText(playerModel?.playerNumber)

        binding.ageET.setText(playerModel?.playerAge.toStringOrEmpty())
        binding.heightEt.setText(playerModel?.playerHeight.toStringOrEmpty())
        binding.weightET.setText(playerModel?.playerWeight.toStringOrEmpty())

        binding.gamesET.setText(playerModel?.totalGames.toStringOrEmpty())

        if (playerModel?.playerType == PlayerType.GOAL_KEEPER) {
            binding.categorySpecificTF.visibility = View.GONE
            binding.savesTF.visibility = View.VISIBLE
            binding.cleanSheetTF.visibility = View.VISIBLE

            binding.savesET.setText(playerModel.totalSaves.toStringOrEmpty())
            binding.cleanSheetET.setText(playerModel.totalCleanSheets.toStringOrEmpty())
        } else {
            if (playerModel?.totalWins != null) {
                binding.categorySpecificET.setText(playerModel.totalWins.toStringOrEmpty())
            } else {
                binding.categorySpecificET.setText(playerModel?.totalGoals.toStringOrEmpty())
            }
        }
    }

    private fun Any?.toStringOrEmpty(): String {
        if (this is Number) {
            if (this == 0 || this == 0.0) {
                return ""
            }
        }
        return this?.toString() ?: ""
    }

    /**
     * Set the drop down text
     */
    private fun setupPlayerTypeDropdown(playerTypeDropdown: MaterialAutoCompleteTextView, constantPlayerType: String? = null) {


        val dropDownListAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, viewModel.dropDownList)
        playerTypeDropdown.apply {

            if (constantPlayerType == null) {
                setText(viewModel.dropDownList[1])
            } else {
                setText(constantPlayerType)
                if (constantPlayerType == COACH) {
                    binding.playerNumber.isEnabled = false
                }
            }

            setAdapter(dropDownListAdapter)
        }
    }

    /**
     * Get the player stats.
     * When the dropdown text is selected, relevant player stats input boxes will be shown.
     */
    private fun getPlayerStats(playerTypeDropdown: MaterialAutoCompleteTextView) {

        playerTypeDropdown.setOnItemClickListener { _, _, position, _ ->

            when (position) {
                0 -> { // Goal Keeper
                    binding.categorySpecificTF.visibility = View.GONE
                    binding.savesTF.visibility = View.VISIBLE
                    binding.cleanSheetTF.visibility = View.VISIBLE
                    binding.playerNumber.isEnabled = true
                }

                4 -> { // Coaching Staff
                    binding.categorySpecificTF.apply {
                        visibility = View.VISIBLE
                        hint = "Wins"
                    }
                    binding.savesTF.visibility = View.GONE
                    binding.cleanSheetTF.visibility = View.GONE
                    binding.playerNumber.isEnabled = false
                }

                else -> { // Others
                    binding.categorySpecificTF.apply {
                        visibility = View.VISIBLE
                        hint = "Goals"
                    }
                    binding.savesTF.visibility = View.GONE
                    binding.cleanSheetTF.visibility = View.GONE
                    binding.playerNumber.isEnabled = true
                }
            }
        }
    }

    /**
     * Reset the dropdown autocompleteTextView after rotate.
     * This prevents a bug where the autoCompleteTextView loses all options.
     */
    override fun onPause() {
        super.onPause()
        (binding.dropDownPlayerType as MaterialAutoCompleteTextView).setText("", false)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}