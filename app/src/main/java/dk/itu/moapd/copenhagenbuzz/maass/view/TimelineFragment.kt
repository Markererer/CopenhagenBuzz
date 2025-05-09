package dk.itu.moapd.copenhagenbuzz.maass.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.R
import android.widget.ListView
import dk.itu.moapd.copenhagenbuzz.maass.view.EventAdapter

class TimelineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline, container, false)

        // Mock data for testing with new Event signature
        val events = listOf(
            Event(
                id = "1",
                eventName = "Copenhagen Jazz Festival",
                eventLocation = "Copenhagen",
                eventDate = 1710000000000L, // example timestamp
                eventType = "Concert",
                eventDescription = "Annual jazz festival.",
                imageResId = 0,
                photoUrl = "",
                eventAuthor = ""
            ),
            Event(
                id = "2",
                eventName = "Tech Conference",
                eventLocation = "IT University",
                eventDate = 1715000000000L,
                eventType = "Conference",
                eventDescription = "Tech talks and workshops.",
                imageResId = 0,
                photoUrl = "",
                eventAuthor = ""
            )
        )

        val eventListView = view.findViewById<ListView>(R.id.event_list_view)
        eventListView.adapter = EventAdapter(requireContext(), events)

        return view
    }
}
