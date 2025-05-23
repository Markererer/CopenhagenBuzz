package dk.itu.moapd.copenhagenbuzz.maass.view

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.maass.databinding.FragmentAddEventBinding
import dk.itu.moapd.copenhagenbuzz.maass.model.Event
import dk.itu.moapd.copenhagenbuzz.maass.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.maass.model.EventLocation
import android.location.Geocoder

/**
 * Fragment for adding or editing an event.
 * Handles user input, image selection (camera/gallery), geocoding, and event creation.
 */
class AddEventFragment : Fragment() {

    private lateinit var binding: FragmentAddEventBinding
    private val viewModel: EventViewModel by activityViewModels()
    private val eventTypes = arrayOf("Conference", "Concert", "Festival", "Workshop", "Seminar")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var imageUri: Uri? = null

    // Camera launcher for taking a photo and saving it to imageUri
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && imageUri != null) {
            binding.imageViewEventPhoto.setImageURI(imageUri)
        }
    }

    // Gallery picker launcher for selecting an image from the device
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imageViewEventPhoto.setImageURI(it)
        }
    }

    /**
     * Inflates the fragment layout and initializes view binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up UI event listeners and restores editing state if editing an event.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Date picker setup
        binding.editTextPickDate.isFocusable = false
        binding.editTextPickDate.isFocusableInTouchMode = false
        binding.editTextPickDate.setOnClickListener {
            showDateRangePicker(binding.editTextPickDate)
        }

        // Event type dropdown setup
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, eventTypes)
        binding.eventType.setAdapter(adapter)
        binding.eventType.setOnClickListener { binding.eventType.showDropDown() }

        // Take photo button
        binding.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
            }
        }

        // Choose from gallery button
        binding.btnChooseFromGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Restore editing event if any
        viewModel.editingEvent?.let { event ->
            binding.editTextEventName.setText(event.eventName)
            binding.editTextEventLocation.setText(event.eventLocation.address)
            binding.editTextPickDate.setText(dateFormat.format(event.eventDate))
            binding.eventType.setText(event.eventType, false)
            binding.editTextEventDescription.setText(event.eventDescription)
            if (event.photoUrl.isNotEmpty()) {
                // Optionally load image from URL (e.g., with Picasso)
            }
            viewModel.errorLiveData.observe(viewLifecycleOwner) { errorMsg ->
                if (!errorMsg.isNullOrEmpty()) {
                    Snackbar.make(requireView(), errorMsg, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Launches the camera to take a photo and save it to the device.
     */
    private fun launchCamera() {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "event_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        takePictureLauncher.launch(imageUri)
    }

    /**
     * Validates user input, geocodes the location, and triggers image upload and event saving.
     */
    fun saveEvent() {
        val name = binding.editTextEventName.text.toString().trim()
        val location = binding.editTextEventLocation.text.toString().trim()
        val dateText = binding.editTextPickDate.text.toString().trim()
        val type = binding.eventType.text.toString().trim()
        val description = binding.editTextEventDescription.text.toString().trim()

        if (name.isEmpty() || location.isEmpty() || dateText.isEmpty()) {
            Snackbar.make(requireView(), "Name, location, and date are required!", Snackbar.LENGTH_SHORT).show()
            return
        }
        if (imageUri == null) {
            Snackbar.make(requireView(), "Please add a photo for the event.", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Geocode the address to get latitude and longitude
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val results = geocoder.getFromLocationName(location, 1)
        val latLng = results?.firstOrNull()

        val eventLocation = EventLocation(
            latitude = latLng?.latitude ?: 0.0,
            longitude = latLng?.longitude ?: 0.0,
            address = location
        )
        // Parse date range: "dd/MM/yyyy - dd/MM/yyyy" -> use start date timestamp
        val startDateTimestamp = try {
            val parts = dateText.split(" - ")
            dateFormat.parse(parts[0])?.time ?: 0L
        } catch (e: Exception) {
            Log.e("AddEventFragment", "Error parsing date", e)
            0L
        }

        // Get current user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val editing = viewModel.editingEvent
        if (imageUri != null) {
            val event = Event(
                id = editing?.id ?: "",
                eventName = name,
                eventLocation = eventLocation,
                eventDate = startDateTimestamp,
                eventType = type,
                eventDescription = description,
                imageResId = 0,
                photoUrl = "", // Will be set after upload
                userId = userId
            )
            uploadImageAndSaveEvent(imageUri!!, event, editing != null)
        }
    }

    /**
     * Uploads the selected image to Firebase Storage and saves or updates the event in the database.
     *
     * @param imageUri The URI of the image to upload.
     * @param event The event object to save.
     * @param isEdit True if editing an existing event, false if creating a new one.
     */
    private fun uploadImageAndSaveEvent(
        imageUri: Uri,
        event: Event,
        isEdit: Boolean
    ) {
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
            .getReference("events/${System.currentTimeMillis()}.jpg")
        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnSuccessListener { uri ->
            event.photoUrl = uri.toString()
            if (isEdit) {
                viewModel.updateEvent(event)
                viewModel.editingEvent = null
            } else {
                viewModel.addEvent(event)
            }
            clearForm()
            findNavController().popBackStack()
        }.addOnFailureListener { e ->
            Snackbar.make(requireView(), "Image upload failed: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Clears all form fields and resets the image view.
     */
    private fun clearForm() {
        binding.editTextEventName.text?.clear()
        binding.editTextEventLocation.text?.clear()
        binding.editTextPickDate.text?.clear()
        binding.eventType.text?.clear()
        binding.editTextEventDescription.text?.clear()
        binding.imageViewEventPhoto.setImageDrawable(null)
        imageUri = null
    }

    /**
     * Shows a date range picker dialog and sets the selected range in the provided EditText.
     *
     * @param editText The EditText to set the selected date range.
     */
    private fun showDateRangePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, startYear, startMonth, startDay ->
                val startCal = Calendar.getInstance().apply { set(startYear, startMonth, startDay) }
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDay ->
                        val endCal = Calendar.getInstance().apply { set(endYear, endMonth, endDay) }
                        val formatted = "${dateFormat.format(startCal.time)} - ${dateFormat.format(endCal.time)}"
                        editText.setText(formatted)
                    },
                    startYear, startMonth, startDay
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * Handles the result of permission requests, specifically for the camera.
     *
     * @param requestCode The request code passed in requestPermissions.
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        }
    }
}