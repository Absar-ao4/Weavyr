package com.weavyr.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.weavyr.model.Researcher
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.User
import com.weavyr.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainViewModel : ViewModel() {

    private val userRepository = UserRepository()

    // --- Profile & UI State ---
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var hasSeenTutorial by mutableStateOf(false)

    // --- Discovery Deck State ---
    private val _allResearchers = MutableStateFlow<List<Researcher>>(emptyList())
    val allResearchers: StateFlow<List<Researcher>> = _allResearchers.asStateFlow()

    private val _isDeckLoading = MutableStateFlow(true)
    val isDeckLoading: StateFlow<Boolean> = _isDeckLoading.asStateFlow()

    // --- Interactive UI Lists ---
    val bookmarkedProfiles = mutableStateListOf<Researcher>()
    val rejectedProfiles = mutableStateListOf<Researcher>()

    // Requests YOU sent
    val connectionRequests = mutableStateListOf<Researcher>()

    // Requests YOU received
    val incomingRequests = mutableStateListOf<Researcher>()

    // Matched collaborators
    val matchedResearchers = mutableStateListOf<Researcher>()

    // --- MATCH EVENT (for popup animation etc.) ---
    private val _matchEvent = MutableStateFlow<Researcher?>(null)
    val matchEvent: StateFlow<Researcher?> = _matchEvent.asStateFlow()


    init {
        refreshAppData()
    }

    /**
     * Helper to refresh all initial data
     */
    fun refreshAppData() {
        fetchMatches()
        fetchMyProfile()
        fetchDiscoverDeck()
        fetchMyBookmarks()

        // Fetch all 3 swipe lists from backend!
        fetchIncomingRequests()
        fetchSentRequests()
        fetchRejectedProfiles()
    }


    fun fetchDiscoverDeck() {
        viewModelScope.launch {
            _isDeckLoading.value = true
            _errorMessage.value = null
            try {
                val response = userRepository.getDiscoverFeed()
                if (response.isSuccessful) {
                    val remoteUsers = response.body()?.recommendations ?: emptyList()
                    _allResearchers.value = remoteUsers.map { mapToResearcher(it) }
                } else {
                    _errorMessage.value = "Failed to load discovery feed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isDeckLoading.value = false
            }
        }
    }

    fun fetchMatches() {
        viewModelScope.launch {
            try {
                val response = userRepository.getMatches()

                if (response.isSuccessful) {
                    val matches = response.body()?.collaborations ?: emptyList()

                    matchedResearchers.clear()

                    matchedResearchers.addAll(
                        // Matches uses nested user object based on Collaboration API
                        matches.map { mapToResearcher(it.user) }
                    )

                } else {
                    _errorMessage.value = "Failed to load collaborations."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error while loading collaborations."
            }
        }
    }

    // When YOU swipe right
    fun addConnectionRequest(profile: Researcher) {

        // 1. Optimistic UI Update & Local Match Detection
        val alreadyLikedMe = incomingRequests.any { it.id == profile.id }

        if (alreadyLikedMe) {
            // ⭐ IT'S A MATCH! Update UI Instantly ⭐
            incomingRequests.removeAll { it.id == profile.id }

            if (!matchedResearchers.any { it.id == profile.id }) {
                matchedResearchers.add(profile)
            }

            _matchEvent.value = profile
        } else {
            // Just a normal sent request
            if (!connectionRequests.any { it.id == profile.id }) {
                connectionRequests.add(profile)
            }
        }

        // Remove from the Discover deck locally so they disappear immediately
        _allResearchers.value = _allResearchers.value.filter { it.id != profile.id }

        // 2. Tell the Backend (Required to save the state on the server)
        viewModelScope.launch {
            try {
                val response = userRepository.recordSwipe(profile.id, "LIKE")

                // If the backend confirms a match that we missed locally
                // (e.g., they swiped us 5 seconds ago while we were online)
                if (response.isSuccessful && response.body()?.isMatch == true) {
                    if (!matchedResearchers.any { it.id == profile.id }) {
                        matchedResearchers.add(profile)
                        _matchEvent.value = profile
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to record swipe"
            }
        }
    }

    fun addRejected(profile: Researcher) {

        if (!rejectedProfiles.contains(profile))
            rejectedProfiles.add(profile)

        _allResearchers.value =
            _allResearchers.value.filter { it.id != profile.id }

        viewModelScope.launch {
            try {
                userRepository.recordSwipe(profile.id, "REJECT")
            } catch (_: Exception) { }
        }
    }

    // --- INCOMING REQUESTS ---

    fun fetchIncomingRequests() {
        viewModelScope.launch {
            try {
                val response = userRepository.getIncomingRequests()
                if (response.isSuccessful) {
                    val incoming = response.body()?.requests ?: emptyList()
                    incomingRequests.clear()

                    // Backend sends flattened User objects directly, so we just map 'it'
                    incomingRequests.addAll(
                        incoming.map { mapToResearcher(it) }
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Could not load incoming requests."
            }
        }
    }

    // --- SENT REQUESTS ---

    fun fetchSentRequests() {
        viewModelScope.launch {
            try {
                val response = userRepository.getSentRequests()
                if (response.isSuccessful) {
                    val sent = response.body()?.sent ?: emptyList()
                    connectionRequests.clear()
                    connectionRequests.addAll(sent.map { mapToResearcher(it) })
                }
            } catch (e: Exception) {
                _errorMessage.value = "Could not load sent requests."
            }
        }
    }

    // --- REJECTED PROFILES ---

    fun fetchRejectedProfiles() {
        viewModelScope.launch {
            try {
                val response = userRepository.getRejectedProfiles()
                if (response.isSuccessful) {
                    val rejected = response.body()?.rejected ?: emptyList()
                    rejectedProfiles.clear()
                    rejectedProfiles.addAll(rejected.map { mapToResearcher(it) })
                }
            } catch (e: Exception) {
                _errorMessage.value = "Could not load rejected profiles."
            }
        }
    }

    // Accept collaborator request from the "Requests" tab (if you bring it back)
    fun acceptRequest(profile: Researcher) {

        incomingRequests.remove(profile)

        if (!matchedResearchers.contains(profile))
            matchedResearchers.add(profile)

        _matchEvent.value = profile

        // Tell the backend we matched
        viewModelScope.launch {
            try {
                userRepository.recordSwipe(profile.id, "LIKE")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to accept request."
            }
        }
    }

    // Reject collaborator request
    fun rejectRequest(profile: Researcher) {

        incomingRequests.remove(profile)
        rejectedProfiles.add(profile) // Add to local rejected list

        // Tell the backend we rejected
        viewModelScope.launch {
            try {
                userRepository.recordSwipe(profile.id, "REJECT")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reject request."
            }
        }
    }

    // --- BOOKMARKS ---

    fun fetchMyBookmarks() {
        viewModelScope.launch {
            try {
                val response = userRepository.fetchBookmarks()
                if (response.isSuccessful) {

                    val realBookmarks =
                        response.body()?.profileBookmarks ?: emptyList()

                    bookmarkedProfiles.clear()

                    bookmarkedProfiles.addAll(
                        realBookmarks.map { mapToResearcher(it) }
                    )
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while fetching bookmarks."
            }
        }
    }

    fun addBookmark(profile: Researcher) {

        if (!bookmarkedProfiles.contains(profile))
            bookmarkedProfiles.add(profile)

        _allResearchers.value =
            _allResearchers.value.filter { it.id != profile.id }

        viewModelScope.launch {
            try {
                val response = userRepository.addBookmark(profile.id)

                if (!response.isSuccessful)
                    _errorMessage.value =
                        "Could not save bookmark to server."

            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while saving bookmark."
            }
        }
    }

    fun removeBookmark(profile: Researcher) {

        bookmarkedProfiles.remove(profile)

        viewModelScope.launch {
            try {
                userRepository.removeBookmark(profile.id)
            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while removing bookmark."
            }
        }
    }

    // --- PROFILE MANAGEMENT ---

    fun fetchMyProfile() {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                val response = userRepository.fetchProfile()

                if (response.isSuccessful) {
                    _userProfile.value = response.body()?.user
                }

            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Handles text and image updates seamlessly
    fun updateProfileData(
        context: Context,
        imageUri: Uri?,
        request: UpdateProfileRequest,
        onSuccess: () -> Unit
    ) {

        viewModelScope.launch {

            _isUpdating.value = true

            try {
                var finalRequest = request

                // 1. If we have a new local image, upload it to Cloudinary directly
                if (imageUri != null && !imageUri.toString().startsWith("http")) {

                    val uploadedImageUrl = uploadImageToCloudinary(imageUri)

                    if (uploadedImageUrl != null) {
                        // Injects the returned secure URL into the profile update request
                        finalRequest = finalRequest.copy(profilePhoto = uploadedImageUrl)
                    } else {
                        _errorMessage.value = "Failed to upload image. Saving text only."
                    }
                }

                // 2. Proceed with updating the profile text data & the new photo URL via your backend
                val response = userRepository.updateProfile(finalRequest)

                if (response.isSuccessful) {
                    fetchMyProfile() // Refresh local state
                    onSuccess()
                } else {
                    // ⭐ CHANGED: Grab the exact error message from your backend
                    val backendError = response.errorBody()?.string()
                    _errorMessage.value = "Backend Error: $backendError"
                    println("API REJECTION: $backendError") // Prints to your Logcat
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Network error: ${e.message}"

            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearMatch() {
        _matchEvent.value = null
    }

    // ⭐ Cloudinary Upload Coroutine Helper
    private suspend fun uploadImageToCloudinary(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(uri)
                .unsigned("user_profiles") // Make sure this perfectly matches your Cloudinary Unsigned Preset Name!
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as? String
                        continuation.resume(secureUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(null) // Resume with null so the app doesn't crash on failure
                    }
                })
                .dispatch()
        }
    }

    // --- MAPPING UTILITY ---

    private fun mapToResearcher(user: User): Researcher {

        return Researcher(
            id = user.id,
            name = user.name ?: "Unknown",
            organization = user.organization ?: "Independent Researcher",
            field = user.field ?: "General Research",
            interests = user.interests ?: emptyList(),
            papers = user.numberOfPapers ?: 0,
            citations = user.totalCitations ?: 0,
            experienceYears = user.experienceYears ?: 0,
            achievements = user.achievements?.map { it.title } ?: emptyList()
        )
    }
}