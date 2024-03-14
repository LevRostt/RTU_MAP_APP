package ru.levrost.rtu_map_app.ui.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapListPlaceBinding
import ru.levrost.rtu_map_app.ui.view.Fragment.MapListFragment
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.util.Base64

class PlaceListRVAdapter(
    private val fragment : MapListFragment,
    private val userViewModel : UserViewModel,
    private val placeListViewModel: PlaceListViewModel
    ) : RecyclerView.Adapter<PlaceListRVAdapter.PlaceListHolder>() {


    private var placesList : List<Place> = ArrayList()
    private var userData : UserData? = null


    class PlaceListHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var _binding : MapListPlaceBinding
        val binding get() = _binding

        init {
            _binding = MapListPlaceBinding.bind(itemView)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceListHolder {
        return PlaceListHolder(
            MapListPlaceBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
                .root
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Place>){
        placesList = list
        notifyDataSetChanged()
    }

    fun updateData(data: UserData){
        userData = data
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun onBindViewHolder(holder: PlaceListHolder, position: Int) {

        val context = holder.itemView.context
        val place = placesList[position]
        val binding = holder.binding

        binding.apply {

            userName.text = place.userName
            placeName.text = place.name
            placeInfo.text = place.description
            countOfLikes.text = place.likes.toString()

            val pic = Base64.getDecoder().decode(place.image)

            placePic.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    pic,
                    0,
                    pic.size
                )
            )

            if (userData?.likes?.contains(place.idPlace) == true){
                btnLike.setImageResource(R.drawable.favorite_icon_active)
            }

        }

        binding.btnLike.setOnClickListener {
            if (userData?.likes?.contains(place.idPlace) == false){
                //add like
                userViewModel.likePlace(place.idPlace)
                placeListViewModel.likePlace(position)
                binding.btnLike.setImageResource(R.drawable.favorite_icon_active)
                binding.countOfLikes.text = (binding.countOfLikes.text.toString().toInt() + 1).toString()
            }
            else{
                userViewModel.unLikePlace(place.idPlace)
                binding.btnLike.setImageResource(R.drawable.favorite_icon)
                binding.countOfLikes.text = (binding.countOfLikes.text.toString().toInt() - 1).toString()
            }
        }

        binding.btnShowOnMap.setOnClickListener {
            placeListViewModel.selectPlace(place.latitude,place.longitude)
            fragment.findNavController().popBackStack()
        }

        binding.btnDelete.setOnClickListener {
            // delete
            placeListViewModel.deletePlace(place.idPlace)
        }

        holder.itemView.animation =
            AnimationUtils.loadAnimation(
                context,
                R.anim.fade_out
//                context.resources.getIdentifier("fade_out", "anim", context.packageName)
            )

    }
}