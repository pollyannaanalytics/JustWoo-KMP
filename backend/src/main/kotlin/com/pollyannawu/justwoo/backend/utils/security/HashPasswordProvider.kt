package com.pollyannawu.justwoo.backend.utils.security

import at.favre.lib.crypto.bcrypt.BCrypt

interface HashPasswordProvider {
    fun hashPassword(password: String): String
    fun validatePassword(plain: String, hash: String): Boolean
}

class BcryptHashPasswordProvider: HashPasswordProvider {

    override fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(10, password.toCharArray())
    }

    override fun validatePassword(plain: String, hash: String): Boolean {
        val result = BCrypt.verifyer().verify(plain.toCharArray(),hash.toCharArray())
        return result.verified
    }
}