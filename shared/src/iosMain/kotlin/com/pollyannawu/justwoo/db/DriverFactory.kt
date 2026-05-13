package com.pollyannawu.justwoo.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(
            schema = JustWooDatabase.Schema,
            name = DB_NAME,
            onConfiguration = { config ->
                config.copy(
                    extendedConfig = config.extendedConfig.copy(
                        foreignKeyConstraints = true,
                    ),
                )
            },
        )

    private companion object {
        const val DB_NAME = "justwoo.db"
    }
}
