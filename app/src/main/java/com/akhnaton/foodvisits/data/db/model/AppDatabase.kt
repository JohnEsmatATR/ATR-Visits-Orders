package com.akhnaton.foodvisits.data.db.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akhnaton.foodvisits.data.db.dao.VisitTimerDao

@Database(entities = [VisitTimerEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun visitTimerDao(): VisitTimerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "visit_timer_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
