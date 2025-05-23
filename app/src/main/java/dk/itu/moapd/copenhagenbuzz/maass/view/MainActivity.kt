package dk.itu.moapd.copenhagenbuzz.maass.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel

/**
 * Main activity that serves as the entry point for the app after login.
 * Handles navigation, UI setup, and user authentication state.
 */
class MainActivity : AppCompatActivity() {

    // View binding for the main activity layout
    private lateinit var binding: ActivityMainBinding
    // Navigation controller for fragment navigation
    private lateinit var navController: NavController
    // Shared ViewModel for event data
    private val viewModel: EventViewModel by viewModels()
    // Tracks if the user is logged in
    private var isLoggedIn: Boolean = false

    /**
     * Called when the activity is starting. Sets up UI, navigation, and authentication state.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.topAppBar)

        // Retrieve login state from intent
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        invalidateOptionsMenu()

        // Initialize sample events in the ViewModel
        viewModel.initializeSampleEvents()

        // Set initial visibility of the FAB based on login state
        binding.fabAddEvent.visibility = if (isLoggedIn) View.VISIBLE else View.GONE

        // Handle system window insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentFrame) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemInsets.top)
            binding.bottomNavigation.updatePadding(bottom = systemInsets.bottom)
            insets
        }

        // Set up navigation with the NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)

        // Configure FAB behavior based on navigation destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addEventFragment -> {
                    binding.fabAddEvent.setImageResource(R.drawable.baseline_save_24)
                    binding.fabAddEvent.setOnClickListener {
                        (navHostFragment.childFragmentManager.fragments
                            .firstOrNull { it is AddEventFragment } as? AddEventFragment)
                            ?.saveEvent()
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

            // Hide FAB on login screen or when not logged in
            binding.fabAddEvent.visibility = when {
                destination.id == R.id.emailInputLayout -> View.GONE
                isLoggedIn -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

    /**
     * Inflates the options menu.
     *
     * @param menu The options menu in which items are placed.
     * @return true for the menu to be displayed; false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Prepares the options menu by setting visibility based on login state.
     *
     * @param menu The options menu as last shown or first initialized.
     * @return true for the menu to be displayed; false otherwise.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.login).isVisible = !isLoggedIn
        menu.findItem(R.id.logout).isVisible = isLoggedIn
        menu.findItem(R.id.action_add_event).isVisible = isLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handles selection of menu items.
     *
     * @param item The selected menu item.
     * @return true if the event was handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "You are now logged out.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("isLoggedIn", false)
                })
                finish()
                true
            }
            R.id.action_add_event -> {
                navController.navigate(R.id.addEventFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}