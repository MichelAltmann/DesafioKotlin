package com.android.desafiokotlin.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Filme(
    @PrimaryKey
    val id: Int?,
    val title: String?,
    val release_date: String?,
    val backdrop_path: String?,
    val overview: String?,
    val popularity: Number?,
    val original_title: String?,
    val vote_average: Number?,
    val vote_count: Int?
){
    val filme: Filme
        get() = Filme(
            id ?: 0,
            title ?: "",
            release_date ?: "",
            backdrop_path ?: "",
            overview ?: "",
            popularity ?: 0,
            original_title ?: "",
            vote_average ?: 0,
            vote_count ?: 0
        )
}

