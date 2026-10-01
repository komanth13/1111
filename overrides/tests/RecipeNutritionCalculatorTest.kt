package com.example.domain.nutrition

import com.example.data.model.FoodItem
import org.junit.Assert.*
import org.junit.Test

class RecipeNutritionCalculatorTest {
    private val ingredient = FoodItem(name = "Ingredient", category = "Test", calories = 100f,
        protein = 10f, fat = 5f, carbs = 3.75f)
    private val water = FoodItem(name = "Water", category = "Test", calories = 0f,
        protein = 0f, fat = 0f, carbs = 0f)

    @Test fun soupWaterReducesDensityWithoutChangingTotalCalories() {
        val recipe = listOf(RecipeNutritionCalculator.Ingredient(ingredient, 200f),
            RecipeNutritionCalculator.Ingredient(water, 300f))
        val per100 = RecipeNutritionCalculator.per100Grams(recipe, 500f)
        assertEquals(40f, per100.calories, 0.001f)
        assertEquals(4f, per100.protein, 0.001f)
        assertEquals(200f, per100.portion(500f).calories, 0.001f)
    }

    @Test fun cookedWeightControlsDensityAndPortionCalculations() {
        val recipe = listOf(RecipeNutritionCalculator.Ingredient(ingredient, 500f))
        val per100 = RecipeNutritionCalculator.per100Grams(recipe, 400f)
        assertEquals(125f, per100.calories, 0f)
        assertEquals(312.5f, per100.portion(250f).calories, 0f)
        assertEquals(31.25f, per100.portion(250f).protein, 0f)
    }

    @Test fun ingredientWeightsScaleEveryMacro() {
        val recipe = listOf(RecipeNutritionCalculator.Ingredient(ingredient, 150f))
        val nutrition = RecipeNutritionCalculator.per100Grams(recipe, 150f)
        assertEquals(100f, nutrition.calories, 0f)
        assertEquals(10f, nutrition.protein, 0f)
        assertEquals(5f, nutrition.fat, 0f)
        assertEquals(3.75f, nutrition.carbs, 0f)
    }

    @Test fun zeroFinishedWeightIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            RecipeNutritionCalculator.per100Grams(listOf(RecipeNutritionCalculator.Ingredient(ingredient, 100f)), 0f)
        }
    }

    @Test fun emptyRecipeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { RecipeNutritionCalculator.per100Grams(emptyList(), 100f) }
    }

    @Test fun invalidIngredientWeightIsRejected() {
        for (weight in listOf(-1f, 0f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) {
                RecipeNutritionCalculator.per100Grams(listOf(RecipeNutritionCalculator.Ingredient(ingredient, weight)), 100f)
            }
        }
    }

    @Test fun impossibleNutrientDensityIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            RecipeNutritionCalculator.per100Grams(listOf(RecipeNutritionCalculator.Ingredient(ingredient, 1000f)), 1f)
        }
    }
}
