package com.realityexpander.stateflowwithstateinandcombine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel3 : ViewModel() {

    private val _isLoggedIn3 = MutableStateFlow(false)
    val isLoggedIn3 = _isLoggedIn3.asStateFlow()

    private val _chatMessages3 = MutableStateFlow(ListChatMessage())
    val chatMessages3 = _chatMessages3.asStateFlow()

    private val _users3 = MutableStateFlow(ListUser())
    val users3 = _users3.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    init {
        _users3.update {
            ListUser(listOf())
        }
        _chatMessages3.update {
            ListChatMessage(listOf())
        }
        _isLoggedIn3.update {
            false
        }
    }


    // using ListChatMessage and ListUser as a workaround for the "no emit without different value" issue with
    //   MutableStateFlow<List<ChatMessage>> and MutableStateFlow<List<User>>
    val chatState3 =
        combine(isLoggedIn3, chatMessages3, users3) {
                isLoggedIn,
                listChatMessages,
                listUser ->

            println("combine executed: \n" +
                    "  isLoggedIn3: $isLoggedIn, \n" +
                    "  users3: [${listUser.users?.joinToString { it.name }}]\n" +
                    "  chatMessages3: \n" +
                    "  [ ${listChatMessages.chatMessages?.joinToString { 
                        it.fromUser.name +" -> "+it.message + "\n  " 
                    }}]"
            )

            if (isLoggedIn) {
                ChatState(
                    userPreviews = listUser.users?.map { curUser ->

                        // Create the preview for the curUser
                        UserPreview(
                            user = curUser,
                            lastMessage = listChatMessages.chatMessages
                                ?.filter { listMessage ->
                                    listMessage.fromUser == curUser
                                }
                                ?.maxByOrNull { message ->
                                    message.time
                                }
                                ?.message,
                            lastMessageTime = listChatMessages.chatMessages
                                ?.filter { message ->
                                    message.fromUser == curUser
                                }
                                ?.maxByOrNull { message ->
                                    message.time
                                }
                                ?.time
                        )
                    } ?: emptyList(),
                    headerTitle = ("Last chat user: " + (listUser.users?.lastOrNull()?.name ?: "No user")),
                )
            } else
                null

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // Using ListUser as a workaround for the issue with MutableStateFlow<List<User>>
    val chatUsers3 = combine(isLoggedIn3, users3) {
            isLoggedIn,
            listUsers ->

        println("chatUsers3 executed:\n" +
                "  isLoggedIn3: $isLoggedIn\n" +
                "  users3: [${listUsers.users?.joinToString { 
                    it.name 
                }}]")

        ChatUsers(
            userNames = listUsers.users?.map { curUser ->
                curUser.name
            },
            headerTitle = ("First User: " + listUsers.users?.firstOrNull()?.name),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    // Using regular List<User>, does NOTHING DIFFERENT than the above
    val chatUsers = combine(isLoggedIn3, _users) {
            isLoggedIn,
            users ->

        println("chatUsers executed:\n" +
                "  isLoggedIn3: $isLoggedIn\n" +
                "  users: [${users?.joinToString {
                    it.name
                }}]")

        ChatUsers(
            userNames = users.map { curUser ->
                curUser.name
            },
            headerTitle = ("First User: " + users.firstOrNull()?.name),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    fun onUserJoined(user: User) {
        _users3.update {
            ListUser(it.users?.plus(user))
        }

        _users.update {
            it.plus(user)
        }
    }

    fun onUserMessageReceived(message: ChatMessage) {
        _chatMessages3.update {
            ListChatMessage(it.chatMessages?.plus(message))
        }
    }

    fun onLogin() {
        _isLoggedIn3.update { true }
    }
    fun onLogout() {
        _isLoggedIn3.update { false }
    }

}

data class ListUser(
    val users: List<User>? = null,
    //private val id: UUID = UUID.randomUUID(), //added unique id to force updating (NOT NEEDED!)
)

data class ListChatMessage(
    val chatMessages: List<ChatMessage>? = null,
    //private val id: UUID = UUID.randomUUID(), //added unique id to force updating (NOT NEEDED!)
)
