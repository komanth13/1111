package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExerciseDao
import com.example.data.dao.FoodDao
import com.example.data.dao.HabitDao
import com.example.data.dao.MealDao
import com.example.data.dao.MeasurementDao
import com.example.data.dao.UserDao
import com.example.data.dao.WaterDao
import com.example.data.dao.WeightDao
import com.example.data.model.BodyMeasurement
import com.example.data.model.DailyHabit
import com.example.data.model.ExerciseEntry
import com.example.data.model.FoodItem
import com.example.data.model.MealEntry
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FoodItem::class,
        MealEntry::class,
        WeightLog::class,
        WaterLog::class,
        ExerciseEntry::class,
        UserProfile::class,
        BodyMeasurement::class,
        DailyHabit::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao
    abstract fun weightDao(): WeightDao
    abstract fun waterDao(): WaterDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userDao(): UserDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "slimtrack_database"
                )
                    .addMigrations(*DatabaseMigrations.ALL)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateReferenceData(database)
                    }
                }
            }

            private suspend fun populateReferenceData(database: AppDatabase) {
                // This is reference/catalog data, not invented user history.
                // No meals, weight logs, water, measurements, habits, exercise or profile
                // are generated for a new production user.
                if (database.foodDao().getCount() == 0) {
                    database.foodDao().insertFoods(DefaultFoodData.foods)
                }
            }
        }
    }
}
