package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.CreatePlaceFragmentBinding
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.ui.view.fragment.CreatePlaceFragment
import kotlin.random.Random

class CreatePlaceFragmentController(val fragment : CreatePlaceFragment, val binding: CreatePlaceFragmentBinding) {


    val getContentFromGallery =
        fragment.registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                binding.placePic.setImageURI(it)
                fragment.placeListViewModel.setLastUriImage(it)
            }
        }


    fun createPlace(){
        fragment.apply {
            if (isInternetAvailable(requireContext())) {
                if (binding.nameField.editText?.text.toString().isEmpty()) {
                    Toast.makeText(context, R.string.please_fill_name_field, Toast.LENGTH_LONG)
                        .show()
                } else if (placeListViewModel.selectedPlace() == null || placeListViewModel.selectedPlace()!!.latitude == 0.0) {
                    Toast.makeText(context, R.string.pick_a_spot, Toast.LENGTH_LONG)
                        .show()
                } else {

                    var pictureToSave = ""

                    if (placeListViewModel.getLastUri() != null) {
                        pictureToSave = placeListViewModel.getLastUri().toString()
                    }

                    fun userObserver() = Observer<UserData> {
                        placeListViewModel.addPlace(
                            binding.nameField.editText?.text.toString(),
                            Random.nextInt(0, 100000000).toString(),
                            it.name,
                            it.userId,
                            placeListViewModel.selectedPlace()!!.latitude,
                            placeListViewModel.selectedPlace()!!.longitude,
                            binding.nameField.editText?.text.toString(),
                            0,
                            false,
                            pictureToSave
                        )
                        userViewModel.getUser().removeObservers(viewLifecycleOwner)
                        findNavController().popBackStack()
                    }

                    userViewModel.getUser().observe(viewLifecycleOwner, userObserver())

                }
            } else {
                Toast.makeText(
                    context,
                    "Failed to send data. Check Internet access",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}