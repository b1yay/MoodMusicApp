package com.example.moodmusicapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _currentUserName = MutableStateFlow<String>("User")
    val currentUserName = _currentUserName.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val user = auth.currentUser
        if (user != null) {
            _currentUserName.value = user.displayName ?: user.email?.substringBefore("@") ?: "User"
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signUp(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Save display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                _currentUserName.value = name
                                _authState.value = AuthState.Authenticated
                            } else {
                                _authState.value = AuthState.Error(
                                    profileTask.exception?.message ?: "Profile update failed"
                                )
                            }
                        }
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Sign up failed"
                    )
                }
            }
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _currentUserName.value = user?.displayName 
                        ?: user?.email?.substringBefore("@") 
                        ?: "User"
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Sign in failed"
                    )
                }
            }
    }

    fun signOut() {
        MediaManager.stop()
        YouTubePlayer.stop()
        MusicPlayer.stop()
        viewModelScope.launch {
            _authState.value = AuthState.LoggingOut
            delay(1500)
            auth.signOut()
            _currentUserName.value = "User"
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun updateDisplayName(newName: String) {
        _authState.value = AuthState.Loading
        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUserName.value = newName
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Update failed"
                    )
                }
            }
    }

    fun sendPasswordReset() {
        val email = auth.currentUser?.email ?: return
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Error("Reset link sent to $email")
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Failed to send reset email"
                    )
                }
            }
    }

    fun deleteAccount() {
        _authState.value = AuthState.Loading
        auth.currentUser?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Unauthenticated
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Delete failed"
                    )
                }
            }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object LoggingOut : AuthState()
    data class Error(val message: String) : AuthState()
}
