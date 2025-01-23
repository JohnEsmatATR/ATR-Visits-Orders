package com.akhnaton.foodvisits.data.db

import android.content.Context
import androidx.room.*
import com.akhnaton.foodvisits.data.db.dao.SaveVisitDao
import com.akhnaton.foodvisits.data.db.dao.VisitPlanDao
import com.akhnaton.foodvisits.data.db.model.SaveVisitDB
import com.akhnaton.foodvisits.data.model.*
import com.akhnaton.foodvisits.data.model.visits.saveVisit.SaveVisit

@Database(entities = [VisitsPlan::class, SaveVisitDB::class], version = 10)
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