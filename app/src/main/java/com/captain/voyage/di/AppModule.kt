package com.captain.voyage.di

import android.content.Context
import androidx.room.Room
import com.captain.voyage.data.local.DailyLogDao
import com.captain.voyage.data.local.ScoreRecordDao
import com.captain.voyage.data.local.VoyageDao
import com.captain.voyage.data.local.VoyageDatabase
import com.captain.voyage.data.repository.VoyageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
    fun provideVoyageRepository(
        voyageDao: VoyageDao,
        scoreRecordDao: ScoreRecordDao,
        dailyLogDao: DailyLogDao
    ): VoyageRepository {
        return VoyageRepository(voyageDao, scoreRecordDao, dailyLogDao)
    }
}
