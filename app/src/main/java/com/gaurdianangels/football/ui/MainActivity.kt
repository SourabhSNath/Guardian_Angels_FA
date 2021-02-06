package com.gaurdianangels.football.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.gaurdianangels.football.R
import com.gaurdianangels.football.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
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
                R.id.loginFragment,
                R.id.playerDetailsFragment -> bottomNavigationBar.visibility = View.GONE
                else -> bottomNavigationBar.visibility = View.VISIBLE
            }

        }

        setContentView(binding.root)
    }


}