package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentRegisterBinding
import ru.levrost.rtu_map_app.global.ResultStatus
import ru.levrost.rtu_map_app.ui.view.fragment.RegisterFragment
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RegisterFragmentController(val fragment: RegisterFragment, val binding: FragmentRegisterBinding) {
    fun register(){
        fragment.apply {
            if (binding.loginField.editText?.text.toString().isEmpty()) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    context,
                    ContextCompat.getString(requireContext(), R.string.pls_fill_name_field),
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.passwordFiled.editText?.text.toString().isEmpty()) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    context,
                    ContextCompat.getString(requireContext(), R.string.pls_fill_passwd_field),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                binding.progressBar.visibility = View.VISIBLE
                tryToReg()
            }
        }
    }

    private fun tryToReg(){
        fragment.apply {
            userViewModel.register(
                binding.loginField.editText?.text.toString(),
                binding.passwordFiled.editText?.text.toString()
            ).observe(viewLifecycleOwner){
                if (it != ResultStatus.Success){
                    binding.progressBar.visibility = View.GONE
                    when (it) {
                        ResultStatus.ServerFail -> binding.someServerError.visibility = View.VISIBLE
                        ResultStatus.UserFail -> binding.userAlreadyReg.visibility = View.VISIBLE
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.userAlreadyReg.visibility = View.INVISIBLE
                    findNavController().navigate(R.id.mainFragment)
                }
            }
        }
    }
}