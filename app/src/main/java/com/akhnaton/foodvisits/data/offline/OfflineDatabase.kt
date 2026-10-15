package com.akhnaton.foodvisits.data.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akhnaton.foodvisits.data.offline.dao.ApiCacheDao
import com.akhnaton.foodvisits.data.offline.dao.LocalVisitStartDao
import com.akhnaton.foodvisits.data.offline.dao.PendingSaveVisitDao
import com.akhnaton.foodvisits.data.offline.dao.PendingVisitsCacheDao
import com.akhnaton.foodvisits.data.offline.dao.VisitCacheDao
import com.akhnaton.foodvisits.data.offline.entity.ApiCacheEntity
import com.akhnaton.foodvisits.data.offline.entity.LocalVisitStartEntity
import com.akhnaton.foodvisits.data.offline.entity.PendingSaveVisitEntity
import com.akhnaton.foodvisits.data.offline.entity.PendingVisitsCacheEntity
import com.akhnaton.foodvisits.data.offline.entity.VisitCacheEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VisitCacheEntity::class,
        PendingVisitsCacheEntity::class,
        ApiCacheEntity::class,
        LocalVisitStartEntity::class,
        PendingSaveVisitEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class OfflineDatabase : RoomDatabase() {

    abstract fun visitCacheDao(): VisitCacheDao
    abstract fun pendingVisitsCacheDao(): PendingVisitsCacheDao
    abstract fun apiCacheDao(): ApiCacheDao
    abstract fun localVisitStartDao(): LocalVisitStartDao
    abstract fun pendingSaveVisitDao(): PendingSaveVisitDao


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
        fun currentUserId(): String =
            com.akhnaton.foodvisits.shared.SharedPreferencesHelper.getInstance().getEmployeeId()

    }
}