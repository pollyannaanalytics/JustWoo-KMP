package com.pollyannawu.justwoo.data

interface AuthService {
    suspend fun register(email: String, password: String, name: String)
}