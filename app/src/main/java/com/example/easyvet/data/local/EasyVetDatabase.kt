package com.example.easyvet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.easyvet.data.local.converters.RoomConverters
import com.example.easyvet.data.local.dao.AdvisoryDao
import com.example.easyvet.data.local.dao.AnimalDao
import com.example.easyvet.data.local.dao.LabReferralDao
import com.example.easyvet.data.local.dao.OutbreakAlertDao
import com.example.easyvet.data.local.dao.SymptomReportDao
import com.example.easyvet.data.local.dao.UserDao
import com.example.easyvet.data.local.entity.AdvisoryEntity
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AnimalEntity::class,
        SymptomReportEntity::class,
        LabReferralEntity::class,
        OutbreakAlertEntity::class,
        AdvisoryEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class EasyVetDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun symptomReportDao(): SymptomReportDao
    abstract fun labReferralDao(): LabReferralDao
    abstract fun outbreakAlertDao(): OutbreakAlertDao
    abstract fun advisoryDao(): AdvisoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: EasyVetDatabase? = null

        fun getInstance(context: Context): EasyVetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EasyVetDatabase::class.java,
                    "easyvet_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateSeedData(database)
                    }
                }
            }
        }

        private suspend fun populateSeedData(db: EasyVetDatabase) {
            db.animalDao().insertAllAnimals(SeedData.initialAnimals)
            db.symptomReportDao().insertAllReports(SeedData.initialSymptomReports)
            db.labReferralDao().insertAllReferrals(SeedData.initialLabReferrals)
            db.outbreakAlertDao().insertAllAlerts(SeedData.initialOutbreakAlerts)
            db.advisoryDao().insertAllAdvisories(SeedData.initialAdvisories)
            for (user in SeedData.initialUsers) {
                db.userDao().insertUser(user)
            }
        }
    }
}
