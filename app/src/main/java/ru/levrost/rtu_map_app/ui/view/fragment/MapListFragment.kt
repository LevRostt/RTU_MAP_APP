package ru.levrost.rtu_map_app.ui.view.fragment

import android.os.Bundle
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
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.MapListFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.lang.Exception

class MapListFragment: Fragment() {
    private var _binding: MapListFragmentBinding? = null
    private val mBinding get() = _binding!!
    private var _controller: MapListFragmentController? = null
    private val controller get() = _controller!!


    val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapListFragmentBinding.inflate(inflater, container, false)
        _controller = MapListFragmentController(this, mBinding)

        placeListViewModel.setLastUriImage(null)//обнуление поля

        mBinding.btnToAddPlace.setOnClickListener {
            findNavController().navigate(R.id.action_mapListFragment_to_createPlaceFragment)
        }

        placeListViewModel.placeList.observe(viewLifecycleOwner){
            controller.placeListUpdate(it)
        }

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller.setupRV()

        mBinding.btnFilter.setOnClickListener {
            controller.showMenu(it, R.menu.menu_map_list_filter)
        }
        mBinding.searchView.setOnQueryTextListener(controller.searchViewQueryListiner)

        controller.updateAdapter()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _controller = null
        userViewModel.getUser().removeObservers(viewLifecycleOwner)
        placeListViewModel.placeList.removeObservers(viewLifecycleOwner)
        placeListViewModel.getPlaceByText("").removeObservers(viewLifecycleOwner)
    }
}