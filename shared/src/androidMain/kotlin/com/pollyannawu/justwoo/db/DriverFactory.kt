package com.pollyannawu.justwoo.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(
            schema = JustWooDatabase.Schema,
            context = context,
            name = DB_NAME,
            callback = object : AndroidSqliteDriver.Callback(JustWooDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            },
        )

    private companion object {
        const val DB_NAME = "justwoo.db"
    }
}
