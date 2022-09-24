package com.realityexpander.stateflowwithstateinandcombine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import java.util.*

class ChatViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()


    val chatState =
        combine(isLoggedIn, chatMessages, users) { isLoggedIn, chatMessages, users ->
            println(
                "combine executed: \n" +
                        "  isLoggedIn: $isLoggedIn, \n" +
                        "  users: ${users.joinToString { it.name }}\n" +
                        "  chatMessages: ${chatMessages.joinToString { it.fromUser.name + " -> " + it.message + "\n  " }}"
            )

            if (isLoggedIn) {
                ChatState(
                    userPreviews = users.map { curUser ->

                        // Create the preview for the curUser
                        UserPreview(
                            user = curUser,
                            lastMessage = chatMessages
                                .filter { message ->
                                    message.fromUser == curUser
                                }
                                .maxByOrNull { message ->
                                    message.time
                                }
                                ?.message,
                            lastMessageTime = chatMessages
                                .filter { message ->
                                    message.fromUser == curUser
                                }
                                .maxByOrNull { message ->
                                    message.time
                                }
                                ?.time
                        )
                    },
                    headerTitle = ("Last chat user: " + (users.lastOrNull()?.name ?: "No user")),
                )
            } else
                null

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    val chatUsers = combine(_isLoggedIn, _users) { isLoggedIn, users ->
        println("  isLoggedIn: $isLoggedIn, users: ${users.joinToString { it.name }}")

        ChatUsers(
            userNames = users.map { curUser ->
                curUser.name
            },
            headerTitle = ("First User: " + users.firstOrNull()?.name),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    fun onUserJoined(user: User) {
        _users.update {
            it + user
        }
    }

    fun onUserMessageReceived(message: ChatMessage) {
        _chatMessages.update {
            it + message
        }
    }

    fun onLogin() {
        _isLoggedIn.update { true }

    }

    fun onLogout() {
        _isLoggedIn.update { false }
    }

}

data class ChatState(
    val userPreviews: List<UserPreview>,
    val headerTitle: String,
)

data class ChatUsers(
    val userNames: List<String>?,
    val headerTitle: String,
)

data class UserPreview(
    val user: User,
    val lastMessage: String?,
    val lastMessageTime: Long?,
)

data class ChatMessage(
    val fromUser: User,
    val message: String,
    val time: Long = System.currentTimeMillis(),
)

