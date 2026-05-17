package com.pollyannawu.justwoo.data.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun create(): SqlDriver
}