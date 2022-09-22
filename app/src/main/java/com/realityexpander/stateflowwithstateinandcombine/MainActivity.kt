package com.realityexpander.stateflowwithstateinandcombine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel = UserViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ////////////// manual way to update localUser state
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

        //////////// Automatic update of localUser state using stateIn
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
            User("local", "Chris Bthanas", "https://www.avatar.com/chrisathanas", isOnline = true)
        )


    }
}