package com.realityexpander.stateflowwithstateinandcombine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

// Based on these ideas:
// https://veldan1202.medium.com/kotlin-setup-sharedflow-31debf613b91

class ChatViewModel2 : ViewModel() {

    private val _isLoggedIn =
        MutableSharedFlow<Boolean>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val isLoggedIn2 = _isLoggedIn.asSharedFlow()

    private val _chatMessages =
        MutableSharedFlow<List<ChatMessage>>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val chatMessages2 = _chatMessages.asSharedFlow()

    private val _users =
        MutableSharedFlow<List<User>>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val users2 = _users.asSharedFlow()


    val chatState2 =
        combine(isLoggedIn2, chatMessages2, users2) {
                isLoggedIn,
                chatMessages,
                users ->

            println("combine executed: \n" +
                    "  isLoggedIn2: $isLoggedIn, \n" +
                    "  users2: ${users.joinToString { it.name }}\n" +
                    "  chatMessages2: ${chatMessages.joinToString { it.fromUser.name +" -> "+it.message + "\n  " }}"
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

    val chatUsers = combine(isLoggedIn2, users2) { isLoggedIn, users ->
        println("  isLoggedIn: $isLoggedIn, users: ${users.joinToString { it.name }}")

        ChatUsers(
            userNames = users.map { curUser ->
                curUser.name
            },
            headerTitle = ("First User: " + users.firstOrNull()?.name),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    fun onUserJoined(user: User) {
        viewModelScope.launch {
            if(_users.replayCache.isEmpty()) {
                _users.tryEmit(listOf(user))
            } else {
                _users.tryEmit(_users.replayCache.first() + user)
            }
        }
    }

    fun onUserMessageReceived(message: ChatMessage) {

        viewModelScope.launch {
            if(_chatMessages.replayCache.isEmpty()) {
                _chatMessages.tryEmit(listOf(message))
            } else {
                _chatMessages.tryEmit(_chatMessages.replayCache.first() + message)
            }
            //_chatMessages.tryEmit(_chatMessages.last() + message)
        }
    }

    fun onLogin() {
        viewModelScope.launch {
            _isLoggedIn.tryEmit(true)
        }
    }
    fun onLogout() {
        viewModelScope.launch {
            _isLoggedIn.tryEmit(false)
        }
    }

}

