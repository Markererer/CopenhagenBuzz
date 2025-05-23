package dk.itu.moapd.copenhagenbuzz.maass.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import dk.itu.moapd.copenhagenbuzz.maass.R

/**
 * Foreground service that collects location updates and broadcasts them.
 * Uses FusedLocationProviderClient to request high-accuracy location updates
 * and sends the latest location via a broadcast intent.
 */
class LocationService : Service() {

    // Client for accessing location services.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Callback for receiving location updates.
    private lateinit var locationCallback: LocationCallback

    /**
     * Called when the service is created.
     * Initializes the location client, notification channel, and starts the service in the foreground.
     */
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(1, createNotification())

        // Define the callback to handle location results.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location? = result.lastLocation
                location?.let {
                    val intent = Intent(ACTION_LOCATION_BROADCAST)
                    intent.putExtra(EXTRA_LOCATION, it)
                    sendBroadcast(intent)
                }
            }
        }
    }

    /**
     * Called when the service is started.
     * Requests location updates with high accuracy and a specified interval.
     *
     * @param intent The Intent supplied to startService.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The mode in which to continue running the service.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
        return START_STICKY
    }

    /**
     * Called when the service is destroyed.
     * Removes location updates to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Not used as this is a started service, not a bound service.
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Always returns null.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Creates a notification for running the service in the foreground.
     *
     * @return The notification to display.
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Collecting location data...")
            .setSmallIcon(R.drawable.ic_location)
            .build()
    }

    /**
     * Creates a notification channel for the foreground service if required (API 26+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        /** Notification channel ID for the foreground service. */
        const val CHANNEL_ID = "location_service_channel"
        /** Action string for broadcasting location updates. */
        const val ACTION_LOCATION_BROADCAST = "dk.itu.moapd.copenhagenbuzz.maass.LOCATION_BROADCAST"
        /** Extra key for passing location data in the broadcast. */
        const val EXTRA_LOCATION = "extra_location"
    }
}