package ru.levrost.rtu_map_app.ui.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.ui.view.activity.MainActivity
import ru.levrost.rtu_map_app.ui.view.fragment.MapListFragment
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class PlaceListRVAdapter(
    private val fragment : MapListFragment,
    private val userViewModel : UserViewModel,
    private val placeListViewModel: PlaceListViewModel
    ) : RecyclerView.Adapter<PlaceListRVAdapter.PlaceListHolder>() {


    private var placesList : MutableList<Place> = ArrayList()
    private var userData : UserData? = null

    init {
        userViewModel.getUser().observeOnce(fragment.viewLifecycleOwner){
            userData = it
        }
    }

    class PlaceListHolder(val binding : MapPlaceCardBinding) : RecyclerView.ViewHolder(binding.root){ }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PlaceListHolder {
        val binding = MapPlaceCardBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return PlaceListHolder(binding)
    }

    fun updateData(newList: List<Place>){
        val callback = PlaceDiffUtil(oldArray = placesList, newArray = newList,
            {old, new ->  old.idPlace == new.idPlace})
        placesList = newList.toMutableList()
        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun onBindViewHolder(holder: PlaceListHolder, position: Int) {

        val context = holder.itemView.context
        val place = placesList[position]

        holder.binding.apply {

            if (place.userName == ""){
                userName.text = "guest"
            }
            else{
                userName.text = place.userName
            }
            placeName.text = place.name
            placeInfo.text = place.description
            countOfLikes.text = place.likes.toString()

            //debugLog(" position = $position ; place = $place ")
            if (place.isPicSaved()){
                placePic.visibility = View.VISIBLE
                placePic.load(placeListViewModel.getUrl(place.image))
            }
            else{
                placePic.visibility = View.GONE
            }

            if (userData?.name == place.userName){
                btnDelete.visibility = View.VISIBLE
            }
            else{
                btnDelete.visibility = View.GONE
            }

            if (userData?.likes?.contains(place.idPlace) == true || place.isLiked){
                btnLike.setImageResource(R.drawable.favorite_icon_active)
            }
            else{
                btnLike.setImageResource(R.drawable.favorite_icon)
            }

            btnLike.setOnClickListener {

                val userId = userData?.userId

                if (isInternetAvailable(context)) {
                    if (userId == null || userId == "0" || userId == "-1") {
                        loginRequest()
                    } else {
                        if (!place.isLiked || !placesList[position].isLiked) {
                            placeListViewModel.likePlace(place.idPlace)
                            btnLike.setImageResource(R.drawable.favorite_icon_active)
                            countOfLikes.text =
                                (countOfLikes.text.toString().toInt() + 1).toString()
                            placesList[position].liked()
                        } else {
                            placeListViewModel.unLikePlace(place.idPlace)
                            btnLike.setImageResource(R.drawable.favorite_icon)
                            countOfLikes.text =
                                (countOfLikes.text.toString().toInt() - 1).toString()
                            placesList[position].unLiked()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Failed to send data. Check Internet access",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnDelete.setOnClickListener {
                val userId = userData?.userId

                if (isInternetAvailable(context)){
                    if (userId == null || userId == "0" || userId == "-1"){
                        loginRequest()
                    }
                    else{
                        placeListViewModel.deletePlace(place.idPlace)
                        if(placesList.size == 1){
                            updateData(emptyList())
                        }
                        placesList.remove(place)
                        notifyItemRemoved(position)
                        Toast.makeText(
                            context,
                            ContextCompat.getString(fragment.requireContext(), R.string.place_is_delete),
                            Toast.LENGTH_SHORT
                        ).show()
                        btnDelete.visibility = View.GONE // Иначе иконка багом сохраняется у первых карточек
                    }
                }

                else {
                    Toast.makeText(
                        context,
                        "Failed to send data. Check Internet access",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnShowOnMap.setOnClickListener {
                placeListViewModel.selectPlace(place.latitude,place.longitude)
                fragment.findNavController().popBackStack()
            }

            userName.setOnClickListener{
                userViewModel.setCardProfileUserData(userName.text.toString(), placesList[position].userId)
                fragment.findNavController().navigate(R.id.action_mapListFragment_to_profileFragment)
            }

            userIcon.setOnClickListener {
                userViewModel.setCardProfileUserData(userName.text.toString(), placesList[position].userId)
                fragment.findNavController().navigate(R.id.action_mapListFragment_to_profileFragment)
            }

        }


        holder.itemView.animation =
            AnimationUtils.loadAnimation(
                context,
                R.anim.fade_out
//                context.resources.getIdentifier("fade_out", "anim", context.packageName)
            )

    }

    private fun loginRequest(){
        val alertDialogBuilder = AlertDialog.Builder(fragment.context)
        alertDialogBuilder.setMessage(ContextCompat.getString(fragment.requireContext(), R.string.please_log_in))
            .setCancelable(false)
            .setPositiveButton(
                ContextCompat.getString(fragment.requireContext(), R.string.login)
            ) { dialog, id -> // login
                fragment.requireActivity().getSharedPreferences("UID", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putString("id", "-1")
                    .apply()
                userViewModel.deleteUser()
                (fragment.requireActivity() as MainActivity).navRestart()
            }
            .setNegativeButton(
                ContextCompat.getString(fragment.requireContext(), R.string.cancel)
            ) { dialog, id -> // Закрываем диалоговое окно
                dialog.cancel()
            }
        alertDialogBuilder.create().show()
    }
}