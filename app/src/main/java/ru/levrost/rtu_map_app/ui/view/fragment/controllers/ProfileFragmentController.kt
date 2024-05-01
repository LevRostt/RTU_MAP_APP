package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentProfileBinding
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.service.NotificationService
import ru.levrost.rtu_map_app.ui.view.activity.MainActivity
import ru.levrost.rtu_map_app.ui.view.fragment.ProfileFragment


class ProfileFragmentController(val fragment: ProfileFragment, val binding: FragmentProfileBinding) {
    private var cardUserProfileId = "0"

    fun selectDrawProfileUi(isUser: Boolean?){
        if (isUser == true){
            binding.apply {
                subscribeBtn.visibility = View.GONE
                jumpBack.visibility = View.GONE
                exit.visibility = View.VISIBLE
                personName.text = fragment.userViewModel.getCachedUser()?.name
            }
        }
        else{
            cardUserProfileId = fragment.userViewModel.cardProfileUserData[1]

            binding.apply {
                if (fragment.userViewModel.getCachedUser()?.name == fragment.userViewModel.cardProfileUserData[0])
                    subscribeBtn.visibility = View.GONE
                else {
                    subscribeBtn.visibility = View.VISIBLE
                    if (fragment.userViewModel.getCachedUser()?.subUsers?.contains(cardUserProfileId) == true) {
                        binding.subscribeBtn.background = ContextCompat.getDrawable(fragment.requireContext(), R.drawable.secondary_button)
                        binding.subscribeBtn.text = ContextCompat.getString(fragment.requireContext(), R.string.unsubscribe)
                    }
                }

                jumpBack.visibility = View.VISIBLE
                exit.visibility = View.GONE
                personName.text = fragment.userViewModel.cardProfileUserData[0]
            }
        }
    }

    fun subscribe() {
        fragment.apply {
            (requireActivity() as MainActivity).checkAndRequestNotificationPermission(requireContext())
            userViewModel.userData.observeOnce(viewLifecycleOwner) {
                if (it.subUsers.contains(cardUserProfileId)) {
                    binding.subscribeBtn.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.main_button)
                    userViewModel.unscribe(cardUserProfileId)
                    binding.subscribeBtn.text =
                        ContextCompat.getString(requireContext(), R.string.subscribe)
                } else {
                    binding.subscribeBtn.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.secondary_button)
                    userViewModel.subscribe(cardUserProfileId)
                    binding.subscribeBtn.text =
                        ContextCompat.getString(requireContext(), R.string.unsubscribe)

                    val serviceIntent = Intent(context, NotificationService::class.java)
                    context?.startForegroundService(serviceIntent)
                }
            }
        }
    }


    fun delUser(){
        fragment.apply {
            userViewModel.deleteUser()
            (activity as MainActivity).navRestart()
        }
    }
}