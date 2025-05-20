package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import android.util.Log
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
import dk.itu.moapd.copenhagenbuzz.maass.model.EventLocation

class EventViewModel : ViewModel() {

    private val _eventLiveData = MutableLiveData<List<Event>>(emptyList())
    val eventLiveData: LiveData<List<Event>> = _eventLiveData

    private val _favoritesLiveData = MutableLiveData<List<Event>>(emptyList())
    val favoritesLiveData: LiveData<List<Event>> = _favoritesLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    var editingEvent: Event? = null

    init {
        loadEventsFromFirebase()
    }

    private fun loadEventsFromFirebase() {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = mutableListOf<Event>()
                snapshot.children.forEach { child ->
                    try {
                        child.getValue(Event::class.java)?.let { events.add(it) }
                    } catch (e: Exception) {
                        Log.e("EventViewModel", "Error parsing event: ${e.message}", e)
                    }
                }
                Log.d("EventViewModel", "Loaded ${events.size} events from Firebase")
                _eventLiveData.value = events
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EventViewModel", "Failed to load events: ${error.message}")
                _errorLiveData.postValue("Failed to load events: ${error.message}")
            }
        })
    }

    fun addEvent(event: Event) {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
        val key = dbRef.push().key ?: run {
            Log.e("EventViewModel", "Failed to generate key for new event")
            return
        }
        event.id = key
        dbRef.child(key).setValue(event)
            .addOnSuccessListener {
                Log.d("EventViewModel", "Event added: $event")
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to add event: ${e.message}", e)
                _errorLiveData.postValue("Failed to add event: ${e.message}")
            }
    }

    fun initializeSampleEvents() {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("EventViewModel", "Events node children count: ${snapshot.childrenCount}")
                if (snapshot.childrenCount == 0L) {
                    Log.d("EventViewModel", "No events found, creating sample events")
                    val testUserId = "test-uid-123"
                    val sampleEvents = listOf(
                        Event(
                            id = "",
                            eventName = "Copenhagen Jazz Festival",
                            eventLocation = EventLocation(
                                latitude = 55.673906,
                                longitude = 12.568337,
                                address = "Tivoli Gardens"),
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
                            eventLocation = EventLocation(
                                latitude = 55.637963,
                                longitude = 12.576899,
                                address = "Bella Center"
                            ),
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
                            eventLocation = EventLocation(
                                latitude = 55.690140,
                                longitude = 12.579639,
                                address = "National Gallery"
                            ),
                            eventDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 3) }.timeInMillis,
                            eventType = "Workshop",
                            eventDescription = "Hands-on art creation session.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/art/600/400",
                            userId = testUserId
                        )
                    )
                    sampleEvents.forEach { addEvent(it) }
                } else {
                    Log.d("EventViewModel", "Events already exist, skipping sample creation")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EventViewModel", "Error reading events: ${error.message}")
                _errorLiveData.postValue("Error reading events: ${error.message}")
            }
        })
    }

    fun addFavorite(event: Event) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("EventViewModel", "No user logged in, cannot add favorite")
            return
        }
        val favoriteRef = MyApplication.database
            .getReference("copenhagen_buzz/favorites/$userId/${event.id}")
        favoriteRef.setValue(event)
            .addOnSuccessListener {
                Log.d("EventViewModel", "Favorite added for user $userId: ${event.id}")
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to add favorite: ${e.message}", e)
                _errorLiveData.postValue("Failed to add favorite: ${e.message}")
            }
    }

    fun removeFavorite(event: Event) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("EventViewModel", "No user logged in, cannot remove favorite")
            return
        }
        val favoriteRef = MyApplication.database
            .getReference("copenhagen_buzz/favorites/$userId/${event.id}")
        favoriteRef.removeValue()
            .addOnSuccessListener {
                Log.d("EventViewModel", "Favorite removed for user $userId: ${event.id}")
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to remove favorite: ${e.message}", e)
                _errorLiveData.postValue("Failed to remove favorite: ${e.message}")
            }
    }

    fun loadFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("EventViewModel", "No user logged in, cannot load favorites")
            return
        }
        val favRef = MyApplication.database.getReference("copenhagen_buzz/favorites/$userId")
        favRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favEvents = snapshot.children
                    .mapNotNull {
                        try {
                            it.getValue(Event::class.java)
                        } catch (e: Exception) {
                            Log.e("EventViewModel", "Error parsing favorite event: ${e.message}", e)
                            null
                        }
                    }
                Log.d("EventViewModel", "Loaded ${favEvents.size} favorites for user $userId")
                _favoritesLiveData.value = favEvents
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("EventViewModel", "Failed to load favorites: ${error.message}")
                _errorLiveData.postValue("Failed to load favorites: ${error.message}")
            }
        })
    }

    fun deleteEvent(event: Event) {
        MyApplication.database.getReference("copenhagen_buzz/events/${event.id}")
            .removeValue()
            .addOnSuccessListener {
                Log.d("EventViewModel", "Event deleted: ${event.id}")
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to delete event: ${e.message}", e)
                _errorLiveData.postValue("Failed to delete event: ${e.message}")
            }
        val favRef = MyApplication.database.getReference("copenhagen_buzz/favorites")
        favRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { userSnapshot ->
                val userId = userSnapshot.key ?: return@forEach
                MyApplication.database.getReference("copenhagen_buzz/favorites/$userId/${event.id}")
                    .removeValue()
                    .addOnSuccessListener {
                        Log.d("EventViewModel", "Removed event ${event.id} from user $userId favorites")
                    }
                    .addOnFailureListener { e ->
                        Log.e("EventViewModel", "Failed to remove favorite: ${e.message}", e)
                        _errorLiveData.postValue("Failed to remove favorite: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("EventViewModel", "Failed to access favorites: ${e.message}", e)
            _errorLiveData.postValue("Failed to access favorites: ${e.message}")
        }
    }

    fun updateEvent(event: Event) {
        MyApplication.database
            .getReference("copenhagen_buzz/events/${event.id}")
            .setValue(event)
            .addOnSuccessListener {
                Log.d("EventViewModel", "Event updated: ${event.id}")
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to update event: ${e.message}", e)
                _errorLiveData.postValue("Failed to update event: ${e.message}")
            }
    }
}