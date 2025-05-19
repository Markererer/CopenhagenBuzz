package dk.itu.moapd.copenhagenbuzz.maass.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.database.FirebaseListOptions
import com.google.firebase.database.Query
import dk.itu.moapd.copenhagenbuzz.maass.MyApplication
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel
import android.util.Log
import androidx.navigation.fragment.findNavController

class TimelineFragment : Fragment() {

    private var eventAdapter: EventAdapter? = null
    private lateinit var viewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline, container, false)
        val eventListView = view.findViewById<ListView>(R.id.event_list_view)

        viewModel = ViewModelProvider(requireActivity())[EventViewModel::class.java]
        viewModel.loadFavorites()

        val query: Query = MyApplication.database
            .getReference("copenhagen_buzz/events")
            .orderByChild("eventDate")

        val options = FirebaseListOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .setLayout(R.layout.event_row_item)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        viewModel.favoritesLiveData.observe(viewLifecycleOwner) { favoriteEvents: List<Event> ->
            val favoriteIds = favoriteEvents.map { it.id }.toSet()

            Log.d("TimelineFragment", "favoriteIds: $favoriteIds")
            eventAdapter = EventAdapter(
                options,
                favoriteIds,
                onFavoriteClick = { event, isFavorite ->
                    if (isFavorite) viewModel.removeFavorite(event)
                    else viewModel.addFavorite(event)
                },
                onEditClick = { event ->
                    // Set the event to be edited in the ViewModel
                    viewModel.editingEvent = event
                    // Navigate to AddEventFragment
                    findNavController().navigate(R.id.addEventFragment)

                },
                onDeleteClick = { event ->
                    viewModel.deleteEvent(event)
                }
            )
            eventListView.adapter = eventAdapter
        }

        return view
    }
}