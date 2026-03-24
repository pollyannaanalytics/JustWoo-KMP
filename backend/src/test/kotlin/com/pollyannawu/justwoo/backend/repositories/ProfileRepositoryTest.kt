//package com.pollyannawu.justwoo.backend.repositories
//
//import com.pollyannawu.justwoo.backend.schema.Users
//import com.pollyannawu.justwoo.core.Profile
//import kotlinx.coroutines.runBlocking
//import kotlinx.datetime.TimeZone
//import kotlinx.datetime.toLocalDateTime
//import org.jetbrains.exposed.sql.insertAndGetId
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Test
//import kotlin.test.assertFailsWith
//import kotlin.time.Clock
//import kotlin.time.ExperimentalTime
//
//@OptIn(ExperimentalTime::class)
//class ProfileRepositoryTest : BaseRepositoryTest() {
//
//    private val repository: ProfileRepository = DefaultProfileRepository()
//
//    @Test
//    fun `should create and get profile`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val userId = transaction {
//            Users.insertAndGetId {
//                it[email] = "test@example.com"
//                it[password] = "password"
//            }.value
//        }
//
//        val profile = Profile(
//            id = userId,
//            name = "Test User",
//            avatar = "avatar_url",
//            bankAccount = "123456",
//            createTime = now,
//            updateTime = now
//        )
//
//        val created = repository.createProfile(profile)
//        assertEquals(profile, created)
//
//        val fetched = repository.getProfile(userId)
//        assertEquals(profile, fetched)
//    }
//
//    @Test
//    fun `should get multiple profiles`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val (id1, id2) = transaction {
//            val uid1 = Users.insertAndGetId {
//                it[email] = "user1@example.com"
//                it[password] = "p1"
//            }.value
//            val uid2 = Users.insertAndGetId {
//                it[email] = "user2@example.com"
//                it[password] = "p2"
//            }.value
//            uid1 to uid2
//        }
//
//        val p1 = Profile(id1, "User 1", "av1", "bank1", now, now)
//        val p2 = Profile(id2, "User 2", "av2", "bank2", now, now)
//
//        repository.createProfile(p1)
//        repository.createProfile(p2)
//
//        val profiles = repository.getProfiles(listOf(id1, id2))
//        assertEquals(2, profiles.size)
//        assertEquals(setOf(p1, p2), profiles.toSet())
//    }
//
//    @Test
//    fun `should update profile`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val userId = transaction {
//            Users.insertAndGetId {
//                it[email] = "update@example.com"
//                it[password] = "pass"
//            }.value
//        }
//
//        val profile = Profile(userId, "Old Name", "old_av", "old_bank", now, now)
//        repository.createProfile(profile)
//
//        val updatedProfile = profile.copy(name = "New Name", bankAccount = "new_bank")
//        val result = repository.updateProfile(updatedProfile)
//
//        assertEquals("New Name", result.name)
//        assertEquals("new_bank", result.bankAccount)
//
//        val fetched = repository.getProfile(userId)
//        assertEquals("New Name", fetched.name)
//    }
//
//    @Test
//    fun `should throw exception when profile not found`() = runBlocking {
//        assertFailsWith<NoSuchElementException> {
//            repository.getProfile(999L)
//        }
//    }
//}
