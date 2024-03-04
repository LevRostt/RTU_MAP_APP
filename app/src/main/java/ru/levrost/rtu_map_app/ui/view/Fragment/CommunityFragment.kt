package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentComunityBinding

class CommunityFragment : Fragment() {

    private var _bindind: FragmentComunityBinding? = null
    private val mBinding get() = _bindind!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindind = FragmentComunityBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindind = null
    }
}