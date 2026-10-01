package com.example.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.FoodItem
import com.example.data.network.RemoteFoodDataSource
import com.example.domain.model.BarcodeLookupException
import com.example.domain.model.BarcodeLookupException.Reason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RoomFitnessRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: RoomFitnessRepository
    private var remoteLookup: suspend (String) -> FoodItem? = { null }
    private val calls = mutableListOf<String>()
    private val barcode = "4820001234567"
    private fun food(custom: Boolean = false) = FoodItem(
        name = "Bread", category = "Крупы", calories = 240f, protein = 8f,
        fat = 2f, carbs = 47f, barcode = barcode, isCustom = custom
    )

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomFitnessRepository(
            db.foodDao(), db.mealDao(), db.weightDao(), db.waterDao(),
            db.exerciseDao(), db.userDao(), db.measurementDao(), db.habitDao(),
            object : RemoteFoodDataSource {
                override suspend fun findByBarcode(barcode: String): FoodItem? {
                    calls.add(barcode)
                    return remoteLookup(barcode)
                }
            }
        )
    }

    @After fun tearDown() = db.close()

    @Test fun barcodeLookup_roundTripsThroughRoom() = runTest {
        repository.insertFood(food(custom = true))
        assertNotNull(repository.findFoodByBarcode(barcode))
        assertEquals(1, repository.allFoods.first().size)
    }

    @Test fun freshBarcodeUsesOnlineAndCachesProductWithRealId() = runTest {
        remoteLookup = { food() }
        val found = repository.findFoodByBarcode(barcode)!!
        assertEquals(listOf(barcode), calls)
        assertTrue(found.id > 0)
        assertEquals(found, db.foodDao().findByBarcode(barcode))
    }

    @Test fun onlineRefreshPrecedesCacheAndDoesNotDuplicateRows() = runTest {
        val id = repository.insertFood(food())
        remoteLookup = { food().copy(calories = 245f) }
        repeat(2) {
            val found = repository.findFoodByBarcode(barcode)!!
            assertEquals(id, found.id)
            assertEquals(245f, found.calories, 0f)
        }
        assertEquals(2, calls.size)
        assertEquals(1, repository.allFoods.first().size)
    }

    @Test fun cachedProductWorksWhenOffline() = runTest {
        repository.insertFood(food())
        remoteLookup = { throw BarcodeLookupException(Reason.NETWORK) }
        assertNotNull(repository.findFoodByBarcode(barcode))
        assertEquals(1, calls.size)
    }

    @Test fun offlineWithoutCachePropagatesNetworkError() = runTest {
        remoteLookup = { throw BarcodeLookupException(Reason.NETWORK) }
        try {
            repository.findFoodByBarcode(barcode)
            fail("Expected explicit offline error")
        } catch (error: BarcodeLookupException) {
            assertEquals(Reason.NETWORK, error.reason)
        }
    }

    @Test fun upcMissTriesEquivalentEanOnline() = runTest {
        remoteLookup = { code -> if (code == "0123456789012") food().copy(barcode = code) else null }
        assertNotNull(repository.findFoodByBarcode("123456789012"))
        assertEquals(listOf("123456789012", "0123456789012"), calls)
    }

    @Test fun absentProductReturnsNullAfterOnlineLookup() = runTest {
        assertNull(repository.findFoodByBarcode(barcode))
        assertEquals(listOf(barcode), calls)
    }

    @Test fun incompleteProductIsReportedSeparatelyAndNotCached() = runTest {
        remoteLookup = { throw BarcodeLookupException(Reason.INCOMPLETE_NUTRITION, "Bread") }
        try {
            repository.findFoodByBarcode(barcode)
            fail("Expected incomplete nutrition")
        } catch (error: BarcodeLookupException) {
            assertEquals(Reason.INCOMPLETE_NUTRITION, error.reason)
        }
        assertTrue(repository.allFoods.first().isEmpty())
    }

    @Test fun customLabelValuesRemainAuthoritative() = runTest {
        val id = repository.insertFood(food(custom = true))
        remoteLookup = { food().copy(calories = 999f) }
        val found = repository.findFoodByBarcode(barcode)!!
        assertEquals(id, found.id)
        assertEquals(240f, found.calories, 0f)
        assertTrue(calls.isEmpty())
    }

    @Test fun cancellationNeverFallsBackToCache() = runTest {
        repository.insertFood(food())
        remoteLookup = { throw CancellationException("cancelled") }
        try {
            repository.findFoodByBarcode(barcode)
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            // Expected.
        }
    }

    @Test fun builtInDishesAreSearchableWithoutWritingOrResettingRoom() = runTest {
        repository.insertFood(food(custom = true))
        val dish = FoodItem(id = -1_000_000L, name = "Борщ", category = "Супы", calories = 50f,
            protein = 3f, fat = 2f, carbs = 5f)
        val withDishes = RoomFitnessRepository(db.foodDao(), db.mealDao(), db.weightDao(), db.waterDao(),
            db.exerciseDao(), db.userDao(), db.measurementDao(), db.habitDao(),
            object : RemoteFoodDataSource { override suspend fun findByBarcode(barcode: String): FoodItem? = null },
            builtInFoods = listOf(dish))
        assertEquals(2, withDishes.allFoods.first().size)
        assertEquals(listOf(dish), withDishes.searchFoods("борщ").first())
        assertEquals(listOf(dish), withDishes.getFoodsByCategory("Супы").first())
        assertEquals(1, db.foodDao().getAllFoods().first().size)
    }
}
