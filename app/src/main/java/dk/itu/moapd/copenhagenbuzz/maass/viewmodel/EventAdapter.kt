package dk.itu.moapd.copenhagenbuzz.maass.view

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    options: FirebaseListOptions<Event>,
    private val favoriteIds: Set<String>,
    private val onFavoriteClick: (Event, Boolean) -> Unit,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : FirebaseListAdapter<Event>(options) {



    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun populateView(v: View, model: Event, position: Int) {
        v.findViewById<TextView>(R.id.event_name).text = model.eventName
        v.findViewById<TextView>(R.id.event_location).text = model.eventLocation
        v.findViewById<TextView>(R.id.event_date).text = dateFormatter.format(Date(model.eventDate))
        val favoriteIcon = v.findViewById<ImageView>(R.id.favoriteIcon)
        val isFavorite = favoriteIds.contains(model.id)
        favoriteIcon.setImageResource(
            if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_border
        )
        favoriteIcon.setOnClickListener {
            onFavoriteClick(model, isFavorite)
        }

        // Edit/Delete logic
        val editIcon = v.findViewById<ImageView>(R.id.editIcon)
        val deleteIcon = v.findViewById<ImageView>(R.id.deleteIcon)
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val isOwner = model.userId == currentUserId

        editIcon?.visibility = if (isOwner) View.VISIBLE else View.GONE
        deleteIcon?.visibility = if (isOwner) View.VISIBLE else View.GONE

        editIcon?.setOnClickListener {
            // Call a callback or start edit flow (implement this in your adapter constructor)
             onEditClick(model)
        }
        deleteIcon?.setOnClickListener {
            // Call a callback or start delete flow (implement this in your adapter constructor)
            onDeleteClick(model)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Use the default implementation to inflate the view
        return super.getView(position, convertView, parent)
    }
}