package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapListFragmentBinding
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.ui.adapters.PlaceListRVAdapter
import ru.levrost.rtu_map_app.ui.view.fragment.MapListFragment
import java.lang.Exception

class MapListFragmentController(val fragment : MapListFragment, val binding: MapListFragmentBinding) {

    private var filterStates : Int = 0 // 0 - all pics, 1 - without pics, 2 - with pics

    private var searchListToRV = ArrayList<Place>() //Создаём два списка, каждый из которых будт заполняться по отдельности, а потом объединятсья и выводиться
    private var typeListToRV = ArrayList<Place>()

    fun placeListUpdate(list : List<Place>){
        searchListToRV = list as ArrayList<Place>
        typeListToRV = searchListToRV
        containAndPushList()
        fragment.placeListViewModel.placeList.removeObservers(fragment.viewLifecycleOwner) // Позволяет единоразово получить данные для инициализации и сразу отписаться
    }

    fun setupRV(){
        binding.rvMapList.layoutManager = LinearLayoutManager(fragment.context)
        binding.rvMapList.adapter = PlaceListRVAdapter(fragment, fragment.userViewModel, fragment.placeListViewModel)
    }

    fun updateAdapter(){

//        userViewModel.getUser().removeObserver(userVMObserver())
//        placeListViewModel.placeList.removeObserver(placeVMObserver())

        fragment.userViewModel.getUser().observe(fragment.viewLifecycleOwner, userVMObserver())
        fragment.placeListViewModel.placeList.observe(fragment.viewLifecycleOwner, placeVMObserver())
    }
    private fun userVMObserver() = Observer<UserData>{
        containAndPushList()
    }


    private fun placeVMObserver() = Observer<List<Place>>{l ->
        typeListToRV = l as ArrayList<Place>
        when(filterStates){ // do in thread
            1 -> {
                typeListToRV = typeListToRV.filter { !it.isPicSaved()  } as ArrayList<Place>
            }
            2 -> {
                typeListToRV = typeListToRV.filter { it.isPicSaved() } as ArrayList<Place>
            }
        }
        containAndPushList()
    }

    fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(fragment.requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.title) {
                ContextCompat.getString(fragment.requireContext(), R.string.all_places) -> filterStates = 0
                ContextCompat.getString(fragment.requireContext(), R.string.only_without_pictures) -> filterStates = 1
                ContextCompat.getString(fragment.requireContext(), R.string.only_with_pictures) -> filterStates = 2
            }
            updateAdapter()
            true
        }
        popup.show()
    }

    val searchViewQueryListiner = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText != null && newText != "") {
                fragment.placeListViewModel.getPlaceByText(newText).observeOnce(fragment.viewLifecycleOwner){
                    searchListToRV = it as ArrayList<Place>
                    containAndPushList()
                }
            }
            else {
                searchListToRV = typeListToRV
            }
            containAndPushList()
            return true
        }
    }


    private fun containAndPushList(){

        var list = searchListToRV.filter {
            if (filterStates == 0)
                true
            else {
                for (i in typeListToRV) {
                    if (i.idPlace == it.idPlace)
                        return@filter true
                }
                false
            }
        } // Можно оптимизировать* | Выполнять в фоне

        val compRV = Comparator<Place>{ place1, place2 ->
            try {
                if (place1.userName > place2.userName){
                    return@Comparator 1
                }
                else if (place1.userName == place2.userName){
                    if (place1.likes < place2.likes)
                        return@Comparator 1
                    else if (place1.likes == place2.likes){
                        return@Comparator 0
                    }
                    else {
                        return@Comparator -1
                    }
                }
            }catch (_ : Exception){}
            return@Comparator -1
        }

        if (binding.searchView.query.toString() != "" && list.isNotEmpty())
            list = list.sortedWith(compRV)

        (binding.rvMapList.adapter as PlaceListRVAdapter).updateData(list)
    }

}