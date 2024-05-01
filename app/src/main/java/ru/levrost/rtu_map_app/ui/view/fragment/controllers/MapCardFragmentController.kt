package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import coil.load
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.FragmentLoginBinding
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.ui.view.activity.MainActivity
import ru.levrost.rtu_map_app.ui.view.fragment.LoginFragment
import ru.levrost.rtu_map_app.ui.view.fragment.MapCardFragment
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MapCardFragmentController(val fragment : MapCardFragment, val binding: MapPlaceCardBinding) {
    private lateinit var userData : UserData


    fun draw() {

        fragment.apply {
            binding.apply {
                btnShowOnMap.visibility = View.GONE

                if (place.userName == "") {
                    userName.text = "guest"
                } else {
                    userName.text = place.userName
                }
                placeName.text = place.name
                placeInfo.text = place.description
                countOfLikes.text = place.likes.toString()

                if (place.isPicSaved()) {
                    placePic.visibility = View.VISIBLE
                    placePic.load(placeListViewModel.getUrl(place.image))
                } else {
                    placePic.visibility = View.GONE
                }

                if (userData.likes.contains(place.idPlace) || place.isLiked) {
                    btnLike.setImageResource(R.drawable.favorite_icon_active)
                } else {
                    btnLike.setImageResource(R.drawable.favorite_icon)
                }

                btnLike.setOnClickListener {

                    val userId = userData.userId

                    if (isInternetAvailable(requireContext())) {
                        if (userId == null || userId == "0" || userId == "-1") {
                            loginRequest()
                        } else {
                            if (!place.isLiked || place.isLiked) {
                                placeListViewModel.likePlace(place.idPlace)
                                btnLike.setImageResource(R.drawable.favorite_icon_active)
                                countOfLikes.text =
                                    (countOfLikes.text.toString().toInt() + 1).toString()
                            } else {
                                placeListViewModel.unLikePlace(place.idPlace)
                                btnLike.setImageResource(R.drawable.favorite_icon)
                                countOfLikes.text =
                                    (countOfLikes.text.toString().toInt() - 1).toString()
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            ContextCompat.getString(requireContext(), R.string.fail_internet),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                userName.setOnClickListener {
                    userViewModel.setCardProfileUserData(
                        userName.text.toString(),
                        place.userId
                    )
                    findNavController()
                        .navigate(R.id.action_mapFragment_to_profileFragment2)
                }

                userIcon.setOnClickListener {
                    userViewModel.setCardProfileUserData(
                        userName.text.toString(),
                        place.userId
                    )
                    findNavController()
                        .navigate(R.id.action_mapFragment_to_profileFragment2)
                }
            }
        }
    }

    private fun loginRequest() {
        fragment.apply {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setMessage(
                ContextCompat.getString(
                    requireContext(),
                    R.string.please_log_in
                )
            )
                .setCancelable(false)
                .setPositiveButton(
                    ContextCompat.getString(
                        requireContext(),
                        R.string.login
                    )
                ) { dialog, id -> // login
                    requireActivity().getSharedPreferences("UID", AppCompatActivity.MODE_PRIVATE)
                        .edit()
                        .putString("id", "-1")
                        .apply()
                    userViewModel.deleteUser()
                    (requireActivity() as MainActivity).navRestart()
                }
                .setNegativeButton(
                    ContextCompat.getString(requireContext(), R.string.cancel)
                ) { dialog, id -> // Закрываем диалоговое окно
                    dialog.cancel()
                }
            alertDialogBuilder.create().show()
        }
    }
}