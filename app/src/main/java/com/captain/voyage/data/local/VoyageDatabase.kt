package com.captain.voyage.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.captain.voyage.data.model.Building // Added
import com.captain.voyage.data.model.Settlement // Added
import com.captain.voyage.data.model.DailyLog
import com.captain.voyage.data.model.Goal
import com.captain.voyage.data.model.Item
import com.captain.voyage.data.model.Market
import com.captain.voyage.data.model.Port
import com.captain.voyage.data.model.PriceHistory
import com.captain.voyage.data.model.Rule
import com.captain.voyage.data.model.ScoreRecord
import com.captain.voyage.data.model.Ship
import com.captain.voyage.data.model.ShipInventory
import com.captain.voyage.data.model.UserStatus

// ★ 버전 9로 변경 (Settlement 관련 테이블 추가)
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
        PriceHistory::class,
        Settlement::class,
        Building::class
    ],
    version = 11, // Version up (Foreign Key added)
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
    abstract fun tradeDao(): TradeDao
    abstract fun settlementDao(): SettlementDao

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