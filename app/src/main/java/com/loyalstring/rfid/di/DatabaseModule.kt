package com.loyalstring.rfid.di

import android.content.Context
import androidx.room.Room
import com.loyalstring.rfid.data.local.dao.BulkItemDao
import com.loyalstring.rfid.data.local.dao.DropdownDao
import com.loyalstring.rfid.data.local.db.AppDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_db"
        ).build()
    }

    @Provides
    fun provideDropdownDao(db: AppDatabase): DropdownDao {
        return db.dropdownDao()
    }

    @Provides
    fun provideBulkItemDao(db: AppDatabase): BulkItemDao {
        return db.bulkItemDao()
    }
}
