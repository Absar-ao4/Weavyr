package com.weavyr.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weavyr.model.UpdateProfileRequest // Added for the update function
import com.weavyr.model.User // Corrected import to use your actual User model
import com.weavyr.repository.UserRepository
import com.weavyr.screen.main.Researcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // --- 1. BACKEND & STATE ---

    private val userRepository = UserRepository()

    // Changed UserProfile to User
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Added this state to track when the save button is pressed in EditProfileScreen
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Fetch profile immediately when the ViewModel is created
    init {
        fetchMyProfile()
    }

    fun fetchMyProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // IMPORTANT: Ensure your UserRepository has a fetchProfile() function
                // that calls your GET "/fetchprofile" endpoint
                val response = userRepository.fetchProfile()

                if (response.isSuccessful) {
                    // Assuming your Retrofit response maps to a wrapper like {"user": {...}}
                    _userProfile.value = response.body()?.user
                } else {
                    _errorMessage.value = "Failed to load profile"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- NEW: Update Profile Function ---
    fun updateProfileData(request: UpdateProfileRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null
            try {
                // Call your existing repository function
                val response = userRepository.updateProfile(request)

                if (response.isSuccessful) {
                    // 🚀 CRITICAL: Re-fetch the profile so the MyProfile screen updates instantly!
                    fetchMyProfile()
                    onSuccess() // This will trigger navigation back
                } else {
                    _errorMessage.value = "Failed to update profile. Please try again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }


    // --- 2. TEMPORARY SWIPE/MATCH STORAGE (Existing) ---

    // Temporary in-memory storage (acts like backend for now)
    val bookmarkedProfiles = mutableStateListOf<Researcher>()
    val rejectedProfiles = mutableStateListOf<Researcher>()
    val watchedProfiles = mutableStateListOf<Researcher>()
    val connectionRequests = mutableStateListOf<Researcher>()

    fun addBookmark(profile: Researcher) {
        if (!bookmarkedProfiles.contains(profile)) {
            bookmarkedProfiles.add(profile)
        }
    }

    fun addConnectionRequest(profile: Researcher) {
        if (!connectionRequests.contains(profile)) {
            connectionRequests.add(profile)
        }
    }

    fun addRejected(profile: Researcher) {
        if (!rejectedProfiles.contains(profile)) {
            rejectedProfiles.add(profile)
        }
    }

    fun addWatched(profile: Researcher) {
        if (!watchedProfiles.contains(profile)) {
            watchedProfiles.add(profile)
        }
    }
}