package dk.itu.moapd.copenhagenbuzz.maass.view

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
import com.bumptech.glide.Glide

/**
 * Adapter for displaying a list of events in a ListView using Firebase.
 * Handles favorite marking, editing, and deleting events, and displays event images.
 *
 * @property options FirebaseListOptions for configuring the adapter.
 * @property favoriteIds Set of event IDs marked as favorites.
 * @property onFavoriteClick Callback when the favorite icon is clicked.
 * @property onEditClick Callback when the edit icon is clicked.
 * @property onDeleteClick Callback when the delete icon is clicked.
 */
class EventAdapter(
    val options: FirebaseListOptions<Event>,
    private var favoriteIds: Set<String>,
    private val onFavoriteClick: (Event, Boolean) -> Unit,
    private val onEditClick: (Event) -> Unit,
    private val onDeleteClick: (Event) -> Unit
) : FirebaseListAdapter<Event>(options) {

    // Formatter for displaying event dates.
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Populates the view for each event item in the list.
     *
     * @param v The view to populate.
     * @param model The Event model for the current item.
     * @param position The position of the item in the list.
     */
    override fun populateView(v: View, model: Event, position: Int) {
        v.findViewById<TextView>(R.id.event_name).text = model.eventName
        v.findViewById<TextView>(R.id.event_location).text = model.eventLocation.address
        v.findViewById<TextView>(R.id.event_date).text = dateFormatter.format(Date(model.eventDate))
        val favoriteIcon = v.findViewById<ImageView>(R.id.favoriteIcon)
        val isFavorite = favoriteIds.contains(model.id)
        favoriteIcon.setImageResource(
            if (isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_border
        )
        favoriteIcon.setOnClickListener {
            onFavoriteClick(model, isFavorite)
        }
        val eventImageView = v.findViewById<ImageView>(R.id.eventImageView)
        if (model.photoUrl.isNotEmpty()) {
            Glide.with(v.context)
                .load(model.photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(eventImageView)
        } else {
            eventImageView.setImageResource(R.drawable.placeholder_image)
        }

        val editIcon = v.findViewById<ImageView>(R.id.editIcon)
        val deleteIcon = v.findViewById<ImageView>(R.id.deleteIcon)
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val isOwner = model.userId == currentUserId

        editIcon?.visibility = if (isOwner) View.VISIBLE else View.GONE
        deleteIcon?.visibility = if (isOwner) View.VISIBLE else View.GONE

        editIcon?.setOnClickListener {
            onEditClick(model)
        }
        deleteIcon?.setOnClickListener {
            onDeleteClick(model)
        }
    }

    /**
     * Returns the view for the specified position, handling out-of-bounds cases.
     *
     * @param position The position of the item.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent view group.
     * @return The view for the specified position.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (position < 0 || position >= count) {
            // Return an empty view to avoid crash
            return View(parent.context)
        }
        return super.getView(position, convertView, parent)
    }

    /**
     * Updates the set of favorite event IDs and refreshes the list.
     *
     * @param newFavoriteIds The new set of favorite event IDs.
     */
    fun updateFavoriteIds(newFavoriteIds: Set<String>) {
        favoriteIds = newFavoriteIds
        notifyDataSetChanged()
    }

    /**
     * Returns the adapter position for the given event.
     *
     * @param event The event to find.
     * @return The position of the event, or -1 if not found.
     */
    fun getPositionForEvent(event: Event): Int {
        for (i in 0 until count) {
            val adapterEvent = getItem(i)
            if (adapterEvent?.id == event.id) return i
        }
        return -1
    }
}