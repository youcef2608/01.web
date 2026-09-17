package com.example.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HelpCall
import com.example.data.repository.HelpCallRepository
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapUiState(
    val calls: List<HelpCall> = emptyList(),
    val selectedCall: HelpCall? = null,
    val centerLatitude: Double = 36.7538,
    val centerLongitude: Double = 3.0588,
    val isActionInProgress: Boolean = false,
    val noticeMessage: String? = null
)

class MapViewModel(
    private val helpCallRepository: HelpCallRepository,
    private val volunteerRepository: VolunteerRepository
) : ViewModel() {

    private val _selectedCall = MutableStateFlow<HelpCall?>(null)
    private val _noticeMessage = MutableStateFlow<String?>(null)
    private val _isActionInProgress = MutableStateFlow(false)

    val uiState: StateFlow<MapUiState> = combine(
        helpCallRepository.helpCalls,
        _selectedCall,
        _noticeMessage,
        _isActionInProgress
    ) { calls, selected, notice, inProgress ->
        val currentSelected = if (selected != null) {
            calls.find { it.id == selected.id } ?: selected
        } else null
        MapUiState(
            calls = calls,
            selectedCall = currentSelected,
            noticeMessage = notice,
            isActionInProgress = inProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState()
    )

    fun selectCall(call: HelpCall?) {
        _selectedCall.value = call
    }

    fun volunteerForSelectedCall() {
        val target = _selectedCall.value ?: return
        val me = volunteerRepository.currentVolunteer.value
        viewModelScope.launch {
            _isActionInProgress.value = true
            helpCallRepository.acceptHelpCall(
                callId = target.id,
                volunteerId = me.id,
                helperName = me.name
            )
            _selectedCall.value = target.copy(status = com.example.data.model.HelpCallStatus.IN_PROGRESS)
            _noticeMessage.value = "تم قبول المساعدة في هذا النداء بنجاح!"
            _isActionInProgress.value = false
        }
    }

    fun clearNotice() {
        _noticeMessage.value = null
    }

    companion object {
        fun provideFactory(
            helpCallRepo: HelpCallRepository,
            volunteerRepo: VolunteerRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(helpCallRepo, volunteerRepo) as T
            }
        }
    }
}
