// File: app/src/main/java/dk/itu/moapd/copenhagenbuzz/maass/model/EventLocation.kt
package dk.itu.moapd.copenhagenbuzz.maass.model

data class EventLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var address: String = ""
)