//package com.pollyannawu.justwoo.backend.repositories
//
//import com.pollyannawu.justwoo.backend.schema.Users
//import com.pollyannawu.justwoo.core.House
//import com.pollyannawu.justwoo.core.HouseMember
//import com.pollyannawu.justwoo.core.MemberRole
//import kotlinx.coroutines.runBlocking
//import kotlinx.datetime.TimeZone
//import kotlinx.datetime.toLocalDateTime
//import org.jetbrains.exposed.sql.insertAndGetId
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import kotlin.time.Clock
//import kotlin.time.ExperimentalTime
//
//@OptIn(ExperimentalTime::class)
//class HouseRepositoryTest : BaseRepositoryTest() {
//
//    private val repository: HouseRepository = DefaultHouseRepository()
//
//    @Test
//    fun `should create and get house details`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val userId = transaction {
//            Users.insertAndGetId {
//                it[email] = "house@example.com"
//                it[password] = "password"
//            }.value
//        }
//
//        val house = House(
//            id = 0,
//            name = "Test House",
//            description = "Test Description",
//            avatar = "house_avatar",
//            members = listOf(
//                HouseMember(0, userId, MemberRole.ADMIN, now)
//            ),
//            createTime = now,
//            updateTime = now
//        )
//
//        val created = repository.createHouse(house, userId)
//        assertEquals("Test House", created.name)
//        assertEquals(1, created.members.size)
//        assertEquals(userId, created.members.first().userId)
//
//        val details = repository.getHouseDetails(userId, created.id)
//        assertEquals(1, details.size)
//        assertEquals("Test House", details.first().name)
//    }
//
//    @Test
//    fun `should check membership correctly`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val (uid1, uid2) = transaction {
//            val u1 = Users.insertAndGetId { it[email] = "u1@h.com"; it[password] = "p" }.value
//            val u2 = Users.insertAndGetId { it[email] = "u2@h.com"; it[password] = "p" }.value
//            u1 to u2
//        }
//
//        val house = House(
//            id = 0,
//            name = "Member House",
//            description = "Desc",
//            avatar = "av",
//            members = listOf(
//                HouseMember(0, uid1, MemberRole.ADMIN, now)
//            ),
//            createTime = now,
//            updateTime = now
//        )
//
//        val created = repository.createHouse(house, uid1)
//
//        assertTrue(repository.isMember(uid1, created.id))
//        assertTrue(!repository.isMember(uid2, created.id))
//    }
//
//    @Test
//    fun `should update house content`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val userId = transaction {
//            Users.insertAndGetId { it[email] = "upd@h.com"; it[password] = "p" }.value
//        }
//
//        val house = House(0, "Old Name", "Old Desc", "av", emptyList(), now, now)
//        val created = repository.createHouse(house, userId)
//
//        val updated = created.copy(name = "New Name", description = "New Desc")
//        val result = repository.updateHouseContent(updated)
//
//        assertEquals("New Name", result.name)
//        assertEquals("New Desc", result.description)
//    }
//
//    @Test
//    fun `should remove member`() = runBlocking {
//        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//        val (uid1, uid2) = transaction {
//            val u1 = Users.insertAndGetId { it[email] = "m1@h.com"; it[password] = "p" }.value
//            val u2 = Users.insertAndGetId { it[email] = "m2@h.com"; it[password] = "p" }.value
//            u1 to u2
//        }
//
//        val house = House(
//            id = 0,
//            name = "Remove Member House",
//            description = "Desc",
//            avatar = "av",
//            members = listOf(
//                HouseMember(0, uid1, MemberRole.ADMIN, now),
//                HouseMember(0, uid2, MemberRole.MEMBER, now)
//            ),
//            createTime = now,
//            updateTime = now
//        )
//
//        val created = repository.createHouse(house, uid1)
//        assertEquals(2, created.members.size)
//
//        val afterRemoval = repository.removeMember(uid2, created.id)
//        assertEquals(1, afterRemoval.members.size)
//        assertTrue(afterRemoval.members.none { it.userId == uid2 })
//    }
//}
