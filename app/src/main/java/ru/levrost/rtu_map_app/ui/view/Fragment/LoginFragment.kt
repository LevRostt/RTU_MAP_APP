package ru.levrost.rtu_map_app.ui.view.Fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentLoginBinding
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val mBinding get() = _binding!!

    private var executor : ScheduledExecutorService? = null

    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        mBinding.btnReg.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        mBinding.btnLogin.setOnClickListener {

            mBinding.progressBar.visibility = View.VISIBLE

            if (mBinding.loginField.editText?.text.toString().isEmpty()) {
                mBinding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Заполните, пожалуйста, поле имени", Toast.LENGTH_LONG).show()
            } else if (mBinding.passwordFiled.editText?.text.toString().isEmpty()) {
                mBinding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Заполните, пожалуйста, поле пароля", Toast.LENGTH_LONG)
                    .show()
            } else {
                userViewModel.login(mBinding.loginField.editText?.text.toString(), mBinding.passwordFiled.editText?.text.toString())
                executor = Executors.newScheduledThreadPool(2)

                executor!!.schedule({
                    mBinding.userAlreadyReg.visibility = View.VISIBLE
                }, 900, TimeUnit.MILLISECONDS)
                executor!!.schedule({
                    mBinding.progressBar.visibility = View.GONE
                }, 1000, TimeUnit.MILLISECONDS)

                userViewModel.getUser().observe(viewLifecycleOwner){
                    val sharePref = requireActivity().getSharedPreferences(
                        "UNAME",
                        AppCompatActivity.MODE_PRIVATE
                    )
                    val userName = sharePref.getString("name", "-1")
                    // чек интернета
                    if (userName != "0" && userName != "-1") {
                        debugLog(it.userId + " " + it.name)
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.userAlreadyReg.visibility = View.INVISIBLE
                        findNavController().navigate(R.id.mainFragment) // Навигация по action выдаёт ошибку
                   }
                }

            }
        }

        mBinding.singInAsGuest.setOnClickListener {
            userViewModel.loginAsGuest()

            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }


        return mBinding.root
    }

    override fun onStop() {
        super.onStop()
        userViewModel.getUser().removeObservers(viewLifecycleOwner)
        executor?.shutdown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}