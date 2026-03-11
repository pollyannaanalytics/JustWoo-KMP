package com.pollyannawu.justwoo.backend.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

internal object Chats: LongIdTable("chats") {
    val content = text("content")
}

internal object ChatsUsers: LongIdTable("chats_users"){
    val chatId = reference("chat_id", Chats, ReferenceOption.CASCADE)
    val assigner = reference("assigner", Users, ReferenceOption.CASCADE)
    val assignee = reference("assignee", Users, ReferenceOption.CASCADE)
}
