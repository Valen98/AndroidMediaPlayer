package com.example.inclassmedia

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicItem(songId: Int) {
    val musicList = MainActivity.musicList
    val mContext = LocalContext.current
    var song: Song? = null
    var popup by remember { mutableStateOf(false) }

    for (songs in musicList) {
        if(songs.id == songId) {

            song = songs
        }
    }

    if(song != null) {
        Column {
            Row(modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        val intent = Intent(mContext, DetailedMusicScreen::class.java)
                        intent.putExtra("SongId", song?.id)
                        mContext.startActivity(intent)
                    },
                    onLongClick = {
                        popup = true
                    }
                )
            ){
                val imageModifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)

                if(song?.cover !=  null) {
                    val coverByteArray = song?.cover
                    var bitmap: Bitmap? = null

                    if(coverByteArray != null) {
                        bitmap = BitmapFactory.decodeByteArray(coverByteArray, 0, coverByteArray!!.size)
                    }
                    if (bitmap != null) {
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Album Cover", modifier = imageModifier )
                    }
                }else {
                    Image(painterResource(id = R.drawable.ic_music_note),"Music Note", modifier = imageModifier)
                }
                Column(modifier = Modifier.padding(8.dp)) {
                    song?.title?.let { Text(text = it, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()) }
                    song?.artist?.let { Text(text = it)}

                }

            }
            if(popup) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Button(
                        onClick = {
                            popup = false
                            musicList.remove(song)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text(text = "Delete")
                    }
                    Button(onClick = {popup = false}) {
                        Text(text = "Cancel")
                    }
                }

            }
            Divider(color = Color.LightGray, thickness = 1.dp)
        }

    }



}


@Preview
@Composable
fun MusicItemPrev() {
    MusicItem(songId = 0)
}