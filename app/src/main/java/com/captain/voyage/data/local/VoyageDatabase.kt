package com.captain.voyage.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.Item // Added
import com.captain.voyage.data.model.Market // Added
import com.captain.voyage.data.model.Port // Added
import com.captain.voyage.data.model.PriceHistory // Added
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipInventory // Added
import com.captain.voyage.data.model.UserStatus

// ★ 버전 7로 변경
@Database(
    entities = [
        Rule::class,
        Ship::class,
        UserStatus::class,
        DailyLog::class,
        ScoreRecord::class,
        Goal::class,
        Port::class,
        Item::class,
        Market::class,
        ShipInventory::class,
        PriceHistory::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class) // 기존 번역가 유지
abstract class VoyageDatabase : RoomDatabase() {

    // 1. 기존 DAO
    abstract fun voyageDao(): VoyageDao
    abstract fun scoreRecordDao(): ScoreRecordDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun goalDao(): GoalDao
    abstract fun portDao(): PortDao

    // 2. 신규 DAO 추가 예정 (무역/항해)
    // abstract fun tradeDao(): TradeDao

    companion object {
        @Volatile
        private var INSTANCE: VoyageDatabase? = null

        fun getDatabase(context: Context): VoyageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoyageDatabase::class.java,
                    "voyage_database"
                )
                    // ★ 중요: DB 구조가 바뀌었으므로, 앱 삭제 안 해도 에러 없이 기존 데이터를 날리고 새로 만듦
                    // (개발 중에는 이 옵션이 충돌 방지에 최고입니다)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}