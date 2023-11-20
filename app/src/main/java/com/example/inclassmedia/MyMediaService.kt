package com.example.inclassmedia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import kotlin.concurrent.thread

private const val ONGOING_NOTIFICATION = 1

class MyMediaService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
AudioManager.OnAudioFocusChangeListener{

    companion object{
        var started = false
        lateinit var self: MyMediaService
    }

    private var mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setOnPreparedListener { start() }
    }

    private var isPlaying: Boolean = false
    private lateinit var musicList: ArrayList<Song>
    private lateinit var mediaSession: MediaSessionCompat
    private var position:Int = 0
    private lateinit var runnable: Runnable
    private lateinit var notificationManager: NotificationManager
    private lateinit var mediaMetadata: MediaMetadataCompat
    private lateinit var bitmapCover: Bitmap
    private  var volume: Float = 1f


    override fun onCreate() {
        super.onCreate()
        self = this
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mediaSession = MediaSessionCompat(applicationContext,"MEDIA_SESSION")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        position = intent.getIntExtra("position",0)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()

        MediaButtonReceiver.handleIntent(mediaSession,intent)

        startForeground(ONGOING_NOTIFICATION,getNotification().build())

        thread{
            createMediaPlayer()
        }

        return START_STICKY
    }

    private fun createMediaPlayer():Boolean{

        musicList = MainActivity.musicList

        try {
            mediaPlayer!!.reset()
            mediaPlayer = MediaPlayer.create(this, musicList[position].id)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.start()
            isPlaying = true
            notificationManager.notify(ONGOING_NOTIFICATION, getNotification().build())
            mediaPlayer.setOnCompletionListener(this)
            return true
        }catch (e:Exception){
            return false
        }
    }

    private fun getNotification() : NotificationCompat.Builder {
        musicList = MainActivity.musicList

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(this, MainActivity::class.java)

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0,
                PendingIntent.FLAG_IMMUTABLE)
        }

        val builder = NotificationCompat.Builder(applicationContext, "music_id").apply {
            setContentTitle(musicList[position].title)
            setContentText(musicList[position].artist)
            setContentIntent(resultPendingIntent)
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    applicationContext,
                    PlaybackStateCompat.ACTION_STOP
                ))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.drawable.ic_music_note)


            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_skip_previous,
                    "Previous",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS,
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        if (isPlaying) PlaybackStateCompat.ACTION_PLAY else PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_skip_next,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )
            val coverByteArray = musicList[position].cover
            if(coverByteArray != null) {
                setLargeIcon(BitmapFactory.decodeByteArray(coverByteArray, 0, coverByteArray!!.size))
            }


            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)

                .setShowActionsInCompactView(1)
            )
        }
        return builder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_NONE
        val mChannel = NotificationChannel("music_id", getString(R.string.channel_name), importance)
        mChannel.description = getString(R.string.channel_descript)
        notificationManager.createNotificationChannel(mChannel)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer != null) {
                    volume = 0.3f
                    mediaPlayer.setVolume(volume, volume)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start()
                    }
                    volume = 1f
                    mediaPlayer.setVolume(volume, volume)
                }
            }
        }
    }

    override fun onCompletion(p0: MediaPlayer?) {
        setPosition(true)
        createMediaPlayer()
        updateMediaMetadata()
        notificationManager.notify(ONGOING_NOTIFICATION, getNotification().build())
    }

    private fun setPosition(increment:Boolean){
        if(increment){
            if(musicList.size - 1 == position){
                position = 0
            }else{
                position++
            }
        }else{
            if(position == 0){
                position = musicList.size - 1
            }else{
                position--
            }
        }
    }

    fun playPause():Boolean{
        if(isPlaying){
            mediaPlayer.pause()
            isPlaying = false
        }else{
            mediaPlayer.start()
            isPlaying = true
        }
        notificationManager.notify(ONGOING_NOTIFICATION, getNotification().build())
        return isPlaying
    }

    fun prevNexSong(increment:Boolean):Int{
        Log.d("Increment before", "The position should not change $position")
        setPosition(increment)
        Log.d("Increment After", "The position should change $position")
        updateMediaMetadata()
        notificationManager.notify(ONGOING_NOTIFICATION, getNotification().build())
        return if(createMediaPlayer()) position
        else 0
    }

    private fun updateMediaMetadata(){

        //extract cover as bitmap
        val coverByteArray = musicList[position].cover
        var bitmap: Bitmap? = null
        if(coverByteArray != null) {
            bitmap = BitmapFactory.decodeByteArray(coverByteArray, 0, coverByteArray!!.size)
            bitmapCover = bitmap
        }

        //update metadata
        mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, musicList[position].title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, musicList[position].artist)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, musicList[position].album)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, musicList[position].durationMS!!)
            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
            .build()

        mediaSession.setMetadata(mediaMetadata)
    }

    /** Called when MediaPlayer is ready */
    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    private val binder = LocalBinder()


    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder: Binder(){
        fun getService(): MyMediaService = this@MyMediaService
    }

    fun changeSong(id: Int) {
        val songRequest = findSongById(id)
        if(songRequest != null) {
            position = songRequest
            createMediaPlayer()
            updateMediaMetadata()
            notificationManager.notify(ONGOING_NOTIFICATION, getNotification().build())
            started = true
        }
    }
    private fun findSongById(id: Int): Int? {
        for (songs in musicList) {
            if(songs.id == id) {
                return musicList.indexOf(songs)
            }
        }
        return null
    }

    fun currentSongPlaying(): Song {
        return musicList[position]
    }

    fun getVolume(): Float{
        return volume
    }

    fun setVolume(setVolume: Float) {
        volume = setVolume
        mediaPlayer.setVolume(volume, volume)
    }
}