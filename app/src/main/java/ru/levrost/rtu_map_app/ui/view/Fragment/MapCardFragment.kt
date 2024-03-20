package ru.levrost.rtu_map_app.ui.view.Fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class MapCardFragment() : Fragment() {

    private var _bindind: MapPlaceCardBinding? = null
    private val mBinding get() = _bindind!!
    private lateinit var place : Place
    private lateinit var userData : UserData

    private val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    constructor(place : Place) : this() {
        this.place = place
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindind = MapPlaceCardBinding.inflate(inflater, container, false)
        userData = userViewModel.getCachedUser()!!
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnShowOnMap.visibility = View.GONE

        mBinding.apply {
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

    private fun loginRequest(){
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("Пожалуйста, залогинтесь, чтобы иметь возможность взаимодействовать с этой функциональностью")
            .setCancelable(false)
            .setPositiveButton("Залогиниться") { dialog, id -> // login
                requireActivity().getSharedPreferences("UID", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putString("id", "-1")
                    .apply()
                userViewModel.deleteUser()
                (requireActivity() as MainActivity).navRestart()
            }
            .setNegativeButton(
                "Отмена"
            ) { dialog, id -> // Закрываем диалоговое окно
                dialog.cancel()
            }
        alertDialogBuilder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindind = null
    }
}