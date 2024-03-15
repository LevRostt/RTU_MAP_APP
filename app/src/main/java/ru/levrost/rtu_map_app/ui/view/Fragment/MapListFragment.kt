package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import ru.levrost.rtu_map_app.ui.adapters.PlaceListRVAdapter
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

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
                if (newText != null) {
                    placeListViewModel.getPlaceByText(newText).observe(viewLifecycleOwner){
                        searchListToRV = it as ArrayList<Place>
                        containAndPushList()
                        placeListViewModel.getPlaceByText(newText).removeObservers(viewLifecycleOwner)
                    }
                }
                else
                    searchListToRV = typeListToRV
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
        Log.d("LRDebugMess", typeListToRV.toString())
        when(filterStates){ // do in thread
            0 -> {
                typeListToRV = ArrayList(l)
            }
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
        val list = searchListToRV.filter {
            for (i in typeListToRV){
                if(i.idPlace == it.idPlace)
                    return@filter true
            }
            false
        } // Можно оптимизировать*
        (mBinding.rvMapList.adapter as PlaceListRVAdapter).updateData(list)
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener {
            Log.d("LRDebugMess", it.title.toString() + " id")
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