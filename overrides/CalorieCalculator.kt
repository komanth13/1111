package com.example.util

import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.GoalPace
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object CalorieCalculator {

    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayString(): String = DATE_FORMAT.format(Date())

    fun formatDateDisplay(
        dateStr: String,
        lang: AppLanguage = LocalizationManager.currentLanguage.value
    ): String {
        return try {
            val date = DATE_FORMAT.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance()
            val todayStr = DATE_FORMAT.format(cal.time)

            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = DATE_FORMAT.format(cal.time)

            cal.add(Calendar.DAY_OF_YEAR, 2)
            val tomorrowStr = DATE_FORMAT.format(cal.time)

            val locale = when (lang) {
                AppLanguage.RU -> Locale("ru")
                AppLanguage.UK -> Locale("uk")
                AppLanguage.EN -> Locale.ENGLISH
            }

            val pattern = if (lang == AppLanguage.EN) "MMM d" else "d MMM"
            val yearPattern = if (lang == AppLanguage.EN) "MMM d, yyyy" else "d MMM yyyy"
            val shortDate = SimpleDateFormat(pattern, locale).format(date)

            val todayPrefix = when (lang) {
                AppLanguage.RU -> "Сегодня, "
                AppLanguage.UK -> "Сьогодні, "
                AppLanguage.EN -> "Today, "
            }
            val yesterdayPrefix = when (lang) {
                AppLanguage.RU -> "Вчера, "
                AppLanguage.UK -> "Вчора, "
                AppLanguage.EN -> "Yesterday, "
            }
            val tomorrowPrefix = when (lang) {
                AppLanguage.RU -> "Завтра, "
                AppLanguage.UK -> "Завтра, "
                AppLanguage.EN -> "Tomorrow, "
            }

            when (dateStr) {
                todayStr -> todayPrefix + shortDate
                yesterdayStr -> yesterdayPrefix + shortDate
                tomorrowStr -> tomorrowPrefix + shortDate
                else -> SimpleDateFormat(yearPattern, locale).format(date)
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun addDaysToDate(dateStr: String, days: Int): String {
        return try {
            val date = DATE_FORMAT.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            DATE_FORMAT.format(cal.time)
        } catch (e: Exception) {
            getTodayString()
        }
    }

    fun calculateBmr(gender: Gender, weightKg: Float, heightCm: Float, age: Int): Float {
        val base = (10f * weightKg) + (6.25f * heightCm) - (5f * age)
        return if (gender == Gender.MALE) base + 5f else base - 161f
    }

    /** Approximate lifestyle estimate; steps include movement at home and work.
     * Use the larger of walking/household estimates, never their sum, to avoid counting
     * the same movement twice. Empty fields use the explicitly selected lifestyle level.
     * These bands are an app heuristic, not a validated pedometer calorimetry equation.
     */
    fun activityMultiplier(activityLevel: ActivityLevel, averageDailySteps: Int? = null,
        householdMinutes: Int? = null): Float {
        require(averageDailySteps == null || averageDailySteps in 0..50000)
        require(householdMinutes == null || householdMinutes in 0..480)
        if (averageDailySteps == null && householdMinutes == null) return activityLevel.multiplier
        val walking = when {
            averageDailySteps == null || averageDailySteps < 3000 -> 1.2f
            averageDailySteps < 6000 -> 1.3f
            averageDailySteps < 9000 -> 1.4f
            averageDailySteps < 12000 -> 1.5f
            else -> 1.6f
        }
        val household = when {
            householdMinutes == null || householdMinutes < 30 -> 1.2f
            householdMinutes < 60 -> 1.3f
            householdMinutes < 120 -> 1.4f
            else -> 1.5f
        }
        return max(walking, household)
    }

    fun calculateTdee(bmr: Float, activityLevel: ActivityLevel, averageDailySteps: Int? = null,
        householdMinutes: Int? = null): Float =
        bmr * activityMultiplier(activityLevel, averageDailySteps, householdMinutes)

    data class Targets(
        val bmr: Int,
        val tdee: Int,
        val calories: Int,
        val proteinGrams: Int,
        val fatGrams: Int,
        val carbGrams: Int,
        val waterMl: Int,
        val appliedDeficit: Int = 0,
        val estimatedWeeklyKg: Float = 0f
    )

    fun calculateRecommendedTargets(
        gender: Gender,
        age: Int,
        heightCm: Float,
        currentWeightKg: Float,
        activityLevel: ActivityLevel,
        goalPace: GoalPace,
        targetWeightKg: Float? = null,
        averageDailySteps: Int? = null,
        householdMinutes: Int? = null
    ): Targets {
        require(ProfileValidator.isSupportedAdultBodyData(age, heightCm, currentWeightKg)) {
            "Automatic nutrition calculations require supported adult profile data."
        }
        if (targetWeightKg != null) {
            require(
                ProfileValidator.isSupportedWeight(targetWeightKg) &&
                    targetWeightKg <= currentWeightKg &&
                    !ProfileValidator.isTargetBelowSupportedAdultRange(heightCm, targetWeightKg)
            ) {
                "Target weight is outside the supported weight-loss range."
            }
        }

        val bmr = calculateBmr(gender, currentWeightKg, heightCm, age)
        val tdee = calculateTdee(bmr, activityLevel, averageDailySteps, householdMinutes).coerceAtLeast(bmr)

        // Keep the selected pace as a preference, but cap the calculated deficit to a conservative
        // share of estimated daily expenditure. If the goal has already been reached, use maintenance.
        val goalReached = targetWeightKg != null && targetWeightKg >= currentWeightKg
        val maxDeficitFromTdee = (tdee * 0.20f).roundToInt().coerceAtLeast(0)
        val appliedDeficit = if (goalReached) 0 else min(goalPace.deficitKcal, maxDeficitFromTdee)

        val minimumCalories = ProfileValidator.minAdultCalories(gender)
        val targetCalories = max(minimumCalories, (tdee - appliedDeficit).roundToInt())
            .coerceAtMost(ProfileValidator.MAX_CALORIES)

        // Allocate macros without allowing the macro calories to exceed the calorie target.
        val minimumCarbGrams = 50
        val minimumFatGrams = 40
        val reservedCalories = minimumCarbGrams * 4 + minimumFatGrams * 9
        val maxProteinByBudget = max(50, (targetCalories - reservedCalories) / 4)
        // A practical protein target for ordinary weight loss, using goal weight for
        // excess weight rather than prescribing athlete-level protein per current kg.
        val weightAtBmi25 = 25f * (heightCm / 100f) * (heightCm / 100f)
        val proteinReferenceKg = min(currentWeightKg, max(targetWeightKg ?: currentWeightKg, weightAtBmi25))
        val desiredProtein = (proteinReferenceKg * 1.6f).roundToInt()
        val proteinGrams = desiredProtein.coerceIn(50, min(250, maxProteinByBudget))

        val caloriesAfterProtein = max(0, targetCalories - proteinGrams * 4)
        val desiredFatGrams = ((targetCalories * 0.28f) / 9f).roundToInt()
        val maxFatByBudget = max(minimumFatGrams, (caloriesAfterProtein - minimumCarbGrams * 4) / 9)
        val fatGrams = desiredFatGrams.coerceIn(minimumFatGrams, min(120, maxFatByBudget))

        val remainingKcal = max(0, targetCalories - proteinGrams * 4 - fatGrams * 9)
        val carbGrams = (remainingKcal / 4).coerceAtLeast(0)

        val waterMl = (currentWeightKg * 33f).roundToInt().coerceIn(1500, 4000)

        return Targets(
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            calories = targetCalories,
            proteinGrams = proteinGrams,
            fatGrams = fatGrams,
            carbGrams = carbGrams,
            waterMl = waterMl,
            appliedDeficit = (tdee.roundToInt() - targetCalories).coerceAtLeast(0),
            estimatedWeeklyKg = ((tdee - targetCalories).coerceAtLeast(0f) * 7f / 7700f)
        )
    }

    data class BmiResult(
        val bmi: Float,
        val categoryRu: String,
        val recommendationRu: String
    )

    fun calculateBmi(weightKg: Float, heightCm: Float): BmiResult {
        if (!weightKg.isFinite() || !heightCm.isFinite() || weightKg <= 0f || heightCm <= 0f) {
            return BmiResult(0f, "Неизвестно", "")
        }
        val heightM = heightCm / 100f
        val bmi = weightKg / (heightM * heightM)
        if (!bmi.isFinite()) return BmiResult(0f, "Неизвестно", "")
        val rounded = (bmi * 10).roundToInt() / 10f

        return when {
            rounded < 18.5f -> BmiResult(
                rounded,
                "Дефицит веса",
                "Рекомендуется здоровое сбалансированное питание для набора массы."
            )
            rounded in 18.5f..24.9f -> BmiResult(
                rounded,
                "Нормальный вес",
                "Отличный здоровый диапазон! Фокусируйтесь на качестве тела и рекомпозиции."
            )
            rounded in 25.0f..29.9f -> BmiResult(
                rounded,
                "Избыточный вес",
                "Умеренный дефицит калорий поможет безопасно снизить вес без стресса."
            )
            else -> BmiResult(
                rounded,
                "Ожирение",
                "Рекомендуется стабильный дефицит 500-700 ккал и регулярная ходьба."
            )
        }
    }

    fun estimateTargetWeeks(currentWeight: Float, targetWeight: Float, weeklyKg: Float): Int {
        if (!currentWeight.isFinite() || !targetWeight.isFinite() || !weeklyKg.isFinite()) return 0
        if (weeklyKg <= 0f || currentWeight <= targetWeight || currentWeight <= 0f || targetWeight <= 0f) return 0
        val remainingKg = currentWeight - targetWeight
        val supportedWeeklyPace = min(weeklyKg, min(1.0f, currentWeight * 0.01f)).coerceAtLeast(0.1f)
        return (remainingKg / supportedWeeklyPace).roundToInt().coerceAtLeast(1)
    }
}
