package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event

/**
 * ViewModel for managing and providing event data to the UI components.
 *
 * This class holds a list of [Event] objects and exposes them via [eventLiveData] for observation.
 * It also manages a list of favorite events.
 */
class EventViewModel : ViewModel() {

    private val _eventLiveData = MutableLiveData<List<Event>>(emptyList())
    val eventLiveData: LiveData<List<Event>> = _eventLiveData


    private val _favoritesLiveData = MutableLiveData<List<Event>>(emptyList())
    val favoritesLiveData: LiveData<List<Event>> = _favoritesLiveData

    fun generateRandomFavorites() {
        val events = eventLiveData.value.orEmpty()
        val favorites = generateRandomFavoritesFromList(events)
        _favoritesLiveData.value = favorites
    }

    private fun generateRandomFavoritesFromList(events: List<Event>): List<Event> {
        val shuffledIndices = (events.indices).shuffled().take(25).sorted()
        return shuffledIndices.mapNotNull { index -> events.getOrNull(index) }
    }


    private val _favorites = MutableLiveData<List<Event>>(emptyList())
    val favorites: LiveData<List<Event>> = _favorites

    fun addEvent(event: Event) {
        val currentList = _eventLiveData.value?.toMutableList() ?: mutableListOf()
        currentList.add(event)
        _eventLiveData.value = currentList
    }

    fun initializeSampleEvents() {
        val locations = listOf("Bella Center", "Tivoli Gardens", "DR Koncerthuset", "Øksnehallen", "Cinemateket")
        val types = listOf("Conference", "Concert", "Workshop", "Networking", "Film")
        val authors = listOf("Maass", "Emma Jensen", "Lars Mikkelsen", "Nadia Rahman", "Jonas Poulsen")
        val descriptions = listOf(
            "An exciting opportunity to meet like-minded professionals.",
            "A night full of music, lights, and unforgettable vibes.",
            "Hands-on experience with industry experts.",
            "Meet, mingle, and grow your professional network.",
            "Screening of classic and contemporary films."
        )

        val sampleEvents = List(50) { index ->
            Event(
                eventName = "Copenhagen Buzz Event #$index",
                eventLocation = locations.random(),
                eventDate = "2025-0${(1..9).random()}-${(10..28).random()}",
                eventType = types.random(),
                eventDescription = descriptions.random(),
                imageResId = getRandomImageResId(),
                eventAuthor = authors.random()
            )
        }


        _eventLiveData.value = sampleEvents
        _favorites.value = generateRandomFavorites(sampleEvents)

    }
    private fun getRandomImageResId(): Int {
        val images = listOf(
            R.drawable.event1,
            R.drawable.event2,
            R.drawable.event3,
            R.drawable.event4,
            R.drawable.event5
        )
        return images.random()
    }



    private fun generateRandomFavorites(events: List<Event>): List<Event> {
        val shuffledIndices = events.indices.shuffled().take(25).sorted()
        return shuffledIndices.mapNotNull { events.getOrNull(it) }
    }
}
