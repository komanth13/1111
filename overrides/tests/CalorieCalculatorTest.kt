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


    @Test fun ordinaryWalkingUsesDeficitAndGoalWeightProtein() {
        val result = CalorieCalculator.calculateRecommendedTargets(Gender.FEMALE, 37, 168f,
            105.5f, ActivityLevel.ACTIVE, GoalPace.RECOMMENDED, 89f, 6000, 45)
        assertEquals(1759, result.bmr)
        assertEquals(2463, result.tdee)
        assertEquals(1970, result.calories)
        assertEquals(493, result.appliedDeficit)
        assertEquals(142, result.proteinGrams)
        assertEquals(61, result.fatGrams)
        assertEquals(213, result.carbGrams)
        assertTrue(result.estimatedWeeklyKg in 0.44f..0.46f)
    }

    @Test fun householdAndStepsDoNotStackOrInheritOldSportLevel() {
        assertEquals(1.4f, CalorieCalculator.activityMultiplier(ActivityLevel.ACTIVE, 6000, 60), 0.001f)
        assertEquals(1.2f, CalorieCalculator.activityMultiplier(ActivityLevel.ACTIVE, 0, 0), 0.001f)
        assertEquals(1.4f, CalorieCalculator.activityMultiplier(ActivityLevel.SEDENTARY, null, 60), 0.001f)
        assertEquals(ActivityLevel.ACTIVE.multiplier,
            CalorieCalculator.activityMultiplier(ActivityLevel.ACTIVE), 0.001f)
    }

    @Test fun lifestyleBandsAreMonotonicAndContinuousAtEveryBoundary() {
        var previous = 0f
        for (steps in listOf(0, 2999, 3000, 5999, 6000, 8999, 9000, 11999, 12000, 50000)) {
            val multiplier = CalorieCalculator.activityMultiplier(ActivityLevel.ACTIVE, steps, 0)
            assertTrue(multiplier >= previous)
            previous = multiplier
        }
    }

    @Test fun zeroActivityIsDifferentFromUnknownAndInvalidValuesAreRejected() {
        assertTrue(CalorieCalculator.activityMultiplier(ActivityLevel.MODERATE, 0, null) <
            CalorieCalculator.activityMultiplier(ActivityLevel.MODERATE))
        for (steps in listOf(-1, 50001)) {
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                CalorieCalculator.activityMultiplier(ActivityLevel.LIGHT, steps, null)
            }
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            CalorieCalculator.activityMultiplier(ActivityLevel.LIGHT, null, 481)
        }
    }

    @Test fun reachedGoalUsesMaintenanceAndMacrosNeverExceedCalories() {
        for (gender in Gender.entries) for (level in ActivityLevel.entries) for (pace in GoalPace.entries) {
            val result = CalorieCalculator.calculateRecommendedTargets(gender, 37, 168f,
                89f, level, pace, 89f, 6000, 45)
            assertEquals(result.tdee, result.calories)
            assertEquals(0, result.appliedDeficit)
            assertEquals(0f, result.estimatedWeeklyKg, 0.001f)
            val macros = result.proteinGrams * 4 + result.fatGrams * 9 + result.carbGrams * 4
            assertTrue(macros <= result.calories)
            assertTrue(result.calories - macros <= 3)
        }
    }

    @Test fun minimumValidationUsesTheSameActivityEstimateAsCalculator() {
        val profile = com.example.data.model.UserProfile(gender = Gender.FEMALE, age = 37,
            heightCm = 168f, currentWeightKg = 105.5f, targetWeightKg = 89f,
            activityLevel = ActivityLevel.ACTIVE, averageDailySteps = 6000, householdMinutes = 45)
        val plan = CalorieCalculator.calculateRecommendedTargets(profile.gender, profile.age,
            profile.heightCm, profile.currentWeightKg, profile.activityLevel, profile.goalPace,
            profile.targetWeightKg, profile.averageDailySteps, profile.householdMinutes)
        assertTrue(com.example.util.ProfileValidator.validate(profile.copy(dailyCalorieTarget = plan.calories)).isValid)
        assertTrue(!com.example.util.ProfileValidator.validateForCalculations(profile.copy(averageDailySteps = -1)).isValid)
        assertTrue(!com.example.util.ProfileValidator.validateForCalculations(profile.copy(householdMinutes = 481)).isValid)
        assertEquals(ActivityLevel.SEDENTARY, com.example.data.model.UserProfile().activityLevel)
    }
}
