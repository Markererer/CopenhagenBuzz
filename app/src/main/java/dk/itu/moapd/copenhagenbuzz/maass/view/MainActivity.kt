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
 * Main activity for the CopenhagenBuzz app, serving as the central hub for navigation and event management.
 *
 * This activity initializes the user interface with a Bottom Navigation Bar and a Floating Action Button (FAB),
 * utilizing Jetpack Navigation to manage transitions between fragments (e.g., Timeline, Favorites, Maps, Add Event).
 * It employs ViewBinding for secure UI component access and integrates with [EventViewModel] to manage event data.
 * The activity supports login/guest states, toggling the top-left action bar icon between an account (logged-in)
 * and logout (guest) representation, and uses explicit Intents to switch to [LoginActivity] for authentication
 * or mode changes. System window insets are handled to ensure compatibility with edge-to-edge displays.
 *
 * @property binding The ViewBinding instance for [ActivityMainBinding] to interact with the layout.
 * @property navController The [NavController] managing fragment navigation.
 * @property viewModel The [EventViewModel] instance handling event data via LiveData.
 * @property isLoggedIn A boolean flag indicating the user's login status, initialized from an Intent extra.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: EventViewModel by viewModels()
    private var isLoggedIn: Boolean = false

    companion object {
        /** Tag for logging purposes, set to the fully qualified class name. */
        private val TAG = MainActivity::class.qualifiedName
    }

    /**
     * Called when the activity is first created.
     *
     * Initializes the activity by setting up ViewBinding, handling system window insets, retrieving the
     * login state from an Intent, configuring the navigation controller with the Bottom Navigation Bar,
     * and setting up the FAB behavior based on the current navigation destination.
     *
     * @param savedInstanceState The saved instance state bundle, or null if none.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Existing view binding setup
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // TA's suggestion: Set up the top app bar
        setSupportActionBar(binding.topAppBar)

        // Get login state from Intent
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        invalidateOptionsMenu() // Update menu based on login state

        // Add insets handling to avoid overlapping with system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentFrame) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to the content frame to avoid overlap with status bar
            view.updatePadding(top = insets.top)
            // Apply padding to BottomNavigationView to avoid overlap with navigation bar
            binding.bottomNavigation.updatePadding(bottom = insets.bottom)
            windowInsets
        }

        // Navigation setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
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

    /**
     * Inflates the menu resource file for the action bar.
     *
     * This method loads the [R.menu.menu_main] resource to display the account/logout icon in the
     * action bar. The menu is updated dynamically in [onPrepareOptionsMenu].
     *
     * @param menu The [Menu] object to inflate the menu into.
     * @return `true` to indicate the menu has been inflated successfully.
     */
    // Merged menu handling
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Prepares the options menu before it is displayed.
     *
     * Updates the action bar icon and click behavior based on the [isLoggedIn] state. For logged-in
     * users, it shows an account icon and triggers logout; for guests, it shows a logout icon and
     * returns to [LoginActivity].
     *
     * @param menu The [Menu] object to prepare.
     * @return `true` to indicate the menu has been prepared, delegating to the superclass.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // TA's suggestion: Toggle visibility of login/logout items
        menu.findItem(R.id.login).isVisible = !isLoggedIn
        menu.findItem(R.id.logout).isVisible = isLoggedIn


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login, R.id.logout -> {
                // TA's suggested login/logout handling
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }



            else -> super.onOptionsItemSelected(item)
        }
    }
}
