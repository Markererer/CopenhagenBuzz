package dk.itu.moapd.copenhagenbuzz.maass.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying a list of events in a ListView.
 */
class EventAdapter(
    context: Context,
    private val events: List<Event>
) : ArrayAdapter<Event>(context, R.layout.event_row_item, events) {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val event = getItem(position)!!
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.event_row_item, parent, false)

        view.findViewById<TextView>(R.id.event_name).text = event.eventName
        view.findViewById<TextView>(R.id.event_location).text = event.eventLocation

        // Format timestamp to human-readable date
        val dateText = dateFormatter.format(Date(event.eventDate))
        view.findViewById<TextView>(R.id.event_date).text = dateText

        return view
    }
}
