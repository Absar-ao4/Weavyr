package com.weavyr.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.User
import com.weavyr.repository.UserRepository
import com.weavyr.model.Researcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var hasSeenTutorial by mutableStateOf(false)

    private val _allResearchers = MutableStateFlow<List<Researcher>>(emptyList())
    val allResearchers: StateFlow<List<Researcher>> = _allResearchers.asStateFlow()

    private val _isDeckLoading = MutableStateFlow(true)
    val isDeckLoading: StateFlow<Boolean> = _isDeckLoading.asStateFlow()

    // Interactive UI Lists
    val bookmarkedProfiles = mutableStateListOf<Researcher>()
    val rejectedProfiles = mutableStateListOf<Researcher>()
    val connectionRequests = mutableStateListOf<Researcher>()

    init {
        fetchMyProfile()
        fetchDiscoverDeck()
    }

    // --- TEMPORARY MOCK FETCH FOR FRONTEND TESTING ---
    fun fetchDiscoverDeck() {
        viewModelScope.launch {
            _isDeckLoading.value = true

            delay(1000)

            val dummyProfiles = listOf(
                Researcher(
                    id = 101,
                    name = "Dr. Alice Smith",
                    organization = "MIT Media Lab",
                    field = "Human-Computer Interaction",
                    interests = listOf("UX", "Accessibility", "AR/VR"),
                    papers = 24,
                    citations = 1200, // Visionary
                    experienceYears = 8,
                    achievements = listOf("Best Paper CHI 2023", "NSF Grant Winner")
                ),
                Researcher(
                    id = 102,
                    name = "James Chen",
                    organization = "Stanford University",
                    field = "Machine Learning",
                    interests = listOf("NLP", "LLMs", "AI Ethics"),
                    papers = 12,
                    citations = 340, // Architect
                    experienceYears = 4,
                    achievements = listOf("NeurIPS Contributor")
                ),
                Researcher(
                    id = 103,
                    name = "Priya Sharma",
                    organization = "IISc Bangalore",
                    field = "Quantum Computing",
                    interests = listOf("Cryptography", "Algorithms"),
                    papers = 8,
                    citations = 150, // Innovator
                    experienceYears = 3,
                    achievements = emptyList()
                )
            )

            // Filter out any you've already interacted with
            _allResearchers.value = dummyProfiles.filter { profile ->
                !connectionRequests.contains(profile) &&
                        !rejectedProfiles.contains(profile) &&
                        !bookmarkedProfiles.contains(profile)
            }

            _isDeckLoading.value = false
        }
    }

    // --- FETCH BOOKMARKS FROM BACKEND (REAL) ---
    fun fetchMyBookmarks() {
        viewModelScope.launch {
            try {
                val response = userRepository.fetchBookmarks()
                if (response.isSuccessful) {
                    val realBookmarks = response.body()?.profileBookmarks ?: emptyList()

                    val mappedBookmarks = realBookmarks.map { user ->
                        Researcher(
                            id = user.id,
                            name = user.name ?: "Unknown",
                            organization = user.organization ?: "Independent",
                            field = user.field ?: "General",
                            interests = user.interests?.map { it.name } ?: emptyList(),
                            papers = user.numberOfPapers ?: 0,
                            citations = user.totalCitations ?: 0,
                            experienceYears = user.experienceYears ?: 0,
                            achievements = user.achievements?.map { it.title } ?: emptyList()
                        )
                    }

                    bookmarkedProfiles.clear()
                    bookmarkedProfiles.addAll(mappedBookmarks)
                } else {
                    _errorMessage.value = "Failed to load bookmarks"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            }
        }
    }

    // --- FETCH PROFILE (REAL) ---
    fun fetchMyProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = userRepository.fetchProfile()

                if (response.isSuccessful) {
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

    // --- UPDATE PROFILE (REAL) ---
    fun updateProfileData(request: UpdateProfileRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null
            try {
                val response = userRepository.updateProfile(request)

                if (response.isSuccessful) {
                    fetchMyProfile()
                    onSuccess()
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

    // --- SWIPE / BOOKMARK UI ACTIONS ---
    fun addBookmark(profile: Researcher) {
        if (!bookmarkedProfiles.contains(profile)) {
            bookmarkedProfiles.add(profile)
        }

        // Restored: Remove from the main deck instantly so they can decide later!
        _allResearchers.value = _allResearchers.value.filter { it.id != profile.id }

        viewModelScope.launch {
            try {
                val response = userRepository.addBookmark(profile.id)
                if (!response.isSuccessful) {
                    _errorMessage.value = "Could not save bookmark to server."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error while saving bookmark."
            }
        }
    }

    fun removeBookmark(profile: Researcher) {
        bookmarkedProfiles.remove(profile)

        viewModelScope.launch {
            try {
                userRepository.removeBookmark(profile.id)
            } catch (e: Exception) {
                _errorMessage.value = "Network error while removing bookmark."
            }
        }
    }

    fun addConnectionRequest(profile: Researcher) {
        if (!connectionRequests.contains(profile)) connectionRequests.add(profile)
        _allResearchers.value = _allResearchers.value.filter { it.id != profile.id }
    }

    fun addRejected(profile: Researcher) {
        if (!rejectedProfiles.contains(profile)) rejectedProfiles.add(profile)
        _allResearchers.value = _allResearchers.value.filter { it.id != profile.id }
    }
}