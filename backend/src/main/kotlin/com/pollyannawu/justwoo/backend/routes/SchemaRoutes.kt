package com.pollyannawu.justwoo.backend.routes

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private val MERMAID_DIAGRAM = """
erDiagram
    Users {
        Long id PK
        String email
        String password
        Instant createTime
        Instant updateTime
    }
    Profiles {
        Long id PK
        Long userId FK
        String name
        String avatar
        String bankAccount
        Instant createTime
        Instant updateTime
    }
    Houses {
        Long id PK
        String name
        String description
        String avatar
        Instant createTime
        Instant updateTime
    }
    HouseMembers {
        Long id PK
        Long houseId FK
        Long memberId FK
        Int role
        Instant joinAt
    }
    Tasks {
        Long id PK
        String title
        Long ownerId FK
        Long houseId FK
        Long executorId FK
        String description
        Int accessLevel
        Int taskStatus
        Double price
        String currencyCode
        Instant dueTime
        Instant createTime
        Instant updateTime
    }
    TasksAssignees {
        Long id PK
        Long taskId FK
        Long userId FK
        Int status
    }
    Settlements {
        Long id PK
        Long houseId FK
        Long payerId FK
        Long payeeId FK
        Double amount
        String currencyCode
        String note
        Instant createTime
    }
    Chats {
        Long id PK
        String content
    }
    ChatsTasks {
        Long id PK
        Long taskId FK
        Long chatId FK
    }

    Users ||--o| Profiles : "has profile"
    Users ||--o{ HouseMembers : "joins"
    Houses ||--o{ HouseMembers : "has members"
    Users ||--o{ Tasks : "owns"
    Houses ||--o{ Tasks : "contains"
    Users ||--o{ Tasks : "executes"
    Tasks ||--o{ TasksAssignees : "assigned to"
    Users ||--o{ TasksAssignees : "is assignee"
    Houses ||--o{ Settlements : "tracks"
    Users ||--o{ Settlements : "pays (payer)"
    Users ||--o{ Settlements : "receives (payee)"
    Tasks ||--o{ ChatsTasks : "linked to chat"
    Chats ||--o{ ChatsTasks : "linked to task"
""".trimIndent()

private fun schemaHtml() = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>JustWoo — Database Schema</title>
  <script src="https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js"></script>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f8f9fa; }
    header { background: #1a1a2e; color: #fff; padding: 20px 32px; }
    header h1 { font-size: 20px; font-weight: 600; }
    header p  { font-size: 13px; color: #a0a8b8; margin-top: 4px; }
    main { padding: 32px; display: flex; justify-content: center; }
    .diagram-card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0,0,0,.08);
      padding: 32px;
      max-width: 1200px;
      width: 100%;
      overflow-x: auto;
    }
  </style>
</head>
<body>
  <header>
    <h1>JustWoo — Database Schema</h1>
    <p>Entity Relationship Diagram</p>
  </header>
  <main>
    <div class="diagram-card">
      <div class="mermaid">
$MERMAID_DIAGRAM
      </div>
    </div>
  </main>
  <script>mermaid.initialize({ startOnLoad: true, theme: 'default' });</script>
</body>
</html>
""".trimIndent()

fun Route.schemaRoute() {
    get("/schema") {
        call.respondText(schemaHtml(), ContentType.Text.Html)
    }
}
