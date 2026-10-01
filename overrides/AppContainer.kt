package com.example.app

import android.content.Context
import com.example.data.auth.AccountManager
import com.example.data.db.AppDatabase
import com.example.data.catalog.PreparedDishCatalog
import com.example.data.repository.AdminConfigManager
import com.example.data.network.OpenFoodFactsClient
import com.example.data.repository.RoomFitnessRepository
import com.example.domain.repository.FitnessRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Small manual dependency-injection container. It keeps Android wiring out of Activity/ViewModel
 * and gives tests one stable seam to replace repository implementations.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(appContext, applicationScope)
    }

    val fitnessRepository: FitnessRepository by lazy {
        RoomFitnessRepository(
            foodDao = database.foodDao(),
            mealDao = database.mealDao(),
            weightDao = database.weightDao(),
            waterDao = database.waterDao(),
            exerciseDao = database.exerciseDao(),
            userDao = database.userDao(),
            measurementDao = database.measurementDao(),
            habitDao = database.habitDao(),
            remoteFoodDataSource = OpenFoodFactsClient(),
            builtInFoods = PreparedDishCatalog.load(appContext)
        )
    }

    val accountManager: AccountManager by lazy { AccountManager(appContext) }

    val adminConfigManager: AdminConfigManager by lazy {
        accountManager
        AdminConfigManager.getInstance(appContext)
    }
}
