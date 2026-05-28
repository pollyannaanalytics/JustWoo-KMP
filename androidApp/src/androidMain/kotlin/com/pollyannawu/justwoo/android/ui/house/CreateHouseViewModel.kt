package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.domain.usecase.house.CreateHouseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateHouseViewModel(
    private val createHouseUseCase: CreateHouseUseCase,
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val nameError: String? = null,
    )

    sealed interface Event {
        data object NavigateToHome : Event
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun submit() {
        val name = _uiState.value.name.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "House name cannot be empty.") }
            return
        }
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                createHouseUseCase(name, "")
                _events.send(Event.NavigateToHome)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = e.message ?: "Failed to create house.")
                }
            }
        }
    }
}
