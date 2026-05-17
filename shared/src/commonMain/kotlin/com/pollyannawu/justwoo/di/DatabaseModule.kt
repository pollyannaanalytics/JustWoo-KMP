package com.pollyannawu.justwoo.di

import com.pollyannawu.justwoo.data.datasource.HouseDataSource
import com.pollyannawu.justwoo.data.datasource.ProfileDataSource
import com.pollyannawu.justwoo.data.datasource.SettlementDataSource
import com.pollyannawu.justwoo.data.datasource.SqlDelightHouseDataSource
import com.pollyannawu.justwoo.data.datasource.SqlDelightProfileDataSource
import com.pollyannawu.justwoo.data.datasource.SqlDelightSettlementDataSource
import com.pollyannawu.justwoo.data.datasource.SqlDelightTaskDataSource
import com.pollyannawu.justwoo.data.datasource.TaskDataSource
import com.pollyannawu.justwoo.db.HouseMemberEntity
import com.pollyannawu.justwoo.data.db.InstantAdapter
import com.pollyannawu.justwoo.db.JustWooDatabase
import com.pollyannawu.justwoo.db.ProfileEntity
import com.pollyannawu.justwoo.db.SettlementEntity
import com.pollyannawu.justwoo.db.HouseEntity
import com.pollyannawu.justwoo.db.TaskAssigneeEntity
import com.pollyannawu.justwoo.db.TaskEntity
import com.pollyannawu.justwoo.data.db.accessLevelAdapter
import com.pollyannawu.justwoo.data.db.assignStatusAdapter
import com.pollyannawu.justwoo.data.db.memberRoleAdapter
import com.pollyannawu.justwoo.data.db.taskStatusAdapter
import org.koin.core.qualifier.named
import org.koin.dsl.module

val IO_DISPATCHER = named("io")

val databaseModule = module {
    single {
        JustWooDatabase(
            driver = get(),
            TaskEntityAdapter = TaskEntity.Adapter(
                accessLevelAdapter = accessLevelAdapter,
                taskStatusAdapter = taskStatusAdapter,
                dueTimeAdapter = InstantAdapter,
                createTimeAdapter = InstantAdapter,
                updateTimeAdapter = InstantAdapter,
            ),
            TaskAssigneeEntityAdapter = TaskAssigneeEntity.Adapter(
                statusAdapter = assignStatusAdapter,
            ),
            HouseEntityAdapter = HouseEntity.Adapter(
                createTimeAdapter = InstantAdapter,
                updateTimeAdapter = InstantAdapter,
            ),
            HouseMemberEntityAdapter = HouseMemberEntity.Adapter(
                roleAdapter = memberRoleAdapter,
                joinedAtAdapter = InstantAdapter,
            ),
            SettlementEntityAdapter = SettlementEntity.Adapter(
                createTimeAdapter = InstantAdapter,
            ),
            ProfileEntityAdapter = ProfileEntity.Adapter(
                createTimeAdapter = InstantAdapter,
                updateTimeAdapter = InstantAdapter,
            ),
        )
    }

    single<TaskDataSource> {
        SqlDelightTaskDataSource(db = get(), dispatcher = get(IO_DISPATCHER))
    }
    single<HouseDataSource> {
        SqlDelightHouseDataSource(db = get(), dispatcher = get(IO_DISPATCHER))
    }
    single<SettlementDataSource> {
        SqlDelightSettlementDataSource(db = get(), dispatcher = get(IO_DISPATCHER))
    }
    single<ProfileDataSource> {
        SqlDelightProfileDataSource(db = get(), dispatcher = get(IO_DISPATCHER))
    }
}
