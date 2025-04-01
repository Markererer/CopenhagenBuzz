package dk.itu.moapd.copenhagenbuzz.maass.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event

/**
 * Adapter for displaying a list of events in a ListView.
 *
 * This adapter binds event data to the views in the ListView, using a custom layout
 * defined in [R.layout.event_row_item]. It extends [ArrayAdapter] to handle the list of [Event] objects.
 *
 * @property context The context in which the adapter is used.
 * @property events The list of events to display.
 * @constructor Creates an instance of [EventAdapter].
 */
class EventAdapter(context: Context, private val events: List<Event>) :
    ArrayAdapter<Event>(context, R.layout.event_row_item, events) {

    /**
     * Provides a view for an event item in the ListView.
     *
     * This method inflates the custom layout for each event item and populates it with data
     * from the corresponding [Event] object.
     *
     * @param position The position of the event in the list.
     * @param convertView The recycled view to populate (if available).
     * @param parent The parent view group for the item.
     * @return A view populated with event data.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val event = getItem(position)!!
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.event_row_item, parent, false)

        // Populate the view with event data
        view.findViewById<TextView>(R.id.event_name).text = event.eventName
        view.findViewById<TextView>(R.id.event_location).text = event.eventLocation
        view.findViewById<TextView>(R.id.event_date).text = event.eventDate

        return view
    }
}