package com.guardianangels.football.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.guardianangels.football.R
import com.guardianangels.football.databinding.ActivityMainBinding
import com.guardianangels.football.util.NetworkConnectionLiveData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)

        var isConnectionLost = false
        NetworkConnectionLiveData(this).observe(this) {
            isConnectionLost = if (it == false) {
                Toast.makeText(this, "Please check your internet.", Toast.LENGTH_LONG).show()
                true
            } else {
                if (isConnectionLost) Toast.makeText(this, "Internet Connected.", Toast.LENGTH_SHORT).show()
                false
            }
        }

        val bottomNavigationBar = binding.bottomNavigation

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationBar.apply {

            setupWithNavController(navController)

            /**
             * Prevent reloading the same fragment on click
             */
            setOnNavigationItemSelectedListener {

                // If current fragment is not the destination then navigate
                if (it.itemId != navController.currentDestination!!.id) {
                    // Attempt to navigate to the [NavDestination] associated with this [MenuItem]
                    it.onNavDestinationSelected(navController)
                }

                true
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.home, R.id.players, R.id.matchListFragment, R.id.gallery -> bottomNavigationBar.visibility = View.VISIBLE
                else -> bottomNavigationBar.visibility = View.GONE
            }
        }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}