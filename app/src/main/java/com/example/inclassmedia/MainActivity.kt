package com.example.inclassmedia

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inclassmedia.ui.theme.InClassMediaTheme

class MainActivity : ComponentActivity() {

    private lateinit var songIds: ArrayList<Int>

    companion object{
        lateinit var musicList: ArrayList<Song>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InClassMediaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary),) {
                        Text(text = "Dan's Music Application",
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 16.dp)
                                .padding(bottom = 8.dp),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            )
                    }
                    LazyColumn(modifier = Modifier.padding(top = 64.dp).background(MaterialTheme.colorScheme.background)) {
                        items(songIds)  { it ->
                            MusicItem(it)
                        }
                    }
                }
            }
        }

        songIds = arrayListOf(R.raw.song1, R.raw.song2, R.raw.song3, R.raw.song4,
            R.raw.song5, R.raw.song6, R.raw.song7, R.raw.song8, R.raw.song9,
            R.raw.song10, R.raw.song11, R.raw.song12, R.raw.song13, R.raw.song14)

        musicList = arrayListOf()

        val mmr: MediaMetadataRetriever = MediaMetadataRetriever()
        var afd: AssetFileDescriptor

        for((count, music) in songIds.withIndex()) {
            afd = this.resources.openRawResourceFd(music)
            mmr.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            musicList.add(
                Song(
                    count,
                    music,
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
                    mmr.embeddedPicture
                )
            )
            afd.close();
        }

    }

    override fun onStart() {
        super.onStart()

        if(MyMediaService.started){
            Intent(this, MyMediaService::class.java).also { intent->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }

        }
        else{
            val intent = Intent(this,MyMediaService::class.java)
            startForegroundService(intent)
        }


    }


    lateinit var mService: MyMediaService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MyMediaService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

}
