package com.akhnaton.foodvisits.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.akhnaton.foodvisits.data.db.dao.SaveVisitDao
import com.akhnaton.foodvisits.data.db.dao.VisitPlanDao
import com.akhnaton.foodvisits.data.db.model.SaveVisitDB
import com.akhnaton.foodvisits.data.model.ListVisitPlanConverters
import com.akhnaton.foodvisits.data.model.SaveVisitConverters
import com.akhnaton.foodvisits.data.model.VisitPlanDataConverters
import com.akhnaton.foodvisits.data.model.VisitsPlan

@Database(entities = [VisitsPlan::class, SaveVisitDB::class], version = 11)
@TypeConverters(ListVisitPlanConverters::class, SaveVisitConverters::class, VisitPlanDataConverters::class)
abstract class VisitDatabase : RoomDatabase() {
    abstract fun saveVisitDao(): SaveVisitDao
    abstract fun visitPlanDao(): VisitPlanDao

    companion object {
        @Volatile
        private var INSTANCE: VisitDatabase? = null

        fun getDatabase(context: Context): VisitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    VisitDatabase::class.java,
                    "visit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}