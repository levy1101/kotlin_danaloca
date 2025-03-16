package com.levy.danaloca.utils

sealed class Resource<T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(Status.SUCCESS, data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(Status.ERROR, data, message)
    class Loading<T>(data: T? = null) : Resource<T>(Status.LOADING, data)

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> error(message: String, data: T? = null): Resource<T> = Error(message, data)
        fun <T> loading(data: T? = null): Resource<T> = Loading(data)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}