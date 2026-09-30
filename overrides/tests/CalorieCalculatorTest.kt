package com.example

import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.GoalPace
import com.example.util.CalorieCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun testBmrCalculationMale() {
        // Male: 80 kg, 180 cm, 30 years -> (10*80) + (6.25*180) - (5*30) + 5 = 800 + 1125 - 150 + 5 = 1780
        val bmr = CalorieCalculator.calculateBmr(Gender.MALE, 80f, 180f, 30)
        assertEquals(1780f, bmr, 0.1f)
    }

    @Test
    fun testBmrCalculationFemale() {
        // Female: 65 kg, 165 cm, 28 years -> (10*65) + (6.25*165) - (5*28) - 161 = 650 + 1031.25 - 140 - 161 = 1380.25
        val bmr = CalorieCalculator.calculateBmr(Gender.FEMALE, 65f, 165f, 28)
        assertEquals(1380.25f, bmr, 0.1f)
    }

    @Test
    fun testBmiCalculation() {
        val result = CalorieCalculator.calculateBmi(70f, 175f)
        // 70 / (1.75^2) = 70 / 3.0625 = 22.857 -> rounded to 22.9
        assertEquals(22.9f, result.bmi, 0.1f)
        assertEquals("Нормальный вес", result.categoryRu)
    }

    @Test
    fun testRecommendedTargets() {
        val targets = CalorieCalculator.calculateRecommendedTargets(
            gender = Gender.MALE,
            age = 28,
            heightCm = 175f,
            currentWeightKg = 82f,
            activityLevel = ActivityLevel.MODERATE,
            goalPace = GoalPace.RECOMMENDED
        )

        assertTrue(targets.calories > 1500)
        assertTrue(targets.proteinGrams >= 130)
        assertTrue(targets.fatGrams in 40..100)
        assertTrue(targets.carbGrams > 50)
        assertTrue(targets.waterMl >= 2000)
    }
    @Test
    fun automaticTargets_rejectUnsupportedMinorProfile() {
        var failed = false
        try {
            CalorieCalculator.calculateRecommendedTargets(
                gender = Gender.MALE,
                age = 17,
                heightCm = 175f,
                currentWeightKg = 70f,
                activityLevel = ActivityLevel.MODERATE,
                goalPace = GoalPace.RECOMMENDED,
                targetWeightKg = 65f
            )
        } catch (_: IllegalArgumentException) {
            failed = true
        }
        assertTrue(failed)
    }

    @Test
    fun automaticTargets_capDeficitAndKeepMacroCaloriesWithinBudget() {
        val targets = CalorieCalculator.calculateRecommendedTargets(
            gender = Gender.FEMALE,
            age = 30,
            heightCm = 165f,
            currentWeightKg = 55f,
            activityLevel = ActivityLevel.SEDENTARY,
            goalPace = GoalPace.MAX,
            targetWeightKg = 52f
        )

        val macroCalories = targets.proteinGrams * 4 + targets.fatGrams * 9 + targets.carbGrams * 4
        assertTrue(targets.calories >= (targets.tdee * 0.8f).toInt())
        assertTrue(macroCalories <= targets.calories + 4)
    }

}
