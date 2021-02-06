package com.gaurdianangels.football.ui.players.addplayer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.databinding.AddPlayerFragmentBinding
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.util.Constants.COACH
import com.gaurdianangels.football.util.Constants.DEFENDER
import com.gaurdianangels.football.util.Constants.FORWARD
import com.gaurdianangels.football.util.Constants.GOAL_KEEPER
import com.gaurdianangels.football.util.Constants.MIDFIELDER
import com.gaurdianangels.football.util.Converters.Companion.getPlayerType
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPlayerFragment : Fragment(R.layout.add_player_fragment) {

    private val viewModel: AddPlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AddPlayerFragmentBinding.bind(view)

        val playerNameET = binding.playerNameET
        val playerTypeDropdown = binding.dropDownPlayerType
        val doneButton = binding.doneButton
        val selectImageButton = binding.addPlayerImageButton
        val playerImageView = binding.playerImage
        val playerNumberET = binding.playerNumberET
        val nestedScrollView = binding.scrollView

        /**
         * Calls for storage access framework to pick the images
         */
        val pickImages = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                playerImageView.setImageURI(it)
                viewModel.playerImageUri(it)
            }
        }

        selectImageButton.setOnClickListener {
            // Get images only from the storage
            pickImages.launch("image/*")
        }

        val dropDownList = arrayListOf(
            GOAL_KEEPER,
            DEFENDER,
            FORWARD,
            MIDFIELDER,
            COACH
        )

        val dropDownListAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, dropDownList)
        playerTypeDropdown.apply {
            setText(dropDownList[1])
            setAdapter(dropDownListAdapter)
        }

        getPlayerStats(binding, playerTypeDropdown)


        doneButton.setOnClickListener {
            val playerName = playerNameET.text.toString().trim()
            val type = playerTypeDropdown.text.toString()
            val playerNumber = playerNumberET.text.toString()

            val isPlayerImageDrawableSelected = playerImageView.drawable != null

            val playerType = getPlayerType(type) // From converters

            val age = binding.ageET.text.toString().toInt()
            val weight = binding.weightET.text.toString().toInt()
            val height = binding.heightEt.text.toString().toInt()

            if (playerName.isNotBlank() && playerNumber.isNotBlank() && isPlayerImageDrawableSelected) {
                val player = Player(
                    playerName = playerName, playerType = playerType, playerNumber = playerNumber,
                    playerAge = age, playerWeight = weight, playerHeight = height
                )

                addPlayer(player)

            } else {
                if (!isPlayerImageDrawableSelected) {
                    Snackbar.make(view, "Please Select Player Image.", Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(view, "Please fill in important details.", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    doneButton.hide()
                } else {
                    doneButton.show()
                }
            }
        } else {
            var oldScrollY = 0
            nestedScrollView.viewTreeObserver.addOnScrollChangedListener {
                if (nestedScrollView.scrollY > oldScrollY) {
                    doneButton.hide()
                } else {
                    doneButton.show()
                }
                oldScrollY = nestedScrollView.scrollY
            }
        }
    }

    private fun getPlayerStats(binding: AddPlayerFragmentBinding, playerTypeDropdown: AutoCompleteTextView) {

        val savesTF = binding.savesTF
        val cleanSheetsTf = binding.cleanSheetTF

        playerTypeDropdown.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    val totalGames = getNumber(binding.gamesET.text.toString())
                    val saves = getNumber(binding.savesET.text.toString())
                    val cleanSheets = getNumber(binding.cleanSheetET.text.toString())

                    binding.categorySpecificTF.visibility = View.INVISIBLE
                }

                4 -> {
                    binding.categorySpecificTF.apply {
                        hint = "Wins"
                        visibility = View.VISIBLE
                    }

                    savesTF.visibility = View.INVISIBLE
                    cleanSheetsTf.visibility = View.INVISIBLE
                    val wins = getNumber(binding.categorySpecificET.text.toString())
                }

                else -> {
                    savesTF.visibility = View.INVISIBLE
                    cleanSheetsTf.visibility = View.INVISIBLE
                }
            }

        }

    }

    private fun getNumber(games: String): Int {
        return if (games == "") {
            0
        } else games.toInt()
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
}