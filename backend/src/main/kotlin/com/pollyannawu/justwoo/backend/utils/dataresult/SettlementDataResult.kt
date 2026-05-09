package com.pollyannawu.justwoo.backend.utils.dataresult

sealed class SettlementDataResult<out T> {
    data class Success<T>(val data: T) : SettlementDataResult<T>()

    sealed class Error : SettlementDataResult<Nothing>() {
        object HouseNotFound : Error()
        data class UserNotAllowed(val id: Long) : Error()
        data class DatabaseError(val message: String) : Error()
        object InvalidAmount : Error()
        data class InvalidCurrency(val code: String) : Error()
    }
}
