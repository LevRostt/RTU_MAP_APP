package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.MapListFragmentBinding

class MapListFragment: Fragment() {
    private lateinit var mBinding: MapListFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = MapListFragmentBinding.inflate(inflater, container, false)

        return mBinding.root
    }
}