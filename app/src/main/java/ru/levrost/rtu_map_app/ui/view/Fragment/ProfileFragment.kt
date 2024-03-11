package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.levrost.rtu_map_app.databinding.FragmentProfileBinding
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class ProfileFragment: Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val mBinding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments?.getBoolean("isUser") == true){
            mBinding.apply {
                subscribeBtn.visibility = View.GONE
                jumpBack.visibility = View.GONE
                exit.visibility = View.VISIBLE
                userViewModel.getUser().observe(viewLifecycleOwner){
                    personName.text = it.name
                }
            }
        }

        mBinding.exit.setOnClickListener {
            requireActivity().getSharedPreferences("UID", AppCompatActivity.MODE_PRIVATE)
                .edit()
                .putString("id", "-1")
                .apply()
            userViewModel.deleteUser()
            (activity as MainActivity).navRestart()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}