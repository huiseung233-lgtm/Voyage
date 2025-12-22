package com.captain.voyage.data.local

import androidx.room.TypeConverter
import com.captain.voyage.data.model.ScoreRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- List<ScoreRecord> 변환기 (DailyLog용) ---
    @TypeConverter
    fun fromScoreRecordList(value: List<ScoreRecord>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toScoreRecordList(value: String): List<ScoreRecord> {
        val listType = object : TypeToken<List<ScoreRecord>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // --- List<Long> 변환기 (Goal 연결된 룰 ID 목록용) ---
    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}