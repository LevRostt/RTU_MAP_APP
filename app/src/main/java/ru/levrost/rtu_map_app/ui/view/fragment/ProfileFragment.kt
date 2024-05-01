package ru.levrost.rtu_map_app.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentProfileBinding
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.service.NotificationService
import ru.levrost.rtu_map_app.ui.view.activity.MainActivity
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.ProfileFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class ProfileFragment: Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val mBinding get() = _binding!!
    private var _controller: ProfileFragmentController? = null
    private val controller get() = _controller!!

    val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        _controller = ProfileFragmentController(this, mBinding)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller.selectDrawProfileUi(arguments?.getBoolean("isUser"))

        mBinding.apply {

            subscribeBtn.setOnClickListener {
                controller.subscribe()
            }

            exit.setOnClickListener {
                controller.delUser()
            }

            jumpBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _controller = null
    }

}