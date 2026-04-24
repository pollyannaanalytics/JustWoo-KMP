package com.pollyannawu.justwoo.model

import com.pollyannawu.justwoo.core.House

sealed class HouseDataResult {
    data class Success(val houses: List<House>) : HouseDataResult()
    sealed class Failure : HouseDataResult() {
        data class NetworkError(val error: String) : HouseDataResult()
        data class LocalError(val error: String) : HouseDataResult()
    }
}
