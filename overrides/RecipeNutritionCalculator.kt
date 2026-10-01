package com.example.domain.nutrition

import com.example.data.model.FoodItem

/** Ingredient calories are conserved within the stated recipe; finished weight includes water. */
object RecipeNutritionCalculator {
    data class Ingredient(val food: FoodItem, val grams: Float)
    data class Nutrition(val calories: Float, val protein: Float, val fat: Float, val carbs: Float) {
        fun portion(grams: Float): Nutrition {
            require(grams.isFinite() && grams > 0f)
            val scale = grams / 100f
            return Nutrition(calories * scale, protein * scale, fat * scale, carbs * scale)
        }
    }

    fun per100Grams(ingredients: List<Ingredient>, finishedWeightGrams: Float): Nutrition {
        require(ingredients.isNotEmpty()) { "Recipe needs ingredients" }
        require(finishedWeightGrams.isFinite() && finishedWeightGrams > 0f)
        val totals = DoubleArray(4)
        ingredients.forEach { ingredient ->
            require(ingredient.grams.isFinite() && ingredient.grams > 0f)
            val values = listOf(ingredient.food.calories, ingredient.food.protein, ingredient.food.fat, ingredient.food.carbs)
            values.forEachIndexed { index, value ->
                require(value.isFinite() && value >= 0f)
                totals[index] += value.toDouble() * ingredient.grams / 100.0
            }
        }
        val result = totals.map { (it * 100.0 / finishedWeightGrams).toFloat() }
        require(result.all { it.isFinite() && it >= 0f })
        require(result[0] <= 1000f && result.drop(1).all { it <= 100f }) {
            "Finished weight is too small for the ingredient nutrition"
        }
        return Nutrition(result[0], result[1], result[2], result[3])
    }
}
