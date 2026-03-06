# JustWoo 🏠 — Smart Roommate Management

**JustWoo** is a full-stack Kotlin solution designed to harmonize shared living. It tackles the two biggest friction points among roommates: **Chore Distribution** and **Expense Splitting**.

Built with a **Unified Kotlin Stack**, JustWoo demonstrates the power of sharing 100% of business logic across Android, iOS, and the Backend.

---

## 🚀 Key Features

- **Smart Chore Rotation:** Automated task assignment with recurring schedules and reminders.
- **Fair Expense Splitting:** Real-time balance tracking using a custom debt-minimization algorithm.
- **Offline-First Experience:** Seamlessly manage tasks even without an internet connection.
- **Real-time Sync:** Instant updates across all roommates' devices.

---

## 🛠 Technical Stack

| Layer | Technology |
| :--- | :--- |
| **Mobile UI** | Compose Multiplatform (Android & iOS) |
| **Backend** | Ktor Framework (Kotlin-native server) |
| **Shared Logic** | Kotlin Multiplatform (KMP) |
| **Local Database** | SQLDelight (Type-safe SQLite) |
| **Remote DB** | PostgreSQL (Exposed ORM) |
| **Dependency Injection** | Koin |
| **Concurrency** | Kotlin Coroutines & Flow |

---

## 🧠 Why KMP & Ktor? (The Full-Stack Kotlin Advantage)

As a developer aiming for the Prague tech market—the birthplace of JetBrains—I chose this stack to solve real-world engineering challenges:

### 1. Single Source of Truth (SSOT)
In a fintech-adjacent feature like **Expense Splitting**, data consistency is non-negotiable. By using **KMP**, the complex splitting logic (e.g., debt simplification) is written and unit-tested **once** in `commonMain`. This ensures that both Android and iOS calculate debts identically, eliminating "platform-specific bugs."

### 2. Contract-Driven Development with Ktor
By pairing **Ktor** with **KMP**, I can share Data Transfer Objects (DTOs) directly between the server and the mobile apps.
- **No more manual mapping:** If a field in the `Chore` model changes on the server, the mobile app fails to compile until it's updated.
- **Zero-overhead serialization:** Both sides use `kotlinx.serialization`, making the networking layer extremely lightweight and efficient.

### 3. Native Performance, Shared Effort
Unlike Flutter or React Native, KMP allows me to stay "Native" where it matters. I get the performance of JVM on the server and the flexibility of native UI on mobile, while still reaping the 70%+ code reuse benefit usually reserved for hybrid frameworks.

### 4. Developer Velocity & Maintainability
Using one language (Kotlin) and one concurrency model (Coroutines) across the entire stack drastically reduces context switching. This allows me to act as a **True Full-Stack Engineer**, maintaining the end-to-end lifecycle of a feature from the database schema to the UI state.

---

## 🏗 Architecture

The project follows **Clean Architecture** principles to ensure testability and scalability:

- **`:server`**: Ktor application handling authentication, persistence, and API endpoints.
- **`:shared`**:
    - `commonMain`: Domain models, Use Cases (Chore rotation logic), and Repository interfaces.
    - `iosMain/androidMain`: Platform-specific drivers for SQLDelight and Http Engines.
- **`:composeApp`**: Declarative UI built with Compose Multiplatform, consuming ViewModels from the shared layer.

---

## 🌍 Vision: From Taipei to Prague
*JustWoo was born out of a personal need to simplify roommate dynamics. It represents my journey as an Android Engineer evolving into a Full-Stack developer, pushing the boundaries of what Kotlin can do in the European tech landscape.*

---

**Contact / Portfolio**
Pin-Yun (Pollyanna) Wu - https://www.linkedin.com/in/pin-yun-wu/