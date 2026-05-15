package com.pollyannawu.justwoo.nav

/**
 * One per-feature interface for every destination the navigation root can
 * reach. Each is intentionally a thin "navigation marker" — it exposes the
 * data the destination needs to identify itself plus the user actions that
 * can move the stack. Business logic still lives in the platform's native
 * ViewModel; these interfaces are what SwiftUI / Compose call into when the
 * user taps something that should change the back stack.
 */

interface SignInComponent {
    fun onSignInSuccess()
    fun onNavigateToRegister()
}

interface RegisterComponent {
    fun onRegisterSuccess()
    fun onNavigateToSignIn()
}

interface HomeComponent {
    val userId: Long
    val houseId: Long

    fun onCreateTask()
    fun onOpenTaskSpace()
    fun onOpenCalendar()
    fun onOpenProfile()
    fun onOpenMenu()
}

interface CreateTaskComponent {
    val userId: Long
    val houseId: Long

    fun onClose()
    fun onOpenProfile()
}

interface TaskExplorationComponent {
    val userId: Long
    val houseId: Long

    fun onClose()
    fun onOpenProfile()
}

interface CalendarComponent {
    fun onClose()
    fun onOpenTask(taskId: Long)
}

interface ProfileEditComponent {
    fun onClose()
}
