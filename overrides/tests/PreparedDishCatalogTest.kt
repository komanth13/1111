package com.example.data.catalog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.nutrition.RecipeNutritionCalculator
import com.example.util.AppLanguage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PreparedDishCatalogTest {
    private lateinit var snapshot: PreparedDishCatalog.Snapshot
    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        snapshot = PreparedDishCatalog.parse(context.assets.open("prepared_dishes.json").bufferedReader().use { it.readText() })
        PreparedDishCatalog.install(snapshot)
    }

    @Test fun everyDishHasUniqueNamesIdentifiersAndAllThreeTranslations() {
        val dishes = snapshot.entries.filter { PreparedDishCatalog.isDish(it.food) }
        assertEquals(325, dishes.size)
        assertEquals(34, dishes.count { it.cuisine == "Закарпатская" })
        assertEquals(snapshot.entries.size, snapshot.foods.map { it.id }.distinct().size)
        dishes.forEach { dish -> AppLanguage.entries.forEach { lang ->
            assertFalse(PreparedDishCatalog.translatedName(dish.food.name, lang).isNullOrBlank())
        } }
    }

    @Test fun ukrainianRussianEnglishAndAliasesFindReadyDishes() {
        val expectations = mapOf("борщ" to "Борщ украинский с говядиной", "борщ український" to "Борщ украинский с говядиной",
            "borscht" to "Борщ украинский с говядиной", "сирники" to "Сырники жареные",
            "битки" to "Биточки мясные в томатном соусе", "бануш" to "Банош с брынзой",
            "гомбовці" to "Гомбовцы творожные", "пивные" to "Гренки чесночные к пиву")
        expectations.forEach { (query, expectedName) ->
            assertTrue("No match for $query", snapshot.foods.any { it.name == expectedName && PreparedDishCatalog.matches(it, query) })
        }
    }

    @Test fun dishModeDoesNotShowIndividualIngredients() {
        val dishes = snapshot.foods.filter { PreparedDishCatalog.accepts(it, PreparedDishCatalog.Kind.DISHES) }
        val products = snapshot.foods.filter { PreparedDishCatalog.accepts(it, PreparedDishCatalog.Kind.PRODUCTS) }
        assertEquals(325, dishes.size)
        assertEquals(107, products.size)
        assertTrue(dishes.none { it in products })
    }

    @Test fun everyDishNutritionIsCalculatedFromItsOwnRecipeAndFinishedYield() {
        snapshot.entries.filter { it.components.isNotEmpty() }.forEach { entry ->
            val expected = RecipeNutritionCalculator.per100Grams(entry.components, entry.finishedWeightGrams)
            assertEquals(expected.calories, entry.food.calories, 0.001f)
            assertEquals(expected.protein, entry.food.protein, 0.001f)
            assertEquals(expected.fat, entry.food.fat, 0.001f)
            assertEquals(expected.carbs, entry.food.carbs, 0.001f)
            assertTrue(entry.food.barcode == null)
        }
    }

    @Test fun multilingualCuisineQueriesFindTranscarpathianFood() {
        val bograch = snapshot.foods.first { it.name == "Бограч закарпатский" }
        assertTrue(PreparedDishCatalog.matches(bograch, "закарпатська"))
        assertTrue(PreparedDishCatalog.matches(bograch, "Transcarpathian"))
        assertEquals("Закарпатская", PreparedDishCatalog.cuisine(bograch))
    }

    @Test fun customRecipesAreDishesWithoutBorrowingStandardRecipeValues() {
        val standard = snapshot.foods.first { it.name == "Бограч закарпатский" }
        val custom = standard.copy(id = 123, isCustom = true, category = "Готовые блюда", calories = 77f)
        assertTrue(PreparedDishCatalog.isDish(custom))
        assertNull(PreparedDishCatalog.entry(custom))
        assertEquals("Домашняя", PreparedDishCatalog.cuisine(custom))
    }
}
