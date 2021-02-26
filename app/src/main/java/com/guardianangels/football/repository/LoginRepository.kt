package com.guardianangels.football.repository

import com.google.firebase.auth.FirebaseAuth
import com.guardianangels.football.network.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class LoginRepository constructor(private val auth: FirebaseAuth) {

    private companion object {
        /* This is the only email allowed to login. The user will not have the ability to create a new account. */
        private const val EMAIL = "admin@guardian.angels"
    }

    /**
     * Check if the user is logged in
     */
    fun checkLogin() = auth.currentUser != null

    /**
     *
     */
    val currentUserUid = if (checkLogin()) auth.currentUser?.uid else null

    /**
     * Login user and return a flow
     */
    fun loginAdmin(password: String) = flow<NetworkState<Boolean>> {
        emit(NetworkState.loading())
        auth.signInWithEmailAndPassword(EMAIL, password).await()

        // Check if the user has logged in successfully
        if (checkLogin()) {
            emit(NetworkState.success(true))
        } else
            emit(NetworkState.failed("Log in Failed. Please try again."))

    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * Logout
     */
    fun logoutAdmin() {
        auth.signOut()
    }
}