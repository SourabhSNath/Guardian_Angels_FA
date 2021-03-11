package com.guardianangels.football.network

import java.lang.Exception


sealed class NetworkState<T> {
    class Loading<T> : NetworkState<T>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class Failed<T>(val exception: Throwable, val message: String) : NetworkState<T>()

    companion object {
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success(data)
        fun <T> failed(exception: Throwable, message: String) = Failed<T>(exception, message)
    }
}