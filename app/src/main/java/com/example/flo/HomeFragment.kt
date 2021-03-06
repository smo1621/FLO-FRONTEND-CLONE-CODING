package com.example.flo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.flo.databinding.FragmentHomeBinding
import com.google.gson.Gson


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private var albums = ArrayList<Album>()

    private lateinit var songDB: SongDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        //Room_DB
        songDB = SongDatabase.getInstance(requireContext())!!
        albums.addAll(songDB.albumDao().getAlbums()) //songdb에서 album list 가져오기

        // 레이아웃 매니저 설정
        binding.homeTodayMusicAlbumRecyclerview.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)


        // 더미데이터와 Adapter 연결
        val albumRVAdapter = AlbumRVAdapter(albums)


        // 리스너 객체 생성 및 전달
        albumRVAdapter.setMyItemClickListener(object : AlbumRVAdapter.MyItemClickListener{
            override fun onItemClick(album: Album) {
                changeAlbumFragment(album)
            }

            override fun onRemoveAlbum(position: Int) {
                albumRVAdapter.removeItem(position)
            }
        })

        // 리사이클러뷰에 Adapter 연결
        binding.homeTodayMusicAlbumRecyclerview.adapter = albumRVAdapter

        val bannerAdapter = BannerViewpagerAdapter(this)
        bannerAdapter.addFragemnt(BannerFragment(R.drawable.img_home_viewpager_exp))
        bannerAdapter.addFragemnt(BannerFragment(R.drawable.img_home_viewpager_exp2))

        binding.homeBannerVp.adapter = bannerAdapter
        binding.homeBannerVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL


        return binding.root
    }

    private fun changeAlbumFragment(album: Album) {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, AlbumFragment().apply {
                arguments = Bundle().apply {
                    val gson = Gson()
                    val albumJson = gson.toJson(album)
                    putString("album", albumJson)
                }
            })
            .commitAllowingStateLoss()
    }


}