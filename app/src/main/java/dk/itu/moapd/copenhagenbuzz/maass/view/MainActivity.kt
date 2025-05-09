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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: EventViewModel by viewModels()
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.topAppBar)

        // Retrieve login state
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        invalidateOptionsMenu()

        // Set initial FAB visibility
        binding.fabAddEvent.visibility = if (isLoggedIn) View.VISIBLE else View.GONE

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentFrame) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemInsets.top)
            binding.bottomNavigation.updatePadding(bottom = systemInsets.bottom)
            insets
        }

        // Navigation setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)

        // Handle FAB logic based on destination
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

            // Hide FAB in login screen or when not logged in
            binding.fabAddEvent.visibility = when {
                destination.id == R.id.emailInputLayout -> View.GONE
                isLoggedIn -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.login).isVisible = !isLoggedIn
        menu.findItem(R.id.logout).isVisible = isLoggedIn
        menu.findItem(R.id.action_add_event).isVisible = isLoggedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
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
