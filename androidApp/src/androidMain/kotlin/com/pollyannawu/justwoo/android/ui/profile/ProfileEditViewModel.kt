package com.pollyannawu.justwoo.android.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.domain.usecase.profile.ObserveCurrentProfileUseCase
import com.pollyannawu.justwoo.domain.usecase.profile.UpdateCurrentProfileUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObserveAllTasksUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObserveProfileTasksInWindowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ProfileEditViewModel(
    private val observeAllTasks: ObserveAllTasksUseCase,
    private val observeProfileTasksInWindow: ObserveProfileTasksInWindowUseCase,
    private val observeCurrentProfile: ObserveCurrentProfileUseCase,
    private val updateCurrentProfile: UpdateCurrentProfileUseCase,
) : ViewModel() {

    companion object {
        const val NAME_LIMIT = 20
        const val BIO_LIMIT = 120
        const val BANK_ACCOUNT_LIMIT = 50
        private const val TAG = "ProfileEdit"
    }

    data class UiState(
        val name: String = "",
        val bio: String = "",
        val bankAccount: String = "",
        val nameError: String? = null,
        val bioError: String? = null,
        val bankAccountError: String? = null,
        val saved: Boolean = false,
        val saveError: String? = null,
        val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    ) {
        val canSave: Boolean get() = name.isNotBlank() && nameError == null && bioError == null && bankAccountError == null
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val allTasks: StateFlow<List<Task>> = observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tasksInWindow: StateFlow<List<Task>> = _uiState
        .map { it.selectedDate }
        .let { dates ->
            @Suppress("OPT_IN_USAGE")
            dates.flatMapLatest { anchor -> observeProfileTasksInWindow(anchor = anchor) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            val profile = observeCurrentProfile().firstOrNull()
            if (profile != null) {
                _uiState.update {
                    it.copy(name = profile.name, bio = profile.bio, bankAccount = profile.bankAccount)
                }
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update {
        it.copy(
            name = v,
            nameError = when {
                v.isBlank() -> "Name is required."
                v.length > NAME_LIMIT -> "Name cannot exceed $NAME_LIMIT characters."
                else -> null
            }
        )
    }

    fun onBioChange(v: String) = _uiState.update {
        it.copy(
            bio = v,
            bioError = if (v.length > BIO_LIMIT) "Bio cannot exceed $BIO_LIMIT characters." else null
        )
    }

    fun onBankAccountChange(v: String) = _uiState.update {
        it.copy(
            bankAccount = v,
            bankAccountError = if (v.length > BANK_ACCOUNT_LIMIT) "Bank account cannot exceed $BANK_ACCOUNT_LIMIT characters." else null
        )
    }

    fun selectDate(date: LocalDate) = _uiState.update { it.copy(selectedDate = date) }

    fun save() {
        val s = _uiState.value
        if (!s.canSave) return
        Log.d(TAG, "save → name=${s.name} bio=${s.bio} bankAccount=${s.bankAccount}")
        viewModelScope.launch {
            val success = updateCurrentProfile(s.name, s.bio, s.bankAccount)
            if (success) {
                Log.d(TAG, "save ← success")
                _uiState.update { it.copy(saved = true) }
            } else {
                Log.w(TAG, "save ← failed")
                _uiState.update { it.copy(saveError = "Something went wrong. Please try again later.") }
            }
        }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }

    fun consumeSaveError() = _uiState.update { it.copy(saveError = null) }
}
