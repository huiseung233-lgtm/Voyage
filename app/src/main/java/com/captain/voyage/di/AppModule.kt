package com.captain.voyage.di

import android.content.Context
import androidx.room.Room
import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.GoalDao // Added
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.local.VoyageDatabase
import com.captain.voyage.data.repository.VoyageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VoyageDatabase {
        return Room.databaseBuilder(
            context,
            VoyageDatabase::class.java,
            "voyage_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideVoyageDao(db: VoyageDatabase): VoyageDao {
        return db.voyageDao()
    }

    @Provides
    @Singleton
    fun provideScoreRecordDao(db: VoyageDatabase): ScoreRecordDao {
        return db.scoreRecordDao()
    }

    @Provides
    @Singleton
    fun provideDailyLogDao(db: VoyageDatabase): DailyLogDao {
        return db.dailyLogDao()
    }

    @Provides
    @Singleton
    fun provideGoalDao(db: VoyageDatabase): GoalDao { // Added
        return db.goalDao()
    }

    @Provides
    @Singleton
    fun providePortDao(db: VoyageDatabase): com.captain.voyage.data.local.PortDao {
        return db.portDao()
    }

    @Provides
    @Singleton
    fun provideTradeDao(db: VoyageDatabase): com.captain.voyage.data.local.TradeDao {
        return db.tradeDao()
    }

    @Provides
    @Singleton
    fun provideSettlementDao(db: VoyageDatabase): com.captain.voyage.data.local.SettlementDao {
        return db.settlementDao()
    }

    @Provides
    @Singleton
    fun provideTradeRepository(
        tradeDao: com.captain.voyage.data.local.TradeDao,
        voyageDao: VoyageDao
    ): com.captain.voyage.data.repository.TradeRepository {
        return com.captain.voyage.data.repository.TradeRepository(tradeDao, voyageDao)
    }

    @Provides
    @Singleton
    fun provideExploredMapDao(db: VoyageDatabase): com.captain.voyage.data.local.ExploredMapDao {
        return db.exploredMapDao()
    }

    @Provides
    @Singleton
    fun provideWorldRepository(
        portDao: com.captain.voyage.data.local.PortDao,
        tradeDao: com.captain.voyage.data.local.TradeDao,
        exploredMapDao: com.captain.voyage.data.local.ExploredMapDao // Added
    ): com.captain.voyage.data.repository.WorldRepository {
        return com.captain.voyage.data.repository.WorldRepository(portDao, tradeDao, exploredMapDao)
    }

    @Provides
    @Singleton
    fun provideSettlementRepository(
        settlementDao: com.captain.voyage.data.local.SettlementDao,
        portDao: com.captain.voyage.data.local.PortDao,
        voyageDao: VoyageDao
    ): com.captain.voyage.data.repository.SettlementRepository {
        return com.captain.voyage.data.repository.SettlementRepository(settlementDao, portDao, voyageDao)
    }

    @Provides
    @Singleton
    fun provideGoalRepository(
        goalDao: GoalDao,
        scoreRecordDao: ScoreRecordDao,
        dailyLogDao: DailyLogDao
    ): com.captain.voyage.data.repository.GoalRepository {
        return com.captain.voyage.data.repository.GoalRepository(goalDao, scoreRecordDao, dailyLogDao)
    }

    @Provides
    @Singleton
    fun provideVoyageRepository(
        voyageDao: VoyageDao,
        appScope: CoroutineScope
    ): VoyageRepository {
        return VoyageRepository(voyageDao, appScope)
    }
}
