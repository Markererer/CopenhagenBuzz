package dk.itu.moapd.copenhagenbuzz.maass.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel

/**
 * Fragment that displays a Google Map with event markers.
 * Observes event data from the ViewModel and places markers for each event location.
 * Handles user location display and camera movement.
 */
class MapsFragment : Fragment(), OnMapReadyCallback {

    // MapView instance for displaying the map.
    private lateinit var mapView: MapView
    // Reference to the GoogleMap object.
    private var googleMap: GoogleMap? = null
    // Shared ViewModel for accessing event data.
    private val viewModel: EventViewModel by activityViewModels()

    /**
     * Inflates the fragment layout and initializes the MapView.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        return view
    }

    /**
     * Called when the GoogleMap is ready to be used.
     * Enables user location, observes events, and sets up marker click handling.
     *
     * @param map The GoogleMap instance.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()
        observeEvents()
        googleMap?.setOnMarkerClickListener { marker ->
            val event = marker.tag as? Event
            event?.let {
                Toast.makeText(requireContext(), it.eventName, Toast.LENGTH_SHORT).show()
                // Optionally, show a dialog or navigate to event details
            }
            true
        }
        moveCameraToUserLocation()
    }

    /**
     * Observes the event list from the ViewModel and adds markers for each event on the map.
     */
    private fun observeEvents() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { events ->
            googleMap?.clear()
            for (event in events) {
                val loc = event.eventLocation
                if (loc.latitude != 0.0 || loc.longitude != 0.0) {
                    val position = LatLng(loc.latitude, loc.longitude)
                    val marker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(event.eventName)
                            .snippet(loc.address)
                    )
                    marker?.tag = event
                }
            }
        }
    }

    /**
     * Enables the user's current location on the map if permission is granted.
     */
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    /**
     * Moves the camera to the user's current location if permission is granted.
     */
    private fun moveCameraToUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
            }
        }
    }

    /**
     * Called immediately after onCreateView.
     * Handles window insets for proper map padding.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnApplyWindowInsetsListener { v, insets ->
            val bottomInset = insets.systemWindowInsetBottom
            mapView.setPadding(0, 0, 0, bottomInset)
            insets
        }
    }

    // MapView lifecycle methods

    /**
     * Forwards the onResume event to the MapView.
     */
    override fun onResume() { super.onResume(); mapView.onResume() }

    /**
     * Forwards the onPause event to the MapView.
     */
    override fun onPause() { super.onPause(); mapView.onPause() }

    /**
     * Forwards the onDestroy event to the MapView.
     */
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }

    /**
     * Forwards the onLowMemory event to the MapView.
     */
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }

    /**
     * Forwards the onSaveInstanceState event to the MapView.
     *
     * @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}