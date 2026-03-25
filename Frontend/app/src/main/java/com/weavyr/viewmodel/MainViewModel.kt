package com.weavyr.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.weavyr.model.Researcher
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.User
import com.weavyr.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainViewModel : ViewModel() {

    private val userRepository = UserRepository()

    /* ---------------- PROFILE STATE ---------------- */

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ⭐ ADDED THIS FUNCTION TO CLEAR LINGERING ERRORS ⭐
    fun clearError() {
        _errorMessage.value = null
    }

    var hasSeenTutorial by mutableStateOf(false)

    /* ---------------- DISCOVER DECK ---------------- */

    private val _allResearchers = MutableStateFlow<List<Researcher>>(emptyList())
    val allResearchers: StateFlow<List<Researcher>> = _allResearchers.asStateFlow()

    private val _isDeckLoading = MutableStateFlow(true)
    val isDeckLoading: StateFlow<Boolean> = _isDeckLoading.asStateFlow()

    /* ---------------- LIST STATES ---------------- */

    val bookmarkedProfiles = mutableStateListOf<Researcher>()
    val rejectedProfiles = mutableStateListOf<Researcher>()
    val connectionRequests = mutableStateListOf<Researcher>()
    val incomingRequests = mutableStateListOf<Researcher>()
    val matchedResearchers = mutableStateListOf<Researcher>()

    /* ---------------- MATCH EVENT ---------------- */

    private val _matchEvent = MutableStateFlow<Researcher?>(null)
    val matchEvent: StateFlow<Researcher?> = _matchEvent.asStateFlow()

    init {
        refreshAppData()
    }

    /* ---------------- INITIAL LOAD ---------------- */

    fun refreshAppData() {
        fetchMatches()
        fetchMyProfile()
        fetchDiscoverDeck()
        fetchMyBookmarks()
        fetchIncomingRequests()
        fetchSentRequests()
        fetchRejectedProfiles()
    }

    /* ---------------- DISCOVER ---------------- */

    fun fetchDiscoverDeck() {
        viewModelScope.launch {

            _isDeckLoading.value = true

            try {

                val response = userRepository.getDiscoverFeed()

                if (response.isSuccessful) {

                    val users = response.body()?.recommendations ?: emptyList()

                    _allResearchers.value =
                        users.map { mapToResearcher(it) }

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

    /* ---------------- MATCHES ---------------- */

    fun fetchMatches() {

        viewModelScope.launch {

            try {

                val response = userRepository.getMatches()

                if (response.isSuccessful) {

                    val matches =
                        response.body()?.collaborations ?: emptyList()

                    matchedResearchers.clear()

                    matchedResearchers.addAll(
                        matches.map { mapToResearcher(it.user) }
                    )

                } else {
                    _errorMessage.value = "Failed to load collaborations"
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while loading collaborations"
            }
        }
    }

    /* ---------------- SWIPES ---------------- */

    fun addConnectionRequest(profile: Researcher) {

        val alreadyLikedMe =
            incomingRequests.any { it.id == profile.id }

        if (alreadyLikedMe) {

            incomingRequests.removeAll { it.id == profile.id }

            if (matchedResearchers.none { it.id == profile.id }) {
                matchedResearchers.add(profile)
            }

            _matchEvent.value = profile

        } else {

            if (connectionRequests.none { it.id == profile.id }) {
                connectionRequests.add(profile)
            }
        }

        _allResearchers.value =
            _allResearchers.value.filter { it.id != profile.id }

        viewModelScope.launch {

            try {

                val response =
                    userRepository.recordSwipe(profile.id, "LIKE")

                if (response.isSuccessful && response.body()?.isMatch == true) {

                    if (matchedResearchers.none { it.id == profile.id }) {

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

        if (rejectedProfiles.none { it.id == profile.id }) {
            rejectedProfiles.add(profile)
        }

        _allResearchers.value =
            _allResearchers.value.filter { it.id != profile.id }

        viewModelScope.launch {
            try {
                userRepository.recordSwipe(profile.id, "REJECT")
            } catch (_: Exception) { }
        }
    }

    fun undoRejection(profile: Researcher) {
        rejectedProfiles.remove(profile)
        viewModelScope.launch {
            try {
                val response = userRepository.undoRejection(profile.id)

                if (!response.isSuccessful) {
                    _errorMessage.value = "Failed to undo rejection on server."
                    rejectedProfiles.add(profile)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error while undoing rejection"
            }
        }
    }

    /* ---------------- REQUESTS ---------------- */

    fun fetchIncomingRequests() {

        viewModelScope.launch {

            try {

                val response =
                    userRepository.getIncomingRequests()

                if (response.isSuccessful) {

                    val users =
                        response.body()?.requests ?: emptyList()

                    incomingRequests.clear()

                    incomingRequests.addAll(
                        users.map { mapToResearcher(it) }
                    )
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Could not load incoming requests"
            }
        }
    }

    fun fetchSentRequests() {

        viewModelScope.launch {

            try {

                val response =
                    userRepository.getSentRequests()

                if (response.isSuccessful) {

                    val users =
                        response.body()?.sent ?: emptyList()

                    connectionRequests.clear()

                    connectionRequests.addAll(
                        users.map { mapToResearcher(it) }
                    )
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Could not load sent requests"
            }
        }
    }

    fun fetchRejectedProfiles() {

        viewModelScope.launch {

            try {

                val response =
                    userRepository.getRejectedProfiles()

                if (response.isSuccessful) {

                    val users =
                        response.body()?.rejected ?: emptyList()

                    rejectedProfiles.clear()

                    rejectedProfiles.addAll(
                        users.map { mapToResearcher(it) }
                    )
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Could not load rejected profiles"
            }
        }
    }

    /* ---------------- BOOKMARKS ---------------- */

    fun fetchMyBookmarks() {

        viewModelScope.launch {

            try {

                val response =
                    userRepository.fetchBookmarks()

                if (response.isSuccessful) {

                    val users =
                        response.body()?.profileBookmarks ?: emptyList()

                    bookmarkedProfiles.clear()

                    bookmarkedProfiles.addAll(
                        users.map { mapToResearcher(it) }
                    )
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while fetching bookmarks"
            }
        }
    }

    fun addBookmark(profile: Researcher) {

        if (bookmarkedProfiles.none { it.id == profile.id }) {
            bookmarkedProfiles.add(profile)
        }

        _allResearchers.value =
            _allResearchers.value.filter { it.id != profile.id }

        viewModelScope.launch {

            try {

                val response =
                    userRepository.addBookmark(profile.id)

                if (!response.isSuccessful) {
                    _errorMessage.value =
                        "Could not save bookmark"
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while saving bookmark"
            }
        }
    }

    fun removeBookmark(profile: Researcher) {

        bookmarkedProfiles.removeAll { it.id == profile.id }

        viewModelScope.launch {

            try {
                userRepository.removeBookmark(profile.id)
            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error while removing bookmark"
            }
        }
    }

    /* ---------------- PROFILE ---------------- */

    fun fetchMyProfile() {

        viewModelScope.launch {

            _isLoading.value = true

            try {

                val response =
                    userRepository.fetchProfile()

                if (response.isSuccessful) {
                    _userProfile.value = response.body()?.user
                }

            } catch (e: Exception) {
                _errorMessage.value =
                    "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

                if (imageUri != null &&
                    !imageUri.toString().startsWith("http")
                ) {

                    val uploadedUrl =
                        uploadImageToCloudinary(imageUri)

                    if (uploadedUrl != null) {
                        finalRequest =
                            finalRequest.copy(profilePhoto = uploadedUrl)
                    }
                }

                val response =
                    userRepository.updateProfile(finalRequest)

                if (response.isSuccessful) {

                    fetchMyProfile()
                    onSuccess()

                } else {

                    val backendError =
                        response.errorBody()?.string()

                    _errorMessage.value =
                        "Backend Error: $backendError"
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    "Network error: ${e.message}"

            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearMatch() {
        _matchEvent.value = null
    }

    /* ---------------- VIEWING OTHER PROFILES ---------------- */

    private val _viewedUserProfile = MutableStateFlow<User?>(null)
    val viewedUserProfile: StateFlow<User?> = _viewedUserProfile.asStateFlow()

    private val _isViewedUserLoading = MutableStateFlow(false)
    val isViewedUserLoading: StateFlow<Boolean> = _isViewedUserLoading.asStateFlow()

    fun fetchOtherUserProfile(userId: Int) {
        viewModelScope.launch {
            _isViewedUserLoading.value = true
            try {
                val response = userRepository.getUserProfileById(userId)
                if (response.isSuccessful && response.body() != null) {
                    _viewedUserProfile.value = response.body()?.user
                } else {
                    _errorMessage.value = "Failed to fetch user's profile."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network Error: ${e.message}"
            } finally {
                _isViewedUserLoading.value = false
            }
        }
    }

    // Call this when pressing "Back" to clear the screen
    fun clearViewedProfile() {
        _viewedUserProfile.value = null
    }

    /* ---------------- CLOUDINARY ---------------- */

    private suspend fun uploadImageToCloudinary(uri: Uri): String? {

        return suspendCancellableCoroutine { continuation ->

            MediaManager.get().upload(uri)
                .unsigned("user_profiles")
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String) {}

                    override fun onProgress(
                        requestId: String,
                        bytes: Long,
                        totalBytes: Long
                    ) {}

                    override fun onReschedule(
                        requestId: String,
                        error: ErrorInfo
                    ) {}

                    override fun onSuccess(
                        requestId: String,
                        resultData: Map<*, *>
                    ) {

                        val url =
                            resultData["secure_url"] as? String

                        continuation.resume(url)
                    }

                    override fun onError(
                        requestId: String,
                        error: ErrorInfo
                    ) {

                        continuation.resume(null)
                    }
                })
                .dispatch()
        }
    }
    /* ---------------- MAPPER ---------------- */
    private fun mapToResearcher(user: User): Researcher {
        return Researcher(
            id = user.id,
            username=user.username,
            name=user.name?: "Unknown",
            organization=user.organization ?: "Independent Researcher",
            field=user.field ?: "General Research",
            interests=user.interests ?: emptyList(),
            papers=user.numberOfPapers ?: 0,
            citations=user.totalCitations ?: 0,
            experienceYears=user.experienceYears ?: 0,
            achievements=user.achievements?.map { it.title } ?: emptyList(),
            profilePhoto=user.profilePhoto,
            roles=user.roles?:emptyList()
        )
    }
}