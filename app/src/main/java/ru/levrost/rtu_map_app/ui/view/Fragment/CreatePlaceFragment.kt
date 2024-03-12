package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.CreatePlaceFragmentBinding
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.util.Arrays
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

        mBinding.placePic.setOnClickListener {

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

    private fun createPlace(){

        if (mBinding.nameField.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Заполните, пожалуйста, поле имени", Toast.LENGTH_LONG).show()
        } else if (placeListViewModel.selectedPlace() == null) {
            Toast.makeText(context, "Выберете, пожалуйста, место на карте", Toast.LENGTH_LONG)
                .show()
        } else {
            userViewModel.getUser().observe(viewLifecycleOwner) {

                placeListViewModel.addPlace(
                    mBinding.nameField.editText?.text.toString(),
                    Random(9999).nextInt().toString(), // edit random
                    it.name,
                    it.userId,
                    placeListViewModel.selectedPlace()!!.latitude,
                    placeListViewModel.selectedPlace()!!.longitude,
                    mBinding.descriptionField.editText?.text.toString() ?: "",
                    0,
                    false,
                    ""
                    )

            }

            findNavController().popBackStack()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}