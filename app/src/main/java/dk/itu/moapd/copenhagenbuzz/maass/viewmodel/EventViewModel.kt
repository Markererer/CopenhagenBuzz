package dk.itu.moapd.copenhagenbuzz.maass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dk.itu.moapd.copenhagenbuzz.maass.model.Event

/**
 * ViewModel for managing and providing event data to the UI components.
 *
 * This class holds a list of [Event] objects and exposes them via [eventLiveData] for observation.
 * It provides methods to add new events to the list, ensuring the UI stays updated with the latest data.
 *
 * @property eventLiveData A [LiveData] list of events that UI components can observe.
 * @constructor Creates an instance of [EventViewModel].
 */
class EventViewModel : ViewModel() {

    /**
     * Backing property for [eventLiveData] to hold the mutable list of events.
     * This is private to ensure data encapsulation.
     */
    private val _eventLiveData = MutableLiveData<List<Event>>(emptyList())

    /**
     * Exposes the list of events as immutable [LiveData] for UI observation.
     * UI components should observe this property to receive updates when events change.
     */
    val eventLiveData: LiveData<List<Event>> = _eventLiveData

    /**
     * Adds a new event to the list and updates [eventLiveData].
     *
     * @param event The [Event] object to add to the list.
     */
    fun addEvent(event: Event) {
        val currentList = _eventLiveData.value?.toMutableList() ?: mutableListOf()
        currentList.add(event)
        _eventLiveData.value = currentList
    }
}