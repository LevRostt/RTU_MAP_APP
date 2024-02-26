package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.levrost.rtu_map_app.databinding.LoginFragmentBinding

class LoginFragment: Fragment() {
    private lateinit var mBinding: LoginFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = LoginFragmentBinding.inflate(inflater, container, false)




        return mBinding.root
    }

}