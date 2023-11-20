package com.example.inclassmedia


data class SongUiState(
    var song: Song? = null,
    var currentPlaying: Boolean = true,
    var songCover: ByteArray? = null,
    var songVolume: Float = 1f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongUiState

        if (songCover != null) {
            if (other.songCover == null) return false
            if (!songCover.contentEquals(other.songCover)) return false
        } else if (other.songCover != null) return false

        return true
    }

    override fun hashCode(): Int {
        return songCover?.contentHashCode() ?: 0
    }
}
