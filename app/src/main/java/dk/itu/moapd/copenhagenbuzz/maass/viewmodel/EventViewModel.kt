package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import java.util.Calendar

/**
 * ViewModel for managing and providing event data to the UI components.
 * Loads events from Firebase and provides methods to add new events.
 */
class EventViewModel : ViewModel() {

    private val _eventLiveData = MutableLiveData<List<Event>>(emptyList())
    val eventLiveData: LiveData<List<Event>> = _eventLiveData

    private val _favoritesLiveData = MutableLiveData<List<Event>>(emptyList())
    val favoritesLiveData: LiveData<List<Event>> = _favoritesLiveData

    init {
        // Load events from Firebase when ViewModel is created
        loadEventsFromFirebase()
    }

    /**
     * Sets a realtime listener on the Firebase 'events' node.
     */
    private fun loadEventsFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance().getReference("copenhagen_buzz/events")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                snapshot.children.forEach { child ->
                    child.getValue(Event::class.java)?.let { events.add(it) }
                }
                _eventLiveData.value = events
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: Handle errors (e.g., log or show message)
            }
        })
    }

    /**
     * Adds a new event to the Firebase database.
     */
    fun addEvent(event: Event) {
        val dbRef = FirebaseDatabase.getInstance().getReference("copenhagen_buzz/events")
        val key = dbRef.push().key ?: return
        event.id = key
        dbRef.child(key).setValue(event)
            .addOnSuccessListener {
                // Listener will update LiveData automatically
            }
            .addOnFailureListener {
                // TODO: Handle failure
            }
    }

    /**
     * Initializes sample events in Firebase if the events node is empty.
     */
    fun initializeSampleEvents() {
        val dbRef = FirebaseDatabase.getInstance().getReference("copenhagen_buzz/events")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount == 0L) {
                    // Add sample events only if the node is empty
                    val sampleEvents = listOf(
                        Event(
                            id = "",
                            eventName = "Copenhagen Jazz Festival",
                            eventLocation = "Tivoli Gardens",
                            eventDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.timeInMillis,
                            eventType = "Festival",
                            eventDescription = "Annual jazz festival with live performances.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/jazz/600/400",
                            eventAuthor = ""
                        ),
                        Event(
                            id = "",
                            eventName = "Tech Conference 2025",
                            eventLocation = "Bella Center",
                            eventDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 10) }.timeInMillis,
                            eventType = "Conference",
                            eventDescription = "Latest trends in technology and innovation.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/tech/600/400",
                            eventAuthor = ""
                        ),
                        Event(
                            id = "",
                            eventName = "Art Workshop",
                            eventLocation = "National Gallery",
                            eventDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 3) }.timeInMillis,
                            eventType = "Workshop",
                            eventDescription = "Hands-on art creation session.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/art/600/400",
                            eventAuthor = ""
                        )
                    )
                    sampleEvents.forEach { addEvent(it) } // Use addEvent to save to Firebase
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: Handle errors (e.g., log or show message)
            }
        })
    }

    /**
     * Removed generateRandomFavorites as it's not needed for now.
     */
}