package com.guardianangels.football.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.guardianangels.football.R
import com.guardianangels.football.databinding.LoginFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.getString
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.login_fragment) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = LoginFragmentBinding.bind(view)

        if (viewModel.checkLogin()) {
            Timber.d("Login: ${viewModel.checkLogin()}")
            setupLoginOrLogout(true, binding)
        } else {
            viewModel.logout()
            Timber.d("Logout: ${viewModel.checkLogin()}")
            setupLoginOrLogout(false, binding)
        }

        val emptyEmailError = "Please enter an email address"
        val emptyPasswordError = "Password shouldn't be empty"
        binding.loginButton.setOnClickListener {
            val password = binding.passwordEditText.getString()
            val email = binding.emailET.getString()

            if (password.isNotBlank() && email.isNotBlank()) viewModel.login(email, password)

            if (password.isBlank()) binding.passwordTF.setOrResetError(emptyPasswordError)
            else binding.passwordTF.setOrResetError()

            if (email.isBlank()) binding.emailTF.setOrResetError(emptyEmailError)
            else binding.emailTF.setOrResetError()
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
            setupLoginOrLogout(false, binding)
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.forgotPasswordTV.setOnClickListener {
            setupPasswordResetState(true, binding)
        }

        binding.passwordResetButton.setOnClickListener {
            val email = binding.emailET.getString()
            if (email.isNotEmpty()) {
                viewModel.forgotPassword(email)
                binding.emailTF.setOrResetError()
            } else
                binding.emailTF.setOrResetError(emptyEmailError)
        }

        binding.cancelPasswordResetButton.setOnClickListener {
            setupPasswordResetState(false, binding)
        }

        viewModel.loginState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkState.Success -> {
                    Toast.makeText(requireContext(), "Successful Login", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    setupLoginOrLogout(true, binding)
                    binding.emailTF.setOrResetError()
                }

                is NetworkState.Failed -> {
                    Timber.d("$it")
                    // Hide the Keyboard to make the Snackbar visible, the snackbar doesn't appear above the keyboard.
                    (requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
                    if (it.exception is IllegalArgumentException) {
                        Snackbar.make(view, "Something went wrong. Please check your email or password.", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(view, it.message, Snackbar.LENGTH_LONG).show()
                    }

                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.isMailSent.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkState.Success -> {
                    Snackbar.make(view, "Password reset link sent to your email.", Snackbar.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                    setupPasswordResetState(false, binding) // Go back to normal state
                }
                is NetworkState.Failed -> {
                    Snackbar.make(view, it.message, Snackbar.LENGTH_LONG).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupPasswordResetState(isPasswordResetState: Boolean, binding: LoginFragmentBinding) {
        binding.forgotPasswordTitle.visibility = if (isPasswordResetState) View.VISIBLE else View.GONE
        binding.loginTV.visibility = if (isPasswordResetState) View.INVISIBLE else View.VISIBLE
        binding.passwordTF.visibility = if (isPasswordResetState) View.GONE else View.VISIBLE
        binding.passwordResetButton.visibility = if (isPasswordResetState) View.VISIBLE else View.GONE
        binding.cancelPasswordResetButton.visibility = if (isPasswordResetState) View.VISIBLE else View.GONE
        binding.forgotPasswordTV.visibility = if (isPasswordResetState) View.GONE else View.VISIBLE
    }

    private fun setupLoginOrLogout(isLogin: Boolean, binding: LoginFragmentBinding) {
        binding.loginTV.text = if (isLogin) "Log out" else "Log in"
        binding.passwordTF.visibility = if (isLogin) View.GONE else View.VISIBLE
        binding.emailTF.visibility = if (isLogin) View.GONE else View.VISIBLE
        binding.loginButton.visibility = if (isLogin) View.GONE else View.VISIBLE
        binding.logoutButton.visibility = if (isLogin) View.VISIBLE else View.GONE
    }

    private fun TextInputLayout.setOrResetError(error: String? = null) {
        this.error = error
        isErrorEnabled = error != null
    }

}