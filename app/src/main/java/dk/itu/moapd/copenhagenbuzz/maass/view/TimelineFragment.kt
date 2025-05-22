package dk.itu.moapd.copenhagenbuzz.maass.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.Query
import dk.itu.moapd.copenhagenbuzz.maass.MyApplication
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel
import androidx.navigation.fragment.findNavController
import kotlin.math.sqrt

class TimelineFragment : Fragment() {

    private var eventAdapter: EventAdapter? = null
    private lateinit var viewModel: EventViewModel
    private lateinit var eventListView: ListView

    private lateinit var eventAdapterOptions: FirebaseListOptions<Event>

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeTimestamp: Long = 0
    private var userLocation: Location? = null

    override fun onDestroyView() {
        super.onDestroyView()
        eventAdapter?.stopListening()
        eventListView.adapter = null
        eventAdapter = null
        sensorManager.unregisterListener(shakeListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timeline, container, false)
        eventListView = view.findViewById(R.id.event_list_view)

        viewModel = ViewModelProvider(requireActivity())[EventViewModel::class.java]
        viewModel.loadFavorites()

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        fetchUserLocation()

        val query: Query = MyApplication.database
            .getReference("copenhagen_buzz/events")
            .orderByChild("eventDate")
        eventAdapterOptions = FirebaseListOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .setLayout(R.layout.event_row_item)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        // Only create the adapter once, when events are loaded
        viewModel.eventLiveData.observe(viewLifecycleOwner) {
            if (eventAdapter == null) {
                val favoriteIds = viewModel.favoritesLiveData.value?.map { it.id }?.toSet() ?: emptySet()
                eventAdapter = EventAdapter(
                    eventAdapterOptions,
                    favoriteIds,
                    onFavoriteClick = { event, isFavorite ->
                        if (isFavorite) viewModel.removeFavorite(event)
                        else viewModel.addFavorite(event)
                    },
                    onEditClick = { event ->
                        viewModel.editingEvent = event
                        findNavController().navigate(R.id.addEventFragment)
                    },
                    onDeleteClick = { event ->
                        viewModel.deleteEvent(event)
                    }
                )
                eventListView.adapter = eventAdapter
            }
        }

        // Just update favorite IDs, do not recreate the adapter
        viewModel.favoritesLiveData.observe(viewLifecycleOwner) { favoriteEvents ->
            val favoriteIds = favoriteEvents.map { it.id }.toSet()
            eventAdapter?.updateFavoriteIds(favoriteIds)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(shakeListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeListener)
    }

    private fun scrollToClosestEvent() {
        val events = viewModel.eventLiveData.value ?: return
        val loc = userLocation ?: return
        if (events.isEmpty()) return

        var minDist = Float.MAX_VALUE
        var minIdx = 0
        var closestEvent: Event? = null
        events.forEachIndexed { idx, event ->
            val eLoc = event.eventLocation
            val results = FloatArray(1)
            Location.distanceBetween(
                loc.latitude, loc.longitude,
                eLoc.latitude, eLoc.longitude,
                results
            )
            if (results[0] < minDist) {
                minDist = results[0]
                minIdx = idx
                closestEvent = event
            }
        }
        eventListView.smoothScrollToPosition(minIdx)
        closestEvent?.let { event ->
            val distanceStr = if (minDist >= 1000) {
                String.format("%.1f km", minDist / 1000)
            } else {
                String.format("%.0f m", minDist)
            }
            view?.let {
                Snackbar.make(
                    it,
                    "The closest event is $distanceStr away - ${event.eventName}.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
            if (gForce > 2.7) {
                val now = System.currentTimeMillis()
                if (now - shakeTimestamp > 1000) {
                    shakeTimestamp = now
                    scrollToClosestEvent()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            userLocation = location
        }
    }
}