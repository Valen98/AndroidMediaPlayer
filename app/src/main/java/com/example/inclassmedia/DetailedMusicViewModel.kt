package com.example.inclassmedia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DetailedMusicViewModel() : ViewModel() {

    var musicState by mutableStateOf(SongUiState())

    fun setSong(song: Song?) {
        musicState.song = song

    }

    fun getSong(): Song? {
        return musicState.song
    }


    fun onAction(action: UserAction) {
        when(action) {
            is UserAction.PausePlaySong -> {
                musicState.currentPlaying = !musicState.currentPlaying
                pausePlay()
            }
            is UserAction.NextSong -> {
                musicState.currentPlaying = true
                nextSong()
                musicState.songCover?.let { setImage(it) }

            }
            is UserAction.PrevSong -> {
                musicState.currentPlaying = true
                prevSong()
                musicState.songCover?.let { setImage(it) }
            }

        }

    }

    private fun pausePlay() {
        musicState.currentPlaying = MyMediaService.self.playPause()
    }

    private fun nextSong() {
        MyMediaService.self.prevNexSong(true)
        MyMediaService.self.setVolume(musicState.songVolume)
        val currentSong: Song =  MyMediaService.self.currentSongPlaying()
        setSong(currentSong)
    }

    private fun setImage(songCover: ByteArray) {
        musicState.songCover = songCover
    }

    private fun prevSong() {
        MyMediaService.self.prevNexSong(false)
        MyMediaService.self.setVolume(musicState.songVolume)
        val currentSong: Song =  MyMediaService.self.currentSongPlaying()
        setSong(currentSong)
    }
}