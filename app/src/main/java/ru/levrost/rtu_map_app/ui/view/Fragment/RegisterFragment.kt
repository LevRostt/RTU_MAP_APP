package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.FragmentRegisterBinding
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RegisterFragment: Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val mBinding get() = _binding!!
    val executor = Executors.newScheduledThreadPool(2)
    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        debugLog(userViewModel.userData.value?.userId.toString())

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnReg.setOnClickListener {

            mBinding.progressBar.visibility = View.VISIBLE

            if (mBinding.loginField.editText?.text.toString().isEmpty()) {
                mBinding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Заполните, пожалуйста, поле имени", Toast.LENGTH_LONG).show()
            } else if (mBinding.passwordFiled.editText?.text.toString().isEmpty()) {
                mBinding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Заполните, пожалуйста, поле пароля", Toast.LENGTH_LONG)
                    .show()
            } else{
                userViewModel.register(mBinding.loginField.editText?.text.toString(), mBinding.passwordFiled.editText?.text.toString())

                executor.schedule({
                    mBinding.userAlreadyReg.visibility = View.VISIBLE
                }, 900, TimeUnit.MILLISECONDS)
                executor.schedule({
                    mBinding.progressBar.visibility = View.GONE
                }, 1000, TimeUnit.MILLISECONDS)

                userViewModel.getUser().observe(viewLifecycleOwner){
                    val sharePref = requireActivity().getSharedPreferences(
                        "UID",
                        AppCompatActivity.MODE_PRIVATE
                    )
                    val userId = sharePref.getString("id", "0")
                    // чек интернета
                    if (sharePref.getString("id", "0") != "0" && sharePref.getString("id", "0") != "-1") {
                        debugLog(it.userId + " " + it.name)
                        mBinding.progressBar.visibility = View.GONE
                        mBinding.userAlreadyReg.visibility = View.INVISIBLE
                        findNavController().navigate(R.id.mainFragment) // Навигация по action выдаёт ошибку
                    }
                }
            }

        }
    }

    override fun onStop() {
        super.onStop()
        userViewModel.getUser().removeObservers(viewLifecycleOwner)
        executor.shutdown()
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}