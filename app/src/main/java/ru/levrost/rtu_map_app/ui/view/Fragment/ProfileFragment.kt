package ru.levrost.rtu_map_app.ui.view.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentProfileBinding
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.service.NotificationService
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class ProfileFragment: Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val mBinding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }
    private var cardUserProfileId = "0"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.getBoolean("isUser") == true){
            mBinding.apply {
                subscribeBtn.visibility = View.GONE
                jumpBack.visibility = View.GONE
                exit.visibility = View.VISIBLE
                personName.text = userViewModel.getCachedUser()?.name
            }
        }
        else{
            cardUserProfileId = userViewModel.cardProfileUserData[1]

            mBinding.apply {
                if (userViewModel.getCachedUser()?.name == userViewModel.cardProfileUserData[0])
                    subscribeBtn.visibility = View.GONE
                else {
                    subscribeBtn.visibility = View.VISIBLE
                    if (userViewModel.getCachedUser()?.subUsers?.contains(cardUserProfileId) == true) {

                    }
                }

                jumpBack.visibility = View.VISIBLE
                exit.visibility = View.GONE
                personName.text = userViewModel.cardProfileUserData[0]
            }
        }

        mBinding.apply {

            subscribeBtn.setOnClickListener {
                subscribe()
            }

            exit.setOnClickListener {
                userViewModel.deleteUser()
                (activity as MainActivity).navRestart()
            }

            jumpBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

    }

    private fun subscribe() {
        userViewModel.userData.observe(viewLifecycleOwner){
            if (it.subUsers.contains(cardUserProfileId)){
                mBinding.subscribeBtn.background = ContextCompat.getDrawable(requireContext(), R.drawable.main_button)
                userViewModel.unscribe(cardUserProfileId)
                mBinding.subscribeBtn.text = ContextCompat.getString(requireContext(), R.string.subscribe)
            }
            else{
                mBinding.subscribeBtn.background = ContextCompat.getDrawable(requireContext(), R.drawable.secondary_button)
                userViewModel.subscribe(cardUserProfileId)
                mBinding.subscribeBtn.text = ContextCompat.getString(requireContext(), R.string.unsubscribe)

                val serviceIntent = Intent(context, NotificationService::class.java)
                context?.startForegroundService(serviceIntent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}