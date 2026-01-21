package com.example.mediacraft.di

import android.content.Context
import androidx.room.Room
import com.example.mediacraft.data.local.AppDatabase
import com.example.mediacraft.data.local.dao.RecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "media_craft.db"
        ).build()
    }

    // 只需要对外暴露 DAO，Repository 不需要知道 Database 的存在
    @Provides
    fun provideRecordDao(database: AppDatabase): RecordDao {
        return database.recordDao()
    }
}