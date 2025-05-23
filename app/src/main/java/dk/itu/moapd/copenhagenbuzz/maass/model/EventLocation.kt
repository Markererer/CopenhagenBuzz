// File: app/src/main/java/dk/itu/moapd/copenhagenbuzz/maass/model/EventLocation.kt
package dk.itu.moapd.copenhagenbuzz.maass.model

/**
 * Data class representing a geographic location for an event.
 *
 * @property latitude The latitude of the event location.
 * @property longitude The longitude of the event location.
 * @property address The human-readable address of the event location.
 */
data class EventLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var address: String = ""
)