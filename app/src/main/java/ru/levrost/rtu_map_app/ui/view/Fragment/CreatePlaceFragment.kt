package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.levrost.rtu_map_app.databinding.CreatePlaceFragmentBinding

class CreatePlaceFragment: Fragment() {
    private var _binding: CreatePlaceFragmentBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreatePlaceFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}