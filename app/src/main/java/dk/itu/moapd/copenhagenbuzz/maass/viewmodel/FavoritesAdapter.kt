package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import com.bumptech.glide.Glide

/**
 * Adapter for displaying a list of favorite events in a RecyclerView using Firebase.
 * Binds event data to the favorite row item layout, including event name, type, user avatar, and image.
 *
 * @constructor
 * @param options FirebaseRecyclerOptions for configuring the adapter with Event data.
 */
class FavoritesAdapter(options: FirebaseRecyclerOptions<Event>) :
    FirebaseRecyclerAdapter<Event, FavoritesAdapter.FavoriteViewHolder>(options) {

    /**
     * ViewHolder for favorite event items.
     *
     * @param itemView The view representing a single favorite event row.
     */
    class FavoriteViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        /** TextView displaying the first letter of the user ID as an avatar. */
        val avatarLetter: TextView = itemView.findViewById(R.id.avatarLetter)
        /** TextView displaying the event name. */
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        /** TextView displaying the event type. */
        val eventType: TextView = itemView.findViewById(R.id.eventType)
        /** ImageView displaying the event image. */
        val eventImage: ImageView = itemView.findViewById(R.id.eventImageView)
    }

    /**
     * Inflates the favorite row item layout and creates a FavoriteViewHolder.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new instance of FavoriteViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_row_item, parent, false)
        return FavoriteViewHolder(view)
    }

    /**
     * Binds the event data to the FavoriteViewHolder.
     *
     * @param holder The FavoriteViewHolder to bind data to.
     * @param position The position of the item in the adapter.
     * @param model The Event model containing event data.
     */
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int, model: Event) {
        if (position < 0 || position >= itemCount) return

        holder.eventName.text = model.eventName
        holder.eventType.text = model.eventType
        holder.avatarLetter.text = model.userId.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        if (model.photoUrl.isNotEmpty()) {
            Glide.with(holder.eventImage.context)
                .load(model.photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_image)
        }
    }
}