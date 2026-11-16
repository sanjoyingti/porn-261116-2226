package com.aibuilt.porn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoEntity::class,
        TorEntity::class,
        FavoriteEntity::class,
        HiddenEntity::class,
        RecentSearchEntity::class,
        DeadLinkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun torDao(): TorDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun hiddenDao(): HiddenDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun deadLinkDao(): DeadLinkDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "porn.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
