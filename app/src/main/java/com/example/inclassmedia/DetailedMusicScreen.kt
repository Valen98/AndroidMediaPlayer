package com.example.inclassmedia

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inclassmedia.ui.theme.InClassMediaTheme

class DetailedMusicScreen : ComponentActivity() {

    private var song : Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InClassMediaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val musicList = MainActivity.musicList
                    val songId : Int? = intent.getIntExtra("SongId", 0)

                    for (songs in musicList) {
                        if(songs.id == songId) {
                            song = songs
                        }
                    }
                    song?.id?.let { MyMediaService.self.changeSong(it) }
                    val viewModel: DetailedMusicViewModel by viewModels()
                    viewModel.setSong(song)
                    MusicView(viewModel)
                }
            }
        }
    }
}

@Composable
fun MusicView(viewModel: DetailedMusicViewModel) {
    val state = viewModel.musicState

    val imageModifierButton = Modifier
        .size(75.dp)
        .padding(8.dp)
    var currentPlaying by remember { mutableStateOf(state.currentPlaying) }
    var songCover by remember { mutableStateOf(state.song?.cover) }
    var songTitle by remember { mutableStateOf(state.song?.title) }
    var songArtist by remember { mutableStateOf(state.song?.artist) }
    var volumePosition by remember { mutableStateOf(state.songVolume) }
    var songProgress by remember { mutableStateOf(0f) }

    Log.d("SongCover", "Song Cover= $songCover")
    Column(modifier = Modifier.fillMaxHeight()) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            val imageModifier = Modifier
                .size(400.dp)
                .padding(8.dp)

            if(songCover !=  null) {
                val coverByteArray = songCover

                val bitmap: Bitmap? = BitmapFactory.decodeByteArray(coverByteArray, 0, coverByteArray!!.size)

                if (bitmap != null) {
                    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Album Cover", modifier = imageModifier )
                }
            }else {
                Image(painterResource(id = R.drawable.ic_music_note),"Music Note", modifier = imageModifier)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            songTitle?.let { Text(text = it, fontSize = 32.sp, fontWeight = FontWeight.Bold) }
            songArtist?.let { Text(text = it, fontSize = 24.sp) }
        }

        Row {
            Text(
                text = "0:00",
                modifier = Modifier.padding(16.dp)
            )

            Slider(
                value = songProgress,
                onValueChange = {songProgress = it},
                modifier = Modifier
                    .width(275.dp)

            )

            state.song?.duration?.let { Text(text = it, modifier = Modifier.padding(16.dp)) }

        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            TextButton(onClick = {
                currentPlaying = true
                viewModel.onAction(UserAction.PrevSong)
                songCover = state.song?.cover
                songArtist = state.song?.artist
                songTitle = state.song?.title
            }) {
                Image(
                    painterResource(id = R.drawable.ic_skip_previous_black)
                    ,"Music Next",
                    modifier = imageModifierButton
                )
            }

            TextButton(onClick = {
                currentPlaying = !currentPlaying
                viewModel.onAction(UserAction.PausePlaySong)
            }) {
                if(currentPlaying) {
                    Image(painterResource(id = R.drawable.ic_pause),
                        "Music Play",
                        modifier = imageModifierButton)
                }else {
                    Image(painterResource(id = R.drawable.ic_play),
                        "Music Play",
                        modifier = imageModifierButton)
                }

            }

            TextButton(onClick = {
                currentPlaying = true
                viewModel.onAction(UserAction.NextSong)
                songCover = state.song?.cover
                songArtist = state.song?.artist
                songTitle = state.song?.title

            }) {
                Image(
                    painterResource(id = R.drawable.ic_skip_next_black)
                    ,"Music Next",
                    modifier = imageModifierButton
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row {
            if(volumePosition < 0.5f) {
                Image(
                    painterResource(id = R.drawable.ic_baseline_volume_down_24)
                    ,"Music Next",
                    modifier = Modifier.size(50.dp)
                )
            }else {
                Image(
                    painterResource(id = R.drawable.ic_baseline_volume_up_24)
                    ,"Music Next",
                    modifier = Modifier.size(50.dp)
                )
            }

            Slider(value = volumePosition,
                onValueChange = {
                    volumePosition = it
                    state.songVolume = volumePosition
                    MyMediaService.self.setVolume(volumePosition)
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            )

        }

    }

}




