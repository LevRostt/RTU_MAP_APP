package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.MapListFragmentBinding

class MapListFragment: Fragment() {
    private var _binding: MapListFragmentBinding? = null
    private val mBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapListFragmentBinding.inflate(inflater, container, false)

        mBinding.btnToAddPlace.setOnClickListener {
            findNavController().navigate(R.id.action_mapListFragment_to_createPlaceFragment)
        }

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}