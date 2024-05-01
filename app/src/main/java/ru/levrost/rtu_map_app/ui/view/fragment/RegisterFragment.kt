package ru.levrost.rtu_map_app.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentRegisterBinding
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.RegisterFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RegisterFragment: Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val mBinding get() = _binding!!
    private var _controller: RegisterFragmentController? = null
    private val controller get() = _controller!!
    val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        _controller = RegisterFragmentController(this, mBinding)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnReg.setOnClickListener {
            controller.register()
        }
    }

    override fun onStop() {
        super.onStop()
        userViewModel.getUser().removeObservers(viewLifecycleOwner)
    }
    override fun onDestroyView() {
        _binding = null
        _controller = null
        super.onDestroyView()
    }

}