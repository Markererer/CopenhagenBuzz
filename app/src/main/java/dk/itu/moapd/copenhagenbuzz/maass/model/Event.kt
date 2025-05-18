package dk.itu.moapd.copenhagenbuzz.maass.model

/**
 * Represents an event with details such as name, location, date, type, description, and author.
 * Designed for use with Firebase Realtime Database; all fields have default values for no-arg constructor.
 *
 * @property id Unique identifier for the event (Firebase key).
 * @property eventName The name of the event.
 * @property eventLocation The location where the event takes place.
 * @property eventDate The date of the event as a timestamp (milliseconds since epoch).
 * @property eventType The type of the event (e.g., Conference, Concert).
 * @property eventDescription A description of the event.
 * @property imageResId Optional local image resource ID (fallback if photoUrl is empty).
 * @property photoUrl URL to the event photo (e.g., from Picsum).
 * @property userId The user ID of the event creator.
 */
data class Event(
    var id: String = "",
    var eventName: String = "",
    var eventLocation: String = "",
    var eventDate: Long = 0L,
    var eventType: String = "",
    var eventDescription: String = "",
    var imageResId: Int = 0,
    var photoUrl: String = "",
    var userId: String = "" // Renamed from eventAuthor
)