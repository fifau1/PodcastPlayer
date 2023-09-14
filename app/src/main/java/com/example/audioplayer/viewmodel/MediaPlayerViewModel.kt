package com.example.audioplayer.viewmodel
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*


class MediaPlayerViewModel: ViewModel() {
    var title: String = "t"
    var artist: String = "a"
    var image: ImageBitmap? = null
    private var currentDuration: CountDownTimer? = null
    private val _currentMinutes = MutableLiveData(0)
    val currentMinutes: LiveData<Int> get() = _currentMinutes
    private val _audioFinish = MutableLiveData(false)
    val audioFinish: LiveData<Boolean> get() = _audioFinish

    fun getMediaDuration(mediaPlayer: MediaPlayer) {
        currentDuration = object : CountDownTimer(mediaPlayer.duration.toLong(), 500) {
            override fun onTick(milliSec: Long) {
                _currentMinutes.value = mediaPlayer.currentPosition
            }

            override fun onFinish() {
                _audioFinish.value = true
                Log.d("TAG", "onFinish: Media Player Finished")
            }
        }

        currentDuration!!.start()
    }



    fun setSongTitle(t: String){
title = t
    }

    fun getSongTitle(): String {
        return title
    }

    fun setSongArtist(t: String){
        artist = t
    }

    fun getSongArtist(): String {
        return artist
    }

    fun setSongImage(t: ImageBitmap?){
        image = t
    }

    fun getSongImage(): ImageBitmap? {
        return image
    }



}

