package com.guardianangels.football.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.guardianangels.football.R
import com.guardianangels.football.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
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
                R.id.addPlayerFragment,
                R.id.addUpcomingMatchFragment,
                R.id.loginFragment,
                R.id.playerDetailsFragment,
                R.id.matchPlayerListFragment -> bottomNavigationBar.visibility = View.GONE
                else -> bottomNavigationBar.visibility = View.VISIBLE
            }

        }

        // Hide the bottom navigation bar for playerListFragment if the previous fragment was addUpcomingFragment
        if (navController.previousBackStackEntry?.destination?.id == R.id.addUpcomingMatchFragment) {
            bottomNavigationBar.visibility = View.GONE
        }

        setContentView(binding.root)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}