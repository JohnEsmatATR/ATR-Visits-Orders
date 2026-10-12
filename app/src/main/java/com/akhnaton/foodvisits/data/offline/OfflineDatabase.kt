package com.akhnaton.foodvisits.data.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akhnaton.foodvisits.data.offline.dao.PendingVisitsCacheDao
import com.akhnaton.foodvisits.data.offline.dao.VisitCacheDao
import com.akhnaton.foodvisits.data.offline.entity.PendingVisitsCacheEntity
import com.akhnaton.foodvisits.data.offline.entity.VisitCacheEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VisitCacheEntity::class, PendingVisitsCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {

    abstract fun visitCacheDao(): VisitCacheDao
    abstract fun pendingVisitsCacheDao(): PendingVisitsCacheDao

    companion object {
        @Volatile
        private var INSTANCE: OfflineDatabase? = null

        fun getInstance(context: Context): OfflineDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDatabase::class.java,
                    "offline_cache.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }

        fun clearCache(context: Context) {
            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                getInstance(appContext).clearAllTables()
            }
        }
    }
}