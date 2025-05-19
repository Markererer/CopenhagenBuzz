package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.MyApplication
import java.util.Calendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class EventViewModel : ViewModel() {

    private val _eventLiveData = MutableLiveData<List<Event>>(emptyList())
    val eventLiveData: LiveData<List<Event>> = _eventLiveData

    private val _favoritesLiveData = MutableLiveData<List<Event>>(emptyList())
    val favoritesLiveData: LiveData<List<Event>> = _favoritesLiveData

    init {
        loadEventsFromFirebase()
    }

    private fun loadEventsFromFirebase() {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                snapshot.children.forEach { child ->
                    child.getValue(Event::class.java)?.let { events.add(it) }
                }
                _eventLiveData.value = events
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: Handle errors
            }
        })
    }

    fun addEvent(event: Event) {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
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

    fun initializeSampleEvents() {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount == 0L) {
                    val testUserId = "test-uid-123"
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
                            userId = testUserId
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
                            userId = testUserId
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
                            userId = testUserId
                        )
                    )
                    sampleEvents.forEach { addEvent(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: Handle errors
            }
        })
    }
    fun addFavorite(event: Event) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favoriteRef = MyApplication.database
            .getReference("copenhagen_buzz/favorites/$userId/${event.id}")
        favoriteRef.setValue(event)
    }

    fun removeFavorite(event: Event) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favoriteRef = MyApplication.database
            .getReference("copenhagen_buzz/favorites/$userId/${event.id}")
        favoriteRef.removeValue()
    }
    fun loadFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = MyApplication.database.getReference("copenhagen_buzz/favorites/$userId")
        favRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favEvents = snapshot.children
                    .mapNotNull { it.getValue(Event::class.java) }
                _favoritesLiveData.value = favEvents
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}