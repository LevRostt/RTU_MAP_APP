package ru.levrost.rtu_map_app.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationBarView
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.MainFragmentBinding

class MainFragment: Fragment() {
    private lateinit var mBinding: MainFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = MainFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = (childFragmentManager.findFragmentById(R.id.main_nav_container) as NavHostFragment).navController
        NavigationUI.setupWithNavController(view.findViewById(R.id.bottom_navigation_view) as NavigationBarView, navController)
    }
}