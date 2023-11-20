package com.example.inclassmedia

sealed class UserAction {
    object PausePlaySong: UserAction()
    object NextSong: UserAction()
    object PrevSong: UserAction()
}