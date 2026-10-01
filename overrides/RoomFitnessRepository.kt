package com.example.data.repository

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
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.model.WaterLog
import com.example.data.model.WeightLog
import com.example.data.network.RemoteFoodDataSource
import com.example.data.catalog.PreparedDishCatalog
import kotlinx.coroutines.flow.map
import com.example.util.BarcodeUtils
import com.example.domain.repository.FitnessRepository
import com.example.domain.model.BarcodeLookupException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow

class RoomFitnessRepository(
    private val foodDao: FoodDao,
    private val mealDao: MealDao,
    private val weightDao: WeightDao,
    private val waterDao: WaterDao,
    private val exerciseDao: ExerciseDao,
    private val userDao: UserDao,
    private val measurementDao: MeasurementDao,
    private val habitDao: HabitDao,
    private val remoteFoodDataSource: RemoteFoodDataSource,
    private val builtInFoods: List<FoodItem> = emptyList()
) : FitnessRepository {
    private val barcodeLookupMutex = Mutex()

    // Food
    override val allFoods: Flow<List<FoodItem>> = foodDao.getAllFoods().map { saved ->
        (saved + builtInFoods).distinctBy { it.name.lowercase() }
    }
    override fun searchFoods(query: String): Flow<List<FoodItem>> = allFoods.map { foods -> foods.filter { PreparedDishCatalog.matches(it, query) } }
    override fun getFoodsByCategory(category: String): Flow<List<FoodItem>> = allFoods.map { foods -> foods.filter { it.category == category } }
    override suspend fun findFoodByBarcode(barcode: String): FoodItem? = barcodeLookupMutex.withLock {
        val candidates = BarcodeUtils.lookupCandidates(barcode)
        if (candidates.isEmpty()) return@withLock null
        val cached = candidates.firstNotNullOfOrNull { foodDao.findByBarcode(it) }
        // A label entered by the user is an intentional correction and stays authoritative.
        if (cached?.isCustom == true) return@withLock cached

        var failure: BarcodeLookupException? = null
        for (candidate in candidates) {
            try {
                val remote = remoteFoodDataSource.findByBarcode(candidate) ?: continue
                val existing = cached ?: remote.barcode?.let { foodDao.findByBarcode(it) }
                val refreshed = remote.copy(id = existing?.id ?: 0L)
                val id = foodDao.insertFood(refreshed)
                return@withLock refreshed.copy(id = id)
            } catch (error: BarcodeLookupException) {
                failure = error
                // Stop on network/service failure instead of sending the equivalent code again.
                break
            }
        }
        cached?.let { return@withLock it }
        failure?.let { throw it }
        null
    }
    override suspend fun insertFood(foodItem: FoodItem): Long = foodDao.insertFood(foodItem)
    override suspend fun insertFoods(foods: List<FoodItem>) = foodDao.insertFoods(foods)
    override suspend fun deleteFood(id: Long) = foodDao.deleteFood(id)

    // Meals
    override fun getMealsForDate(date: String): Flow<List<MealEntry>> = mealDao.getMealsForDate(date)
    override fun getMealsForDateAndType(date: String, mealType: MealType): Flow<List<MealEntry>> =
        mealDao.getMealsForDateAndType(date, mealType)
    override suspend fun insertMeal(meal: MealEntry): Long = mealDao.insertMeal(meal)
    override suspend fun insertMeals(meals: List<MealEntry>) = mealDao.insertMeals(meals)
    override suspend fun getMealsForDateSync(date: String): List<MealEntry> = mealDao.getMealsForDateSync(date)
    override suspend fun deleteMeal(id: Long) = mealDao.deleteMealById(id)

    // Weight
    override val allWeightLogs: Flow<List<WeightLog>> = weightDao.getAllWeightLogs()
    override suspend fun getAllWeightLogsSync(): List<WeightLog> = weightDao.getAllWeightLogsSync()
    override val latestWeightLog: Flow<WeightLog?> = weightDao.getLatestWeightLog()
    override suspend fun insertWeight(weightLog: WeightLog): Long = weightDao.insertWeight(weightLog)
    override suspend fun deleteWeight(id: Long) = weightDao.deleteWeightById(id)

    // Measurements (Объемы тела)
    override val allMeasurements: Flow<List<BodyMeasurement>> = measurementDao.getAllMeasurements()
    override val latestMeasurement: Flow<BodyMeasurement?> = measurementDao.getLatestMeasurement()
    override suspend fun insertMeasurement(measurement: BodyMeasurement): Long =
        measurementDao.insertMeasurement(measurement)
    override suspend fun deleteMeasurement(id: Long) = measurementDao.deleteMeasurementById(id)

    // Habits & Streaks (Привычки и стрик)
    override fun getHabitForDate(date: String): Flow<DailyHabit?> = habitDao.getHabitForDate(date)
    override val allHabits: Flow<List<DailyHabit>> = habitDao.getAllHabits()
    override suspend fun saveHabit(habit: DailyHabit) = habitDao.saveHabit(habit)

    // Water
    override fun getWaterLogsForDate(date: String): Flow<List<WaterLog>> = waterDao.getWaterLogsForDate(date)
    override suspend fun addWater(date: String, amountMl: Int) {
        waterDao.insertWaterLog(WaterLog(date = date, amountMl = amountMl))
    }
    override suspend fun removeLastWater(date: String) = waterDao.removeLastWaterLog(date)
    override suspend fun clearWaterForDate(date: String) = waterDao.clearWaterForDate(date)

    // Exercise
    override fun getExercisesForDate(date: String): Flow<List<ExerciseEntry>> =
        exerciseDao.getExercisesForDate(date)
    override suspend fun insertExercise(exercise: ExerciseEntry): Long = exerciseDao.insertExercise(exercise)
    override suspend fun deleteExercise(id: Long) = exerciseDao.deleteExerciseById(id)

    // User Profile
    override val userProfile: Flow<UserProfile?> = userDao.getUserProfile()
    override suspend fun saveUserProfile(profile: UserProfile) = userDao.saveUserProfile(profile)
    override suspend fun getUserProfileOnce(): UserProfile? = userDao.getUserProfileOnce()
}

