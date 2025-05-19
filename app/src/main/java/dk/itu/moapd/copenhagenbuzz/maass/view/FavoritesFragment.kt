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

class FavoritesFragment : Fragment() {

    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

// In FavoritesFragment.kt

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("FavoritesFragment", "Current userId: $userId")
        if (userId == null) return
        val favoritesRef = dk.itu.moapd.copenhagenbuzz.maass.MyApplication.database
            .getReference("copenhagen_buzz/favorites/$userId")
            .orderByChild("eventDate")

        val options = FirebaseRecyclerOptions.Builder<Event>()
            .setQuery(favoritesRef, Event::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        adapter = FavoritesAdapter(options)
        recyclerView.adapter = adapter
    }
}