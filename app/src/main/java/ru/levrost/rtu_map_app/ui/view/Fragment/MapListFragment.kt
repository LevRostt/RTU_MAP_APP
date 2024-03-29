package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapListFragmentBinding
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.ui.adapters.PlaceListRVAdapter
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.lang.Exception

class MapListFragment: Fragment() {
    private var _binding: MapListFragmentBinding? = null
    private val mBinding get() = _binding!!

    private var filterStates : Int = 0 // 0 - all pics, 1 - without pics, 2 - with pics

    private var searchListToRV = ArrayList<Place>() //Создаём два списка, каждый из которых будт заполняться по отдельности, а потом объединятсья и выводиться
    private var typeListToRV = ArrayList<Place>()

    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    private val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapListFragmentBinding.inflate(inflater, container, false)

        placeListViewModel.setLastUriImage(null)//обнуление поля

        mBinding.btnToAddPlace.setOnClickListener {
            findNavController().navigate(R.id.action_mapListFragment_to_createPlaceFragment)
        }

        placeListViewModel.placeList.observe(viewLifecycleOwner){
            searchListToRV = it as ArrayList<Place>
            typeListToRV = searchListToRV
            containAndPushList()
            placeListViewModel.placeList.removeObservers(viewLifecycleOwner) // Позволяет единоразово получить данные для инициализации и сразу отписаться
        }

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.rvMapList.layoutManager = LinearLayoutManager(context)
        mBinding.rvMapList.adapter = PlaceListRVAdapter(this, userViewModel, placeListViewModel)

        mBinding.btnFilter.setOnClickListener {
            showMenu(it, R.menu.menu_map_list_filter)
        }

        mBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText != "") {
                    placeListViewModel.getPlaceByText(newText).observeOnce(viewLifecycleOwner){
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
        })

        updateAdapter()
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

    private fun updateAdapter(){

//        userViewModel.getUser().removeObserver(userVMObserver())
//        placeListViewModel.placeList.removeObserver(placeVMObserver())

        userViewModel.getUser().observe(viewLifecycleOwner, userVMObserver())
        placeListViewModel.placeList.observe(viewLifecycleOwner, placeVMObserver())
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

        if (mBinding.searchView.query.toString() != "" && list.isNotEmpty())
            list = list.sortedWith(compRV)

        (mBinding.rvMapList.adapter as PlaceListRVAdapter).updateData(list)
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.title) {
                getString(R.string.all_places) -> filterStates = 0
                getString(R.string.only_without_pictures) -> filterStates = 1
                getString(R.string.only_with_pictures) -> filterStates = 2
            }
            updateAdapter()
            true
        }
        popup.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        userViewModel.getUser().removeObservers(viewLifecycleOwner)
        placeListViewModel.placeList.removeObservers(viewLifecycleOwner)
        placeListViewModel.getPlaceByText("").removeObservers(viewLifecycleOwner)
        _binding = null
    }
}