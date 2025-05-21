package com.loyalstring.rfid.di

import com.loyalstring.rfid.repository.BulkRepository
import com.loyalstring.rfid.repository.BulkRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BulkModule {

    @Binds
    abstract fun bindBulkRepository(
        impl: BulkRepositoryImpl
    ): BulkRepository
}
