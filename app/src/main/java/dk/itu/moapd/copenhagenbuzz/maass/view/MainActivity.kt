package dk.itu.moapd.copenhagenbuzz.maass.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dk.itu.moapd.copenhagenbuzz.maass.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel

/**
 * Main activity for the CopenhagenBuzz app, handling navigation between fragments and event management.
 *
 * This activity sets up the Bottom Navigation Bar and Floating Action Button (FAB), coordinating
 * navigation via Jetpack Navigation. It uses ViewBinding for UI interaction, manages login/guest
 * states with an icon toggle, and delegates event data to [EventViewModel]. Users can switch
 * to [LoginActivity] for authentication or guest mode.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: EventViewModel by viewModels()
    private var isLoggedIn: Boolean = false

    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get login state from Intent
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        invalidateOptionsMenu() // Update menu based on login state

        // Add insets handling to avoid overlapping with system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to the root layout to avoid overlap with status bar
            view.updatePadding(top = insets.top, bottom = insets.bottom)
            // Apply padding to BottomNavigationView to avoid overlap with navigation bar
            binding.bottomNavigation.updatePadding(bottom = insets.bottom)
            // Adjust FAB margin if needed (handled by layout constraints)
            windowInsets
        }

        // Navigation setup
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Observe navigation changes to update FAB
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addEventFragment -> {
                    binding.fabAddEvent.setImageResource(R.drawable.baseline_save_24)
                    binding.fabAddEvent.setOnClickListener {
                        val addEventFragment = navHostFragment.childFragmentManager.fragments
                            .firstOrNull { it is AddEventFragment } as? AddEventFragment
                        addEventFragment?.saveEvent()
                    }
                }
                else -> {
                    binding.fabAddEvent.setImageResource(R.drawable.baseline_add_24)
                    binding.fabAddEvent.setOnClickListener {
                        if (navController.currentDestination?.id != R.id.addEventFragment) {
                            navController.navigate(R.id.addEventFragment)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.action_account)
        if (isLoggedIn) {
            menuItem.setIcon(R.drawable.baseline_account_circle_24)
            menuItem.setOnMenuItemClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
        } else {
            menuItem.setIcon(R.drawable.baseline_close_24)
            menuItem.setOnMenuItemClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }
}