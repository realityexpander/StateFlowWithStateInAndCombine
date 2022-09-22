package com.realityexpander.stateflowwithstateinandcombine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class UserViewModel: ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _localUser = MutableStateFlow<User?>(null)
    val localUser = _localUser.asStateFlow()


    // Automatic updating of localUser2
    //  - Using stateIn to get the latest value of the flow
    val users2 = _users.asStateFlow()
    val localUser2 = users2.map { users ->
        users.find { user ->
            user.id == "local"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)



    // Simulate a user being added to the list
    fun onUserJoined(user: User) {
        _users.update {
            it + user
        }

        // Manual updating of localUser
        // Note: must manually update localUser (and remember to do it in all places)
        // **THIS IS NOT NEEDED WHEN USING THE STATEIN SOLUTION**
        if(user.id == "local") {
            _localUser.update {
                user
            }
        }
    }

    // Simulate a user updating his information (like change isOnline)
    fun onUserInfoUpdated(user: User) {
        _users.update {
            it.map { curUser ->
                if(curUser.id == user.id)
                    user
                else
                    curUser
            }
        }

        // Manual updating of localUser
        // Note: must manually update localUser (and remember to do it in all places)
        // **THIS IS NOT NEEDED WHEN USING THE STATEIN SOLUTION**
        if(user.id == "local") {
            _localUser.update {
                user
            }
        }
    }

}

data class User(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isOnline: Boolean = false
)