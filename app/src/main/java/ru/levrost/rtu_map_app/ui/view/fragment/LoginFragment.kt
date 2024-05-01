package ru.levrost.rtu_map_app.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentLoginBinding
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.LoginFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.util.concurrent.ScheduledExecutorService


class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val mBinding get() = _binding!!
    private var _controller: LoginFragmentController? = null
    private val controller get() = _controller!!

    val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        _controller = LoginFragmentController(this, mBinding)

        mBinding.btnReg.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        mBinding.btnLogin.setOnClickListener {
            controller.login()
        }

        mBinding.singInAsGuest.setOnClickListener {
            controller.loginAsGuest()
        }


        return mBinding.root
    }

    override fun onStop() {
        super.onStop()
        userViewModel.getUser().removeObservers(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _controller = null
    }

}