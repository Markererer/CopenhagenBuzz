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

/**
 * ViewModel for managing event and favorite data.
 * Handles loading, adding, updating, and deleting events from Firebase.
 * Also manages user favorites and provides LiveData for UI observation.
 */
class EventViewModel : ViewModel() {

    // LiveData holding the list of all events.
    private val _eventLiveData = MutableLiveData<List<Event>>(emptyList())
    val eventLiveData: LiveData<List<Event>> = _eventLiveData

    // LiveData holding the list of favorite events for the current user.
    private val _favoritesLiveData = MutableLiveData<List<Event>>(emptyList())
    val favoritesLiveData: LiveData<List<Event>> = _favoritesLiveData

    // LiveData for error messages.
    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    // Holds the event currently being edited.
    var editingEvent: Event? = null

    /**
     * Initializes the ViewModel by loading events and removing duplicates.
     */
    init {
        loadEventsFromFirebase()
        removeDuplicateEventsByName()
    }

    /**
     * Loads events from Firebase and updates the eventLiveData.
     * Handles errors and logs the process.
     */
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

    /**
     * Adds a new event to Firebase.
     * Generates a unique key for the event and handles success or failure.
     *
     * @param event The event to add.
     */
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

    /**
     * Initializes the database with sample events if none exist.
     * Used for demonstration or testing purposes.
     */
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
                            eventLocation = EventLocation(55.6761, 12.5683, "Kongens Nytorv, 1050 København K"),
                            eventDate = System.currentTimeMillis() + 86400000L,
                            eventType = "Festival",
                            eventDescription = "Annual jazz festival in the city center.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/jazz/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Food Market",
                            eventLocation = EventLocation(55.6871, 12.5992, "Torvehallerne, Frederiksborggade 21, 1360 København K"),
                            eventDate = System.currentTimeMillis() + 2 * 86400000L,
                            eventType = "Market",
                            eventDescription = "Taste local and international food.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/food/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Art Exhibition",
                            eventLocation = EventLocation(55.6901, 12.5996, "Statens Museum for Kunst, Sølvgade 48-50, 1307 København K"),
                            eventDate = System.currentTimeMillis() + 3 * 86400000L,
                            eventType = "Exhibition",
                            eventDescription = "Modern art from Danish artists.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/art/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Tech Meetup",
                            eventLocation = EventLocation(55.6627, 12.5916, "IT-Universitetet, Rued Langgaards Vej 7, 2300 København S"),
                            eventDate = System.currentTimeMillis() + 4 * 86400000L,
                            eventType = "Meetup",
                            eventDescription = "Networking for tech enthusiasts.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/tech/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Opera Night",
                            eventLocation = EventLocation(55.6815, 12.6009, "Operaen, Ekvipagemestervej 10, 1438 København K"),
                            eventDate = System.currentTimeMillis() + 5 * 86400000L,
                            eventType = "Concert",
                            eventDescription = "Enjoy a night at the opera.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/opera/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Street Food Festival",
                            eventLocation = EventLocation(55.6929, 12.5991, "Reffen, Refshalevej 167A, 1432 København K"),
                            eventDate = System.currentTimeMillis() + 6 * 86400000L,
                            eventType = "Festival",
                            eventDescription = "Street food from around the world.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/streetfood/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Book Fair",
                            eventLocation = EventLocation(55.6759, 12.5655, "Rådhuspladsen, 1599 København V"),
                            eventDate = System.currentTimeMillis() + 7 * 86400000L,
                            eventType = "Fair",
                            eventDescription = "Meet authors and buy books.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/book/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Film Screening",
                            eventLocation = EventLocation(55.6731, 12.5683, "Grand Teatret, Mikkel Bryggers Gade 8, 1460 København K"),
                            eventDate = System.currentTimeMillis() + 8 * 86400000L,
                            eventType = "Screening",
                            eventDescription = "Classic films on the big screen.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/film/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Yoga in the Park",
                            eventLocation = EventLocation(55.6833, 12.5714, "Østre Anlæg, 2100 København Ø"),
                            eventDate = System.currentTimeMillis() + 9 * 86400000L,
                            eventType = "Wellness",
                            eventDescription = "Morning yoga session outdoors.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/yoga/600/400",
                            userId = testUserId
                        ),
                        Event(
                            id = "",
                            eventName = "Startup Pitch Night",
                            eventLocation = EventLocation(55.6761, 12.5683, "Founders House, Njalsgade 19D, 2300 København S"),
                            eventDate = System.currentTimeMillis() + 10 * 86400000L,
                            eventType = "Business",
                            eventDescription = "Startups pitch to investors.",
                            imageResId = 0,
                            photoUrl = "https://picsum.photos/seed/startup/600/400",
                            userId = testUserId
                        )
                    )
                    sampleEvents.forEach { event ->
                        val key = dbRef.push().key ?: return@forEach
                        event.id = key
                        dbRef.child(key).setValue(event)
                    }
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

    /**
     * Removes duplicate events from Firebase based on event name.
     * Keeps only the first occurrence of each event name.
     */
    fun removeDuplicateEventsByName() {
        val dbRef = MyApplication.database.getReference("copenhagen_buzz/events")
        dbRef.get().addOnSuccessListener { snapshot ->
            val seenNames = mutableSetOf<String>()
            val toDelete = mutableListOf<String>()
            snapshot.children.forEach { child ->
                val event = child.getValue(Event::class.java)
                if (event != null) {
                    if (seenNames.contains(event.eventName)) {
                        toDelete.add(event.id)
                    } else {
                        seenNames.add(event.eventName)
                    }
                }
            }
            toDelete.forEach { id ->
                dbRef.child(id).removeValue()
            }
        }
    }

    /**
     * Adds an event to the current user's favorites in Firebase.
     *
     * @param event The event to add as favorite.
     */
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

    /**
     * Removes an event from the current user's favorites in Firebase.
     *
     * @param event The event to remove from favorites.
     */
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

    /**
     * Loads the current user's favorite events from Firebase and updates favoritesLiveData.
     */
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

    /**
     * Deletes an event from Firebase and removes it from all users' favorites.
     *
     * @param event The event to delete.
     */
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

    /**
     * Updates an existing event in Firebase.
     *
     * @param event The event to update.
     */
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