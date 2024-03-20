package ru.levrost.rtu_map_app.ui.view.Fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.CreatePlaceFragmentBinding
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Base64
import kotlin.random.Random

class CreatePlaceFragment: Fragment() {
    private var _binding: CreatePlaceFragmentBinding? = null
    private val mBinding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    private val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreatePlaceFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (placeListViewModel.getLastUri() != null){
            mBinding.placePic.setImageURI(placeListViewModel.getLastUri())
        }

        mBinding.placePic.setOnClickListener {
            getContentFromGallery.launch("image/*")
        }

        mBinding.btnConfirm.setOnClickListener {
            createPlace()
        }

        mBinding.selectOnMap.setOnClickListener {
            findNavController().navigate(R.id.action_createPlaceFragment_to_mapSelectedFragment)
        }

        mBinding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private val getContentFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()){
        if (it != null){
            mBinding.placePic.setImageURI(it)
            placeListViewModel.setLastUriImage(it)
        }
    }


    private fun createPlace(){
        if (isInternetAvailable(requireContext())) {
            if (mBinding.nameField.editText?.text.toString().isEmpty()) {
                Toast.makeText(context, R.string.please_fill_name_field, Toast.LENGTH_LONG).show()
            } else if (placeListViewModel.selectedPlace() == null || placeListViewModel.selectedPlace()!!.latitude == 0.0) {
                Toast.makeText(context, R.string.pick_a_spot, Toast.LENGTH_LONG)
                    .show()
            } else {

                var pictureToSave = ""

                if (placeListViewModel.getLastUri() != null) {
                    pictureToSave = placeListViewModel.getLastUri().toString()
                }

                fun userObserver() = Observer<UserData>{
                    placeListViewModel.addPlace(
                        mBinding.nameField.editText?.text.toString(),
                        Random.nextInt(0,100000000).toString(),
                        it.name,
                        it.userId,
                        placeListViewModel.selectedPlace()!!.latitude,
                        placeListViewModel.selectedPlace()!!.longitude,
                        mBinding.nameField.editText?.text.toString(),
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

    override fun onDestroyView() {
        super.onDestroyView()
        placeListViewModel.selectPlace(0.0,0.0)
        _binding = null
    }
}