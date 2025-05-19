package dk.itu.moapd.copenhagenbuzz.maass.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
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
import kotlin.text.format


class AddEventFragment : Fragment() {

    private lateinit var binding: FragmentAddEventBinding
    private val viewModel: EventViewModel by activityViewModels()
    private val eventTypes = arrayOf("Conference", "Concert", "Festival", "Workshop", "Seminar")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        // Generate a mock photo URL (Picsum)
        val photoUrl = "https://picsum.photos/seed/${System.currentTimeMillis()}/600/400"

        val editing = viewModel.editingEvent
        if (editing != null) {
            val updatedEvent = editing.copy(
                eventName = name,
                eventLocation = location,
                eventDate = startDateTimestamp,
                eventType = type,
                eventDescription = description,
                photoUrl = editing.photoUrl
            )
            viewModel.updateEvent(updatedEvent)
            viewModel.editingEvent = null
        } else {
            // Add new event
            val event = Event(
                id = "",
                eventName = name,
                eventLocation = location,
                eventDate = startDateTimestamp,
                eventType = type,
                eventDescription = description,
                imageResId = 0,
                photoUrl = photoUrl,
                userId = userId
            )
            viewModel.addEvent(event)
        }
        clearForm()
        findNavController().popBackStack()
    }

    private fun clearForm() {
        binding.editTextEventName.text?.clear()
        binding.editTextEventLocation.text?.clear()
        binding.editTextPickDate.text?.clear()
        binding.eventType.text?.clear()
        binding.editTextEventDescription.text?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make editTextPickDate non-focusable to avoid focus click
        binding.editTextPickDate.isFocusable = false
        binding.editTextPickDate.isFocusableInTouchMode = false

        // Date picker
        binding.editTextPickDate.setOnClickListener {
            showDateRangePicker(binding.editTextPickDate)
        }

        // Event type dropdown
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, eventTypes)
        binding.eventType.setAdapter(adapter)
        binding.eventType.setOnClickListener { binding.eventType.showDropDown() }

        viewModel.editingEvent?.let { event ->
            binding.editTextEventName.setText(event.eventName)
            binding.editTextEventLocation.setText(event.eventLocation)
            binding.editTextPickDate.setText(dateFormat.format(event.eventDate))
            binding.eventType.setText(event.eventType, false)
            binding.editTextEventDescription.setText(event.eventDescription)
            viewModel.errorLiveData.observe(viewLifecycleOwner) { errorMsg ->
                if (!errorMsg.isNullOrEmpty()) {
                    Snackbar.make(requireView(), errorMsg, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

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
}