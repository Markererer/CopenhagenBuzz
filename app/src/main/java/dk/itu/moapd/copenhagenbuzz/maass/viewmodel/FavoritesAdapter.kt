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

class FavoritesAdapter(options: FirebaseRecyclerOptions<Event>) :
    FirebaseRecyclerAdapter<Event, FavoritesAdapter.FavoriteViewHolder>(options) {

    class FavoriteViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val avatarLetter: TextView = itemView.findViewById(R.id.avatarLetter)
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventType: TextView = itemView.findViewById(R.id.eventType)
        val eventImage: ImageView = itemView.findViewById(R.id.eventImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_row_item, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int, model: Event) {
        if (position < 0 || position >= itemCount) {
            // Do not bind if position is invalid
            return
        }


        android.util.Log.d("FavoritesAdapter", "Binding event: ${model.eventName} (${model.id})")
        holder.eventName.text = model.eventName
        holder.eventType.text = model.eventType
        holder.avatarLetter.text = model.userId?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        if (model.photoUrl.isNotEmpty()) {
            Glide.with(holder.eventImage.context)
                .load(model.photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.placeholder_image)
            android.util.Log.d(
                "FavoritesAdapter",
                "Binding event: ${model.eventName} (${model.id})"
            )
            holder.eventName.text = model.eventName
            holder.eventType.text = model.eventType
            holder.avatarLetter.text =
                model.userId?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
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
}