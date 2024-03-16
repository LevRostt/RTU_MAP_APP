package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.databinding.FragmentProfileBinding
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class ProfileFragment: Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val mBinding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }
    private var cardUserProfileId = "0"

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
        else{
            cardUserProfileId = userViewModel.cardProfileUserData[1]
            Log.d("LRDebugMess",cardUserProfileId)
            mBinding.apply {
                subscribeBtn.visibility = View.VISIBLE
                jumpBack.visibility = View.VISIBLE
                exit.visibility = View.GONE
                personName.text = userViewModel.cardProfileUserData[0]
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

        mBinding.jumpBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}