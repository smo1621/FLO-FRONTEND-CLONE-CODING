package com.example.flo

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flo.databinding.ActivitySongBinding
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class SongActivity : AppCompatActivity(){
    lateinit var binding : ActivitySongBinding

    //미디어 플레이어
    private var mediaPlayer: MediaPlayer? = null
    lateinit var timer : Timer


    private var songs = ArrayList<Song>()
    private var nowPos = 0
    private lateinit var songDB : SongDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayList()
        initSong()
        initClickListener()
    }

    override fun onPause() {
        super.onPause()

        songs[nowPos].second = (songs[nowPos].playTime * binding.songPlayerSb.progress) / 1000
        songs[nowPos].isPlaying = false
        setPlayerStatus(false)

        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit() // sharedPreferences 조작할 때 사용.

        editor.putInt("songId", songs[nowPos].id)
        editor.apply()
    }

    override fun onDestroy(){
        super.onDestroy()

        timer.interrupt() // 스레드 해제
        mediaPlayer?.release() // 미디어 플레이어가 갖고 있던 리소스 해제
        mediaPlayer = null // 미디어 플레이어 해제
    }

    private fun initPlayList() {
        songDB = SongDatabase.getInstance(this)!!
        songs.addAll(songDB.songDao().getSongs())
    }


    private fun startTimer() {
        timer = Timer(songs[nowPos].playTime, songs[nowPos].isPlaying)
        timer.start()
    }



    private fun initSong(){
        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId",0)

        nowPos = getPlayingSongPosition(songId)

        Log.d("now Song ID", songs[nowPos].id.toString())

        startTimer()
        setPlayer(songs[nowPos])
    }

    private fun getPlayingSongPosition(songId : Int) : Int{
        for (i in 0 until songs.size){
            if (songs[i].id == songId){
                return i
            }
        }
        return 0
    }


    private fun setPlayer(song: Song){
        val music = resources.getIdentifier(song.music, "raw", this.packageName)

        binding.songTitleTv.text = song.title
        binding.songArtistTv.text = song.singer
        binding.songStarttimeTv.text =
            String.format("%02d:%02d", song.second / 60, song.second % 60)
        binding.songEndtimeTv.text =
            String.format("%02d:%02d", song.playTime / 60, song.playTime % 60)
        binding.songImgAlbumExp2Iv.setImageResource(song.coverImg!!)
        binding.songPlayerSb.progress = (song.second * 1000 / song.playTime)

        setPlayerStatus(song.isPlaying)

        if (song.isLike){
            binding.songIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_on)
        }
        else {
            binding.songIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_off)
        }

        mediaPlayer = MediaPlayer.create(this,music)
    }

    private fun initClickListener() {
        binding.songNuguBtnDownIv.setOnClickListener{
            finish()
        }

        binding.songBtnPlayerPlayIv.setOnClickListener{
            setPlayerStatus(true)
        }

        binding.songBtnMiniplayMvpauseIv.setOnClickListener{
            setPlayerStatus(false)
        }

        binding.songBtnMiniplayerPreviousIv.setOnClickListener{
            setPlayerStatus(false)
            moveSong(-1)
            setPlayerStatus(true)
        }
        binding.songBtnMiniplayerNextIv.setOnClickListener{
            setPlayerStatus(false)
            moveSong(+1)
            setPlayerStatus(true)
        }
        binding.songIcMyLikeOffIv.setOnClickListener {
            setLike(songs[nowPos].isLike)
        }
    }

    private fun moveSong(direct : Int){
        if (nowPos + direct < 0){
            Toast.makeText(this,"first song", Toast.LENGTH_SHORT).show()
            return
        }
        if (nowPos + direct >= songs.size){
            Toast.makeText(this,"last song", Toast.LENGTH_SHORT).show()
            return
        }
        nowPos += direct
        timer.interrupt()
        startTimer()


        mediaPlayer?.release()
        mediaPlayer = null

        setPlayer(songs[nowPos])
    }

    private fun setLike(isLike : Boolean){
        songs[nowPos].isLike = !isLike
        songDB.songDao().updateIsLikeById(!isLike,songs[nowPos].id)

        if (isLike){
            binding.songIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_off)
        }
        else{
            binding.songIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_on)
        }
    }


    fun setPlayerStatus(isPlaying : Boolean){
        timer.isPlaying = isPlaying
        songs[nowPos].isPlaying = isPlaying

        if(isPlaying){
            binding.songBtnPlayerPlayIv.visibility = View.GONE
            binding.songBtnMiniplayMvpauseIv.visibility = View.VISIBLE

            mediaPlayer?.start()
        }
        else {
            binding.songBtnPlayerPlayIv.visibility = View.VISIBLE
            binding.songBtnMiniplayMvpauseIv.visibility = View.GONE
            mediaPlayer?.pause()
        }
    }
    inner class Timer(private val playTime:Int = 0, var isPlaying : Boolean = false) : Thread(){
        private var second : Long =0

        @SuppressLint("setTextI18n")
        override fun run() {
            try{
                while (true){
                    if(second>=playTime){
                        break
                    }

                    if(isPlaying){
                        sleep(1000)
                        second++

                        runOnUiThread{
                            binding.songPlayerSb.progress=(second*1000/playTime).toInt()
                            binding.songStarttimeTv.text = String.format("%02d:%02d",
                                TimeUnit.SECONDS.toMinutes(second),second%60)
                        }
                    }

                }

            }catch (e : InterruptedException){
                Log.d("SONG", "쓰레드가 죽었습니다. ${e.message}")
            }
        }
    }
}