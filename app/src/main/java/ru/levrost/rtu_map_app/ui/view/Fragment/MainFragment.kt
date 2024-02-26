package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.levrost.rtu_map_app.databinding.MainFragmentBinding

class MainFragment: Fragment() {
    private lateinit var mBinding: MainFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = MainFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }
}