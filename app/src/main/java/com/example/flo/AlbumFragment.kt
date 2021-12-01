package com.example.flo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.flo.databinding.FragmentAlbumBinding
import com.example.flo.databinding.FragmentSongBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson

class AlbumFragment : Fragment(){

    lateinit var binding : FragmentAlbumBinding
    private var gson : Gson = Gson()

    val information = arrayListOf("수록곡", "상세정보", "영상")

    private var isLiked: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlbumBinding.inflate(inflater, container, false)

        // Home 에서 넘어온 데이터 받아오기
        val albumData = arguments?.getString("album")
        val album = gson.fromJson(albumData, Album::class.java)
        isLiked = isLikedAlbum(album.id)

        // Home에서 넘어온 데이터를 반영
        setViews(album)
        setClickListeners(album)

        //ROOM_DB
        val songs = getSongs(album.id)

        binding.albumBtnArrowBlackIv.setOnClickListener {
            (context as MainActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .commitAllowingStateLoss()
        }

        binding.albumImgAlbumExp2Iv.clipToOutline = true


        val albumAdapter = AlbumViewpagerAdapter(this)
        binding.albumContentVp.adapter = albumAdapter
        TabLayoutMediator(binding.albumContentTb, binding.albumContentVp){
                tab, position ->
            tab.text = information[position]
        }.attach()


        return binding.root
    }


    private fun setViews(album: Album) {
        binding.albumTitleTv.text = album.title.toString()
        binding.albumArtistTv.text = album.singer.toString()
        binding.albumImgAlbumExp2Iv.setImageResource(album.coverImg!!)

        if(isLiked){
            binding.albumIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_on)
        } else {
            binding.albumIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_off)
        }
    }

    private fun setClickListeners(album : Album) {
        val userId : Int = getUserIdx(requireContext())

        binding.albumIcMyLikeOffIv.setOnClickListener{
            if(isLiked){
                binding.albumIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_off)
                disLikedAlbum(userId, album.id)
            } else {
                binding.albumIcMyLikeOffIv.setImageResource(R.drawable.ic_my_like_on)
                likeAlbum(userId, album.id)
            }
        }
    }


    private fun likeAlbum(userId: Int, albumId: Int){
        val songDB = SongDatabase.getInstance(requireContext())!!
        val like = Like(userId, albumId)

        songDB.albumDao().likeAlbum((like))
    }

    private fun isLikedAlbum(albumId: Int) : Boolean {
        val songDB = SongDatabase.getInstance(requireContext())!!
        val userId = getUserIdx(requireContext())

        val likeId : Int? = songDB.albumDao().isLikeAlbum(userId, albumId)

        return likeId != null
    }

    private fun disLikedAlbum(userId: Int,albumId: Int) {
        val songDB = SongDatabase.getInstance(requireContext())!!
        songDB.albumDao().disLikeAlbum(userId, albumId)

    }


    //ROOM_DB
    private fun getSongs(albumIdx : Int) : ArrayList<Song>{
        val songDB = SongDatabase.getInstance(requireContext())!!

        val songs = songDB.songDao().getSongsIntAlbum(albumIdx) as ArrayList

        return songs
    }
}