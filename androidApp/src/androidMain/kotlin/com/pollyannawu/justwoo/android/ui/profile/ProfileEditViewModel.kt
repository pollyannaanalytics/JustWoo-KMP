package com.pollyannawu.justwoo.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.domain.usecase.task.ObserveAllTasksUseCase
import com.pollyannawu.justwoo.domain.usecase.task.ObserveProfileTasksInWindowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Profile edit — matches the Figma variants: edit 姓名 / bio / hashtags
 * with character-limit error states.
 *
 * NOTE: the shared module doesn't yet expose a ProfileRepository.write() API;
 * when it does, inject it here and wire submit() to it. For now this screen
 * is pure form state.
 */
class ProfileEditViewModel(
    private val observeAllTasks: ObserveAllTasksUseCase,
    private val observeProfileTasksInWindow: ObserveProfileTasksInWindowUseCase,
) : ViewModel() {

    companion object {
        const val NAME_LIMIT = 20
        const val BIO_LIMIT = 120
        const val HASHTAG_LIMIT = 5
    }

    data class UiState(
        val name: String = "",
        val bio: String = "",
        val hashtags: List<String> = emptyList(),
        val newHashtag: String = "",
        val nameError: String? = null,
        val bioError: String? = null,
        val hashtagError: String? = null,
        val saved: Boolean = false,
        val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** All cached tasks — feeds the calendar grid's per-day markers. */
    val allTasks: StateFlow<List<Task>> = observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Tasks within the 7-day window ending on the currently selected date.
     * Re-subscribes whenever `selectedDate` changes.
     */
    val tasksInWindow: StateFlow<List<Task>> = _uiState
        .map { it.selectedDate }
        .let { dates ->
            @Suppress("OPT_IN_USAGE")
            dates.flatMapLatest { anchor -> observeProfileTasksInWindow(anchor = anchor) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onNameChange(v: String) = _uiState.update {
        it.copy(
            name = v,
            nameError = if (v.length > NAME_LIMIT) "Name cannot exceed $NAME_LIMIT characters." else null
        )
    }

    fun onBioChange(v: String) = _uiState.update {
        it.copy(
            bio = v,
            bioError = if (v.length > BIO_LIMIT) "Bio cannot exceed $BIO_LIMIT characters." else null
        )
    }

    fun onNewHashtagChange(v: String) = _uiState.update { it.copy(newHashtag = v, hashtagError = null) }

    fun addHashtag() = _uiState.update { current ->
        val tag = current.newHashtag.trim().trimStart('#')
        when {
            tag.isEmpty() -> current
            current.hashtags.size >= HASHTAG_LIMIT ->
                current.copy(hashtagError = "Up to $HASHTAG_LIMIT hashtags.")
            current.hashtags.contains(tag) ->
                current.copy(hashtagError = "Hashtag already added.")
            else -> current.copy(hashtags = current.hashtags + tag, newHashtag = "", hashtagError = null)
        }
    }

    fun removeHashtag(tag: String) = _uiState.update {
        it.copy(hashtags = it.hashtags - tag)
    }

    fun selectDate(date: LocalDate) = _uiState.update { it.copy(selectedDate = date) }

    fun save() {
        val s = _uiState.value
        if (s.nameError != null || s.bioError != null) return
        // TODO: wire to ProfileRepository when it exists in shared.
        _uiState.update { it.copy(saved = true) }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }
}
