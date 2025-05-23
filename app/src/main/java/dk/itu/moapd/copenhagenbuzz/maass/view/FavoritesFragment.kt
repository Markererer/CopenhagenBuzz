package dk.itu.moapd.copenhagenbuzz.maass.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.FavoritesAdapter

/**
 * Fragment that displays the user's favorite events in a RecyclerView.
 * Uses Firebase to fetch and display the list of favorite events for the current user.
 */
class FavoritesFragment : Fragment() {

    // Adapter for displaying favorite events in the RecyclerView
    private lateinit var adapter: FavoritesAdapter

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    /**
     * Called immediately after onCreateView.
     * Sets up the RecyclerView and binds it to the Firebase data source for the user's favorites.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get the current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("FavoritesFragment", "Current userId: $userId")
        if (userId == null) return

        // Reference to the user's favorites in the database, ordered by event date
        val favoritesRef = dk.itu.moapd.copenhagenbuzz.maass.MyApplication.database
            .getReference("copenhagen_buzz/favorites/$userId")
            .orderByChild("eventDate")

        // Set up FirebaseRecyclerOptions for the adapter
        val options = FirebaseRecyclerOptions.Builder<Event>()
            .setQuery(favoritesRef, Event::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        // Initialize and set the adapter for the RecyclerView
        adapter = FavoritesAdapter(options)
        recyclerView.adapter = adapter
    }
}