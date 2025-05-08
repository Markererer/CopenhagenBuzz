package dk.itu.moapd.copenhagenbuzz.maass.view

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
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

/**
 * Fragment for adding new events to the CopenhagenBuzz app.
 *
 * This fragment provides a form for users to input event details such as name, location, date, type, and description.
 * It uses [EventViewModel] to manage event data and [FragmentAddEventBinding] for View Binding.
 *
 * @property binding The View Binding object for accessing UI components.
 * @property viewModel The [EventViewModel] instance for managing event data.
 * @property eventTypes A list of predefined event types for the dropdown.
 */
class AddEventFragment : Fragment() {

    private lateinit var binding: FragmentAddEventBinding
    private val viewModel: EventViewModel by activityViewModels()
    private val eventTypes = arrayOf("Conference", "Concert", "Festival", "Workshop", "Seminar")

    /**
     * Inflates the fragment's layout and initializes the binding object.
     *
     * @param inflater The layout inflater to inflate the fragment's view.
     * @param container The parent view for the fragment's UI.
     * @param savedInstanceState If non-null, the fragment is re-initialized from a saved state.
     * @return The inflated view for the fragment.
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
     * Saves the event if all required fields are filled and navigates back to the previous fragment.
     * Displays a Snackbar error message if required fields are empty.
     */
    fun saveEvent() {
        val name = binding.editTextEventName.text.toString().trim()
        val location = binding.editTextEventLocation.text.toString().trim()
        val date = binding.editTextPickDate.text.toString().trim()
        val type = binding.eventType.text.toString().trim()
        val description = binding.editTextEventDescription.text.toString().trim()

        if (name.isNotEmpty() && location.isNotEmpty()) {
            viewModel.addEvent(Event(name, location, date, type, description))
            clearForm()
            findNavController().popBackStack() // Navigate back
        } else {
            Snackbar.make(requireView(), "Name and location are required!", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Clears all input fields in the form.
     */
    private fun clearForm() {
        binding.editTextEventName.text?.clear()
        binding.editTextEventLocation.text?.clear()
        binding.editTextPickDate.text?.clear()
        binding.eventType.text?.clear()
        binding.editTextEventDescription.text?.clear()
    }

    /**
     * Initializes UI components and sets up event listeners after the view is created.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState If non-null, the fragment is re-initialized from a saved state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize date picker
        binding.editTextPickDate.setOnClickListener {
            showDateRangePicker(binding.editTextPickDate)
        }

        // Set up the event type dropdown
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_dropdown_item_1line,
            eventTypes
        )
        binding.eventType.setAdapter(adapter)

        // Show dropdown when clicked
        binding.eventType.setOnClickListener {
            binding.eventType.showDropDown()
        }

        // Handle item selection
        binding.eventType.setOnItemClickListener { _, _, position, _ ->
            binding.eventType.setText(eventTypes[position])
        }
    }

    /**
     * Displays a date range picker dialog to allow users to select start and end dates for an event.
     *
     * @param editText The [EditText] where the selected date range will be displayed.
     */
    private fun showDateRangePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        DatePickerDialog(
            requireContext(),
            { _, startYear, startMonth, startDay ->
                val startDate = Calendar.getInstance().apply {
                    set(startYear, startMonth, startDay)
                }
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDay ->
                        val endDate = Calendar.getInstance().apply {
                            set(endYear, endMonth, endDay)
                        }
                        val formattedDate = "${dateFormat.format(startDate.time)} - ${dateFormat.format(endDate.time)}"
                        editText.setText(formattedDate)
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