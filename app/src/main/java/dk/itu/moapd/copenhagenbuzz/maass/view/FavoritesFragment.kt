package dk.itu.moapd.copenhagenbuzz.maass.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.FavoritesAdapter

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.favoritesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoritesAdapter(emptyList(),emptyList()) // start with empty list
        recyclerView.adapter = adapter

        // Get ViewModel
        viewModel = ViewModelProvider(requireActivity())[EventViewModel::class.java]

        // Initialize sample events if needed
        viewModel.initializeSampleEvents()

        // Observe favorites
        viewModel.favoritesLiveData.observe(viewLifecycleOwner) { favoriteEvents ->
            adapter.updateData(favoriteEvents)
        }
    }
}
