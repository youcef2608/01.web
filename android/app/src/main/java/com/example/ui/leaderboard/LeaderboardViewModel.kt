package com.example.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Volunteer
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class LeaderboardTab(val titleArabic: String) {
    NEIGHBORHOOD("صدارة الحي"),
    STATE("صدارة الولاية"),
    MONTHLY("صدارة الشهر")
}

data class LeaderboardUiState(
    val selectedTab: LeaderboardTab = LeaderboardTab.NEIGHBORHOOD,
    val volunteers: List<Volunteer> = emptyList(),
    val currentUser: Volunteer? = null
)

class LeaderboardViewModel(
    private val volunteerRepository: VolunteerRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LeaderboardTab.NEIGHBORHOOD)

    val uiState: StateFlow<LeaderboardUiState> = combine(
        _selectedTab,
        volunteerRepository.neighborhoodLeaderboard,
        volunteerRepository.stateLeaderboard,
        volunteerRepository.monthlyLeaderboard,
        volunteerRepository.currentVolunteer
    ) { tab, neighborhood, state, monthly, me ->
        val list = when (tab) {
            LeaderboardTab.NEIGHBORHOOD -> neighborhood
            LeaderboardTab.STATE -> state
            LeaderboardTab.MONTHLY -> monthly
        }
        LeaderboardUiState(
            selectedTab = tab,
            volunteers = list,
            currentUser = me
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeaderboardUiState()
    )

    fun selectTab(tab: LeaderboardTab) {
        _selectedTab.value = tab
    }

    companion object {
        fun provideFactory(volunteerRepo: VolunteerRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LeaderboardViewModel(volunteerRepo) as T
                }
            }
    }
}
