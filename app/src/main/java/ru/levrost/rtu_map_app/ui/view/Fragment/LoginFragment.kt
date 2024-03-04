package ru.levrost.rtu_map_app.ui.view.Fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentLoginBinding


class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val mBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        mBinding.btnReg.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        mBinding.singInAsGuest.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }


        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}