package dk.itu.moapd.copenhagenbuzz.maass.model

/**
 * Represents an event with details such as name, location, date, type, and description.
 *
 * This data class is used to hold information about an event in the CopenhagenBuzz app.
 *
 * @property eventName The name of the event.
 * @property eventLocation The location where the event takes place.
 * @property eventDate The date of the event.
 * @property eventType The type of the event (e.g., Conference, Concert).
 * @property eventDescription A description of the event.
 */
data class Event(
    var eventName: String = "",
    var eventLocation: String = "",
    var eventDate: String = "",
    var eventType: String = "",
    var eventDescription: String = "",
    val imageResId: Int,
    var eventAuthor: String = ""
)