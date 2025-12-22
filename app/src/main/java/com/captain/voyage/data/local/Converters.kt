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

    // Enum들은 Room이 기본적으로 처리할 수도 있지만,
    // 명시적으로 String으로 변환해주면 나중에 데이터를 직접 열어볼 때 보기 편합니다.
    // 필요 시 여기에 Enum 변환기도 추가할 수 있습니다.
}