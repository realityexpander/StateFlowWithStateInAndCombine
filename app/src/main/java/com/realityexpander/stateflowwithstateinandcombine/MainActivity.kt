package com.realityexpander.stateflowwithstateinandcombine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Based on these ideas:
// https://stackoverflow.com/questions/62331931/mutablestateflow-is-not-emitting-values-after-1st-emit-kotlin-coroutine

class MainActivity : AppCompatActivity() {

    private val viewModel = UserViewModel()  // uses MutableStateFlow and
    private val chatViewModel = ChatViewModel() // uses MutableStateFlow and combine
    private val chatViewModel2 = ChatViewModel2() // uses MutableStateFlow and combine
    private val chatViewModel3 =
        ChatViewModel3() // uses MutableStateFlow and combine and use wrapper classes to force emits

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ////////////// manual way to update localUser state (not recommended) //////////////
        if (false) {
            lifecycleScope.launch {
                viewModel.users.collect {
                    println("Users: $it")
                }
            }
            lifecycleScope.launch {
                viewModel.localUser.collect {
                    println("localUser: $it")
                }
            }


            //////////// Automatic update of localUser state using `stateIn` //////////////
            lifecycleScope.launch {
                viewModel.users2.collect {
                    println("Users2: $it")
                }
            }
            lifecycleScope.launch {
                viewModel.localUser2.collect {
                    println("localUser2: $it")
                }
            }


            /////// Simulate network interaction

            viewModel.onUserJoined(
                User("John1", "john doe", "https://www.avatar.com/johndoe")
            )
            viewModel.onUserJoined(
                User("Sally1", "Sassy Sally", "https://www.avatar.com/sassysally")
            )
            viewModel.onUserJoined(
                User("local", "Chris Athanas", "https://www.avatar.com/chrisathanas")
            )


            viewModel.onUserInfoUpdated(
                User("John1", "john doe-updated", "https://www.avatar.com/johndoe", isOnline = true)
            )
            viewModel.onUserInfoUpdated(
                User(
                    "local",
                    "Chris Bthanas",
                    "https://www.avatar.com/chrisathanas",
                    isOnline = true
                )
            )
        }


        //////////////////////////////////////////////////////////////////////////
        //// ChatViewModel - Combine multiple state flows into a single state flow

        if (false) {
            println("\n\nCHAT VIEWMODEL\n\n")

            lifecycleScope.launch {
                chatViewModel.chatState.collect {
                    //chatViewModel.chatState.stateIn(lifecycleScope).collect {
                    //flowOf(chatViewModel.chatState).flatMapMerge {
                    //flowOf(chatViewModel.chatState).flattenMerge(1).collect {
                    println(
                        "chatState collected: \n" +
                                "  header=${it?.headerTitle} \n" +
                                "  users latest messages: ${
                                    it?.userPreviews?.joinToString { preview ->
                                        preview.user.name + " >>> " + preview.lastMessage.toString() + "\n  "
                                    }
                                }"
                    )
                }
            }

//            // Simulate another collector of chatState (e.g. another screen) - just gets the latest value
//            lifecycleScope.launch {
//                chatViewModel.chatState.collect {
//                    println(
//                        "chatState collector #2: \n" +
//                                "  users latest messages: ${
//                                    it?.userPreviews?.joinToString { preview ->
//                                        preview.user.name + " >>> " + preview.lastMessage.toString() + "\n"
//                                    }
//                                }"
//                    )
//                }
//            }

//        lifecycleScope.launch {
//            chatViewModel.chatUsers.collect {
//                println("chatUsers MA: $it")
//            }
//        }

//        lifecycleScope.launch {
//            chatViewModel.isLoggedIn.collect {
//                println("isLoggedIn: $it")
//            }
//        }
//
//        lifecycleScope.launch {
//            chatViewModel.chatMessages.collect {
//                println("chatMessages: $it")
//            }
//        }
//
//        lifecycleScope.launch {
//            chatViewModel.users.collect {
//                println("users: $it")
//            }
//        }

            val user1 = User("John1", "john doe", "https://www.avatar.com/johndoe")
            val user2 = User("Sally1", "Sassy Sally", "https://www.avatar.com/sassysally")
            val user3 = User("local", "Chris Athanas", "https://www.avatar.com/chrisathanas")
            val user4 = User("jimbo", "Jimbo Jangles", "https://www.avatar.com/jimbojangles")

            lifecycleScope.launch {
                delay(100)
                println("\nAfter 100ms delay...")

                chatViewModel.onLogin()

                chatViewModel.onUserJoined(
                    user1
                )
                chatViewModel.onUserJoined(
                    user2
                )
                chatViewModel.onUserJoined(
                    user3

                )
                delay(200)
                println("\nAfter 200ms delay...")

                chatViewModel.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 1")
                )
                chatViewModel.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 2")
                )
                chatViewModel.onUserMessageReceived(
                    ChatMessage(user2, "user2 - Message 1")
                )

                delay(300)
                println("\nAfter 300ms delay...")

                chatViewModel.onUserMessageReceived(
                    ChatMessage(user3, "user3 - Message 1")
                )
                chatViewModel.onLogout()

                chatViewModel.onUserMessageReceived(
                    ChatMessage(user3, "user3 - Message 2")
                )
                chatViewModel.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 3")
                )

                delay(400)
                println("\nAfter 400ms delay...")

                chatViewModel.onLogin()
                chatViewModel.onLogout()

                chatViewModel.onUserJoined(
                    user4
                )

                delay(500)
                println("\nAfter 500ms delay...")

                chatViewModel.onLogin()
                chatViewModel.onLogout()
            }
        }


        //////////////////////////////////////////////////////////////////////////
        //// ChatViewModel2 - Combine multiple SharedFlows into a single StateFlow

        if (false) {
            println("\n\nCHAT VIEWMODEL2\n\n")

            lifecycleScope.launch {
                chatViewModel2.chatState2.collect {
                    //chatViewModel.chatState2.stateIn(lifecycleScope).collect {
                    //flowOf(chatViewModel.chatState2).flatMapMerge {
                    //flowOf(chatViewModel.chatState2).flattenMerge(1).collect {
                    println(
                        "chatState2 collected: \n" +
                                "  header=${it?.headerTitle} \n" +
                                "  users latest messages: ${
                                    it?.userPreviews?.joinToString { preview ->
                                        preview.user.name + " >>> " + preview.lastMessage.toString() + "\n"
                                    }
                                }"
                    )
                }
            }

//            // Simulate another collector of chatState2
//            lifecycleScope.launch {
//                chatViewModel2.chatState2.collect {
//                    println(
//                        "chatState2 collector #2: \n" +
//                                "  users latest messages: ${
//                                    it?.userPreviews?.joinToString { preview ->
//                                        preview.user.name + " >>> " + preview.lastMessage.toString() + "\n"
//                                    }
//                                }"
//                    )
//                }
//            }

//        lifecycleScope.launch {
//            chatViewModel.chatUsers2.collect {
//                println("chatUsers MA: $it")
//            }
//        }

            val user1 = User("John1", "john doe", "https://www.avatar.com/johndoe")
            val user2 = User("Sally1", "Sassy Sally", "https://www.avatar.com/sassysally")
            val user3 = User("local", "Chris Athanas", "https://www.avatar.com/chrisathanas")
            val user4 = User("jimbo", "Jimbo Jangles", "https://www.avatar.com/jimbojangles")

            lifecycleScope.launch {
                delay(100)
                println("\nAfter 100ms delay...")

                chatViewModel2.onLogin()

                chatViewModel2.onUserJoined(
                    user1
                )
                chatViewModel2.onUserJoined(
                    user2
                )
                chatViewModel2.onUserJoined(
                    user3

                )
                delay(200)
                println("\nAfter 200ms delay...")

                chatViewModel2.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 1")
                )
                chatViewModel2.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 2")
                )
                chatViewModel2.onUserMessageReceived(
                    ChatMessage(user2, "user2 - Message 1")
                )

                delay(300)
                println("\nAfter 300ms delay...")

                chatViewModel2.onUserMessageReceived(
                    ChatMessage(user3, "user3 - Message 1")
                )
                chatViewModel2.onLogout()

                chatViewModel2.onUserMessageReceived(
                    ChatMessage(user3, "user3 - Message 2")
                )
                chatViewModel2.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 3")
                )

                delay(400)
                println("\nAfter 400ms delay...")

                chatViewModel2.onLogin()
                chatViewModel2.onLogout()

                chatViewModel2.onUserJoined(
                    user4
                )

                delay(500)
                println("\nAfter 500ms delay...")

                chatViewModel2.onLogin()
                chatViewModel2.onLogout()
            }
        }

        //////////////////////////////////////////////////////////////////////////
        //// ChatViewModel3 - Combine multiple StateFlows into a single StateFlow
        ////   but force the state to be updated using wrappers with UUID's (NOT NEEDED!!!)

        if (true) {
            println("\n\nCHAT VIEWMODEL3\n\n")

            lifecycleScope.launch {
                chatViewModel3.chatState3.collect { chatState ->
                    println(
                        "chatState3 collected: \n" +
                                "  headerTitle=${chatState?.headerTitle}\n" +
                                "  users3 latest message:\n  [ ${
                                    chatState?.userPreviews?.joinToString { preview ->
                                        preview.user.name + " >>> " + preview.lastMessage.toString() + "\n  "
                                    }
                                } ]"
                    )
                }
            }

//            lifecycleScope.launch {
//                chatViewModel3.chatUsers3.collect { chatUsers ->
//                    println("chatUsers3 MA (ListUser -> ChatUsers): \n" +
//                            "  ChatUsers: [${chatUsers?.userNames?.joinToString { user -> user }}]")
//                }
//            }
//
//            lifecycleScope.launch {
//                chatViewModel3.chatUsers.collect { chatUsers ->
//                    println("chatUsers MA (List<User> -> ChatUsers): \n" +
//                            "  ChatUsers: [${chatUsers?.userNames?.joinToString { user -> user }}]")
//                }
//            }
//
//            lifecycleScope.launch {
//                chatViewModel3.users3.collect {
//                    println("users3 MA (ListUser): \n" +
//                            "  ListUser: $it")
//                }
//            }

            lifecycleScope.launch {
                chatViewModel3.users.collect {
                    println("users MA (List<User>): \n" +
                            "  List<User>: $it")
                }
            }

            val user1 = User("John1", "john doe", "https://www.avatar.com/johndoe")
            val user2 = User("Sally1", "Sassy Sally", "https://www.avatar.com/sassysally")
            val user3 = User("local", "Chris Athanas", "https://www.avatar.com/chrisathanas")
            val user4 = User("jimbo", "Jimbo Jangles", "https://www.avatar.com/jimbojangles")

            // Add users/messages without lifecycleScope or Coroutines
            if (true) {

                chatViewModel3.onUserJoined(
                    user1
                )
                chatViewModel3.onUserJoined(
                    user2
                )
                chatViewModel3.onUserJoined(
                    user3
                )

                chatViewModel3.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 1")
                )
                chatViewModel3.onUserMessageReceived(
                    ChatMessage(user1, "user1 - Message 2")
                )
                chatViewModel3.onUserMessageReceived(
                    ChatMessage(user2, "user2 - Message 1")
                )

                chatViewModel3.onLogin()
            }

            // Add users/messages with lifecycleScope and Coroutines
            if (false) {
                lifecycleScope.launch {
                    delay(100)
                    println("\nAfter 100ms delay...")

                    chatViewModel3.onLogin()

                    chatViewModel3.onUserJoined(
                        user1
                    )
                    chatViewModel3.onUserJoined(
                        user2
                    )
                    chatViewModel3.onUserJoined(
                        user3
                    )
                    delay(200)
                    println("\nAfter 200ms delay...")

                    if (false) {

                        chatViewModel3.onUserMessageReceived(
                            ChatMessage(user1, "user1 - Message 1")
                        )
                        chatViewModel3.onUserMessageReceived(
                            ChatMessage(user1, "user1 - Message 2")
                        )
                        chatViewModel3.onUserMessageReceived(
                            ChatMessage(user2, "user2 - Message 1")
                        )

                        delay(300)
                        println("\nAfter 300ms delay...")

                        chatViewModel3.onUserMessageReceived(
                            ChatMessage(user3, "user3 - Message 1")
                        )
                        chatViewModel3.onLogout()

                        chatViewModel3.onUserMessageReceived(
                            ChatMessage(user3, "user3 - Message 2")
                        )
                        chatViewModel3.onUserMessageReceived(
                            ChatMessage(user1, "user1 - Message 3")
                        )

                        delay(400)
                        println("\nAfter 400ms delay...")

                        chatViewModel3.onLogin()
                        chatViewModel3.onLogout()

                        chatViewModel3.onUserJoined(
                            user4
                        )

                        delay(500)
                        println("\nAfter 500ms delay...")

                        chatViewModel3.onLogin()
                        chatViewModel3.onLogout()
                    }
                }
            }
        }


    }
}