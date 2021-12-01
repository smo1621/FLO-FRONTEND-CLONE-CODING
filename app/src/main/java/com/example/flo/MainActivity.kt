package com.example.flo

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flo.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    // 미디어 플레이어
    private var mediaPlayer : MediaPlayer? = null
    lateinit var timer : Timer


    // songs, songDB
    private var songs = ArrayList<Song>()
    private var nowPos = 0
    private lateinit var songDB : SongDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()
        inputDummyAlbums()
        inputDummySongs()

        initPlayList()
        initSong()
        initClickListener()

        binding.mainPlayerLayout.setOnClickListener{
            val editor = getSharedPreferences("song", MODE_PRIVATE).edit()
            editor.putInt("songId", songs[nowPos].id)
            editor.apply()

            val intent = Intent(this@MainActivity, SongActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()

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

        startTimer()
        setMiniPlayer(songs[nowPos])
    }

    private fun getPlayingSongPosition(songId : Int) : Int{
        for (i in 0 until songs.size){
            if (songs[i].id == songId){
                return i
            }
        }
        return 0
    }


    private fun setMiniPlayer(song : Song) {
        binding.mainMiniplayerTitleTv.text = song.title
        binding.mainMiniplayerSingerTv.text = song.singer
        binding.mainProgressSb.progress = (song.second * 1000 / song.playTime)

        val music = resources.getIdentifier(song.music, "raw", this.packageName)

        setPlayerStatus(song.isPlaying)
        mediaPlayer = MediaPlayer.create(this, music)

        if (song.isPlaying) {
            binding.mainPauseBtn.visibility = View.VISIBLE
            binding.mainMiniplayerBtn.visibility = View.GONE
        } else {
            binding.mainPauseBtn.visibility = View.GONE
            binding.mainMiniplayerBtn.visibility = View.VISIBLE
        }

    }


    private fun initClickListener() {
        binding.mainMiniplayerBtn.setOnClickListener{
            setPlayerStatus(true)
        }

        binding.mainPauseBtn.setOnClickListener{
            setPlayerStatus(false)
        }

        binding.mainMiniplayerPrevious.setOnClickListener{
            setPlayerStatus(false)
            moveSong(-1)
            setPlayerStatus(true)
        }
        binding.mainMiniplayerNext.setOnClickListener{
            setPlayerStatus(false)
            moveSong(+1)
            setPlayerStatus(true)
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

        setMiniPlayer(songs[nowPos])
    }


    fun setPlayerStatus(isPlaying : Boolean){
        timer.isPlaying = isPlaying
        songs[nowPos].isPlaying = isPlaying

        if(isPlaying){
            binding.mainMiniplayerBtn.visibility = View.GONE
            binding.mainPauseBtn.visibility = View.VISIBLE

            mediaPlayer?.start()
        }
        else {
            binding.mainMiniplayerBtn.visibility = View.VISIBLE
            binding.mainPauseBtn.visibility = View.GONE
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
                            binding.mainProgressSb.progress=(second*1000/playTime).toInt()
                        }

                    }

                }

            }catch (e : InterruptedException){
                Log.d("SONG", "쓰레드가 죽었습니다. ${e.message}")
            }
        }
    }



    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences("song", MODE_PRIVATE)
        val songId = sharedPreferences.getInt("songId",0)

        val songDB = SongDatabase.getInstance(this)!!
        songs[nowPos] = if (songId == 0){
            songDB.songDao().getSong(1)
        } else {
            songDB.songDao().getSong(songId)
        }
        Log.d("song ID",songs[nowPos].id.toString())
        setMiniPlayer(songs[nowPos])

    }


    private fun initNavigation() {
        supportFragmentManager.beginTransaction().replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.lookFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, LookFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.searchFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, SearchFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.lockerFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, LockerFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

            }
            false
        }

    }


    //ROOM_DB
    private fun inputDummyAlbums(){
        val songDB = SongDatabase.getInstance(this)!!
        val albums = songDB.albumDao().getAlbums()

        if (albums.isNotEmpty()) return

        songDB.albumDao().insert(
            Album(
                1,
                "No place to hide","카더가든 (Carthegarden)",R.drawable.img_album_exp6
            )
        )

        songDB.albumDao().insert(
            Album(
                2,
                "can't","아월 (OurR)",R.drawable.img_album_exp7
            )
        )

        songDB.albumDao().insert(
            Album(
                3,
                "haaAakkKKK!!!","아월 (OurR)",R.drawable.img_album_exp9
            )
        )

        songDB.albumDao().insert(
            Album(
                4,
                "YOUTH","다섯 (Dasut)",R.drawable.img_album_exp10
            )
        )

        songDB.albumDao().insert(
            Album(
                5,
                "Love Is All Around","웨터 (Wetter)",R.drawable.img_album_exp11
            )
        )

        songDB.albumDao().insert(
            Album(
                6,
                "Go Back","멜로망스 (Melomance)",R.drawable.img_album_exp4
            )
        )

        songDB.albumDao().insert(
            Album(
                7,
                "Savage","에스파 (Aespa)",R.drawable.img_album_exp3
            )
        )

        songDB.albumDao().insert(
            Album(
                8,
                "LILAC", "아이유 (IU)",R.drawable.img_album_exp2
            )
        )

        songDB.albumDao().insert(
            Album(
                9,
                "Butter", "방탄소년단 (BTS)",R.drawable.img_album_exp
            )
        )
    }


    //ROOM_DB
    private fun inputDummySongs() {
        val jwt: Int = getUserIdx(this)
        val songDB = SongDatabase.getInstance(this)!!
        val songs = songDB.songDao().getSongs()

        if (songs.isNotEmpty()) return

        songDB.songDao().insert(
            Song(
                "숨을 곳 없어요",
                "카더가든 (Carthegarden)",
                0,
                218,
                false,
                "music_no_place_to_hide",
                R.drawable.img_album_exp6,
                false,
                1
            )
        )

        songDB.songDao().insert(
            Song(
                "Home Sweet Home",
                "카더가든 (Carthegarden)",
                0,
                205,
                false,
                "music_home_sweet_home",
                R.drawable.img_album_exp6,
                false,
                1
            )
        )

        songDB.songDao().insert(
            Song(
                "Mung",
                "아월 (OurR)",
                0,
                298,
                false,
                "music_mung",
                R.drawable.img_album_exp7,
                false,
                2
            )
        )

        songDB.songDao().insert(
            Song(
                "haaAakkKKK!!!",
                "아월 (OurR)",
                0,
                173,
                false,
                "music_haakk",
                R.drawable.img_album_exp9,
                false,
                3
            )
        )

        songDB.songDao().insert(
            Song(
                "Youth",
               "다섯 (Dasut)",
                0,
                361,
                false,
                "music_youth",
                R.drawable.img_album_exp10,
                false,
                4
            )
        )

        songDB.songDao().insert(
            Song(
                "Love is all around",
                "웨터 (Wetter)",
                0,
                264,
                false,
                "music_love_is_all_around",
                R.drawable.img_album_exp11,
                false,
                5
            )
        )

        songDB.songDao().insert(
            Song(
                "고백",
                "멜로망스 (MeloMance)",
                0,
                232,
                false,
                "music_goback",
                R.drawable.img_album_exp4,
                false,
                6
            )
        )

        songDB.songDao().insert(
            Song(
                "Savage",
                "에스파 (Aespa)",
                0,
                238,
                false,
                "music_savage",
                R.drawable.img_album_exp3,
                false,
                7
            )
        )

        songDB.songDao().insert(
            Song(
                "Lilac",
                "아이유 (IU)",
                0,
                213,
                false,
                "music_lilac",
                R.drawable.img_album_exp2,
                false,
                8
            )
        )

        songDB.songDao().insert(
            Song(
                "Butter",
                "방탄소년단 (bts)",
                0,
                163,
                false,
                "music_butter",
                R.drawable.img_album_exp,
                false,
                9
            )
        )


    }


}

