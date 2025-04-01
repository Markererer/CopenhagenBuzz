package dk.itu.moapd.copenhagenbuzz.maass.view
import android.os.Bundle
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
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel // Import your ViewModel

/**
 * Main activity for the CopenhagenBuzz app, handling navigation between fragments.
 *
 * This activity sets up the Bottom Navigation Bar and Floating Action Button (FAB),
 * coordinating navigation via Jetpack Navigation. It uses View Binding to interact
 * with UI components and delegates event data management to [EventViewModel].
 */

class MainActivity : AppCompatActivity() { // Remove <EventViewModel>

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: EventViewModel by viewModels() // Use the actual ViewModel class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Add insets handling to avoid overlapping with the status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
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
                        addEventFragment?.saveEvent() // Call the fragment's method
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
}