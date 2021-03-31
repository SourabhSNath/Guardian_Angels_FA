package com.guardianangels.football.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.guardianangels.football.R
import com.guardianangels.football.databinding.LoginFragmentBinding
import com.guardianangels.football.network.NetworkState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.login_fragment) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LoginFragmentBinding.bind(view)

        val loginTV = binding.loginTV
        val backButton = binding.backButton
        val passwordEditText = binding.passwordEditText
        val passwordTextField = binding.passwordTF
        val loginButton = binding.loginButton
        val logoutButton = binding.logoutButton

        if (viewModel.checkLogin()) {
            loginAction(loginTV, passwordTextField, loginButton, logoutButton)
        } else {
            viewModel.logout()
            logoutAction(loginTV, passwordTextField, loginButton, logoutButton)
        }

        loginButton.setOnClickListener {
            val password = passwordEditText.text.toString()
            if (password.isNotBlank()) viewModel.login(password)
        }

        logoutButton.setOnClickListener {
            viewModel.logout()
            logoutAction(loginTV, passwordTextField, loginButton, logoutButton)
        }

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.loginState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkState.Success -> {
                    Toast.makeText(requireContext(), "Successful Login", Toast.LENGTH_SHORT).show()
                    loginAction(loginTV, passwordTextField, loginButton, logoutButton)
                    binding.progressBar.visibility = View.GONE
                }

                is NetworkState.Failed -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    passwordEditText.error = it.message
                    binding.progressBar.visibility = View.GONE
                }
            }
        }


    }

    private fun logoutAction(
        loginTV: TextView,
        passwordEditText: TextInputLayout,
        loginButton: MaterialButton,
        logoutButton: Button
    ) {
        loginTV.text = "Log in"
        passwordEditText.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        logoutButton.visibility = View.GONE
    }

    private fun loginAction(
        loginTV: TextView,
        passwordEditText: TextInputLayout,
        loginButton: MaterialButton,
        logoutButton: Button
    ) {
        loginTV.text = "Log out"
        passwordEditText.visibility = View.GONE
        loginButton.visibility = View.GONE
        logoutButton.visibility = View.VISIBLE
    }


}