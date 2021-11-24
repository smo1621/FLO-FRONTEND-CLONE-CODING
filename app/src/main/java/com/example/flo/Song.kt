package com.example.flo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//제목, 가수, 사진, 재생시간, 현재 재생시간, 재생유무, 음악, 커버 이미지, 좋아요유무, 앨범ID
@Entity(tableName = "SongTable")
data class Song(
    var title : String = "",
    var singer : String = "",
    var second : Int = 0,
    var playTime : Int = 0,
    var isPlaying : Boolean = false,
    var music : String = "",
    var coverImg : Int? = null,
    var isLike : Boolean = false,
    var albumIdx : Int = 0 // song이 어느 앨범인지 가리키는 변수
){
    @PrimaryKey(autoGenerate = true) var id : Int = 0
}
