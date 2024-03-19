package ru.levrost.rtu_map_app.ui.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity
import ru.levrost.rtu_map_app.ui.view.Fragment.MapListFragment
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.lang.NumberFormatException
import java.util.Base64

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

    class PlaceListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var _binding : MapPlaceCardBinding
        val binding get() = _binding

        init {
            _binding = MapPlaceCardBinding.bind(itemView)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceListHolder {
        return PlaceListHolder(
            MapPlaceCardBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
                .root
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Place>){

        placesList = newList.toMutableList()
        notifyDataSetChanged()

//        if (placesList.isEmpty() && newList.isNotEmpty()){
//            placesList = newList.toMutableList()
//            notifyDataSetChanged()
//        }
//        else
//            for (i in newList.indices) {
//                if (i > placesList.size - 1) {
//                    placesList.add(newList[i])
//                    notifyItemInserted(i)
//                } else if (newList[i].idPlace != placesList[i].idPlace) {
//                    placesList[i] = newList[i]
//                    notifyItemChanged(i)
//                }
//            }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun onBindViewHolder(holder: PlaceListHolder, position: Int) {

        val context = holder.itemView.context
        val place = placesList[position]
        val binding = holder.binding

        binding.apply {

            if (place.userName == ""){
                userName.text = "guest"
            }
            else{
                userName.text = place.userName
            }
            placeName.text = place.name
            placeInfo.text = place.description
            countOfLikes.text = place.likes.toString()

            if (place.isPicSaved()){
//                val pic : ByteArray = Base64.getDecoder().decode(place.image)
//
//                placePic.setImageBitmap(
//                    BitmapFactory.decodeByteArray(
//                        pic,
//                        0,
//                        pic.size
//                    )
//                )

                placePic.setImageURI(Uri.parse(place.image))
            }
            else{
                placePic.setImageResource(R.drawable.empty_pic)
            }

            if (userData?.name == place.userName){
                btnDelete.visibility = View.VISIBLE
            }

            if (userData?.likes?.contains(place.idPlace) == true || place.isLiked){
                btnLike.setImageResource(R.drawable.favorite_icon_active)
            }

            btnLike.setOnClickListener {

                val userId = userData?.userId

                if (isInternetAvailable(context)) {
                    if (userId == null || userId == "0" || userId == "-1") {
                        loginRequest()
                    } else {
                        if (!place.isLiked || !placesList[position].isLiked) {
                            //userViewModel.likePlace(place.idPlace)
                            placeListViewModel.likePlace(place.idPlace)
                            binding.btnLike.setImageResource(R.drawable.favorite_icon_active)
                            binding.countOfLikes.text =
                                (binding.countOfLikes.text.toString().toInt() + 1).toString()
                            placesList[position].liked()
                        } else {
                            //userViewModel.unLikePlace(place.idPlace)
                            placeListViewModel.unLikePlace(place.idPlace)
                            binding.btnLike.setImageResource(R.drawable.favorite_icon)
                            binding.countOfLikes.text =
                                (binding.countOfLikes.text.toString().toInt() - 1).toString()
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
                        notifyItemRemoved(position)
                        Toast.makeText(
                            context,
                            "Место удалено",
                            Toast.LENGTH_SHORT
                        ).show()

                        binding.btnDelete.visibility = View.GONE // Иначе иконка багом сохраняется у первых карточек
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
                userViewModel.setCardProfileUserData(binding.userName.text.toString(), placesList[position].userId)
                fragment.findNavController().navigate(R.id.action_mapListFragment_to_profileFragment)
            }

            userIcon.setOnClickListener {
                userViewModel.setCardProfileUserData(binding.userName.text.toString(), placesList[position].userId)
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
        alertDialogBuilder.setMessage("Пожалуйста, залогинтесь, чтобы иметь возможность взаимодействовать с этой функциональностью")
            .setCancelable(false)
            .setPositiveButton("Залогиниться") { dialog, id -> // login
                fragment.requireActivity().getSharedPreferences("UID", AppCompatActivity.MODE_PRIVATE)
                    .edit()
                    .putString("id", "-1")
                    .apply()
                userViewModel.deleteUser()
                (fragment.requireActivity() as MainActivity).navRestart()
            }
            .setNegativeButton(
                "Отмена"
            ) { dialog, id -> // Закрываем диалоговое окно
                dialog.cancel()
            }
        alertDialogBuilder.create().show()
    }
}