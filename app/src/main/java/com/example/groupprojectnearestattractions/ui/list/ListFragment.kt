package com.example.groupprojectnearestattractions.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.location.Location
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.example.groupprojectnearestattractions.PlacesListAdapter
import com.example.groupprojectnearestattractions.PlacesManager
import com.example.groupprojectnearestattractions.databinding.FragmentListBinding
import com.example.groupprojectnearestattractions.ui.FilterDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ListFragment : Fragment(), FilterDialogFragment.FilterDialogListener {

    private var lastKnownLocation: Location? = null
    private lateinit var placesManager: PlacesManager
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = binding.placesList
        val recyclerViewManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = recyclerViewManager
        recyclerView.visibility = View.GONE
        placesManager = PlacesManager(requireContext())
        binding.button.setOnClickListener {
            FilterDialogFragment(this).show(requireActivity().supportFragmentManager, "filters")
        }
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("MissingPermission")
    private fun getPlaces(categories: List<String>? = null) {
        try {
            recyclerView.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            placesManager.getDeviceLocation().addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
                            val places = PlacesManager(requireContext())
                                .fetchPlaces(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude,
                                    2,
                                    categories
                                )
                            places?.let {
                                val placesListAdapter = PlacesListAdapter(it)
                                recyclerView.adapter = placesListAdapter
                                recyclerView.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, categories: List<String>) {
        getPlaces()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }
}