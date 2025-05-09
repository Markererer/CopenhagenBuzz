package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event

class FavoritesAdapter(private val events: List<Event>, private var eventList: List<Event>) :

    RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarLetter: TextView = itemView.findViewById(R.id.avatarLetter)
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventType: TextView = itemView.findViewById(R.id.eventType)
        val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorite_row_item, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.eventName
        holder.eventType.text = event.eventType
        holder.avatarLetter.text = event.eventAuthor.first().uppercaseChar().toString()
        holder.eventImage.setImageResource(R.drawable.baseline_save_24)  // Use Glide for URLs
    }

    override fun getItemCount() = events.size

    fun updateData(newEvents: List<Event>) {
        eventList = newEvents
        notifyDataSetChanged()
    }

}
