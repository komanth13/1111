package com.example.util

import com.example.data.model.Gender
import com.example.data.model.UserProfile
import kotlin.math.max
import kotlin.math.roundToInt

enum class ProfileField {
    NAME,
    AGE,
    HEIGHT,
    START_WEIGHT,
    CURRENT_WEIGHT,
    TARGET_WEIGHT,
    CALORIES,
    PROTEIN,
    FAT,
    CARBS,
    WATER,
    STEPS,
    HOUSEHOLD
}

data class ProfileValidationResult(
    val errors: Map<ProfileField, StringKey> = emptyMap()
) {
    val isValid: Boolean get() = errors.isEmpty()
    fun errorFor(field: ProfileField): StringKey? = errors[field]
}

/**
 * Central validation policy for profile data and nutrition calculations.
 *
 * The automatic weight-loss calculator is intentionally adult-only. The numeric limits below are
 * supported-input guardrails: they prevent impossible/corrupted values and keep formulas away from
 * divisions, overflows and extreme outputs. They are not a diagnosis or a substitute for clinical
 * guidance.
 */
object ProfileValidator {
    const val MIN_ADULT_AGE = 18
    const val MAX_SUPPORTED_AGE = 100

    const val MIN_HEIGHT_CM = 120f
    const val MAX_HEIGHT_CM = 230f

    const val MIN_WEIGHT_KG = 30f
    const val MAX_WEIGHT_KG = 250f

    const val MAX_NAME_LENGTH = 80

    const val MAX_CALORIES = 6000
    const val MAX_PROTEIN_GRAMS = 300
    const val MAX_FAT_GRAMS = 200
    const val MAX_CARB_GRAMS = 700
    const val MIN_WATER_ML = 500
    const val MAX_WATER_ML = 6000

    fun minAdultCalories(gender: Gender): Int = if (gender == Gender.MALE) 1500 else 1200

    fun parseDecimal(value: String): Float? = value
        .trim()
        .replace(',', '.')
        .toFloatOrNull()
        ?.takeIf { it.isFinite() }

    fun parseInteger(value: String): Int? = value.trim().toIntOrNull()

    fun validate(profile: UserProfile): ProfileValidationResult {
        val errors = linkedMapOf<ProfileField, StringKey>()

        if (profile.name.length > MAX_NAME_LENGTH) {
            errors[ProfileField.NAME] = StringKey.PROFILE_ERROR_NAME_TOO_LONG
        }

        if (profile.age !in MIN_ADULT_AGE..MAX_SUPPORTED_AGE) {
            errors[ProfileField.AGE] = if (profile.age in 1 until MIN_ADULT_AGE) {
                StringKey.PROFILE_ERROR_ADULT_ONLY
            } else {
                StringKey.PROFILE_ERROR_AGE_RANGE
            }
        }

        if (!profile.heightCm.isFinite() || profile.heightCm !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
            errors[ProfileField.HEIGHT] = StringKey.PROFILE_ERROR_HEIGHT_RANGE
        }

        if (profile.startWeightKg != 0f && !isSupportedWeight(profile.startWeightKg)) {
            errors[ProfileField.START_WEIGHT] = StringKey.PROFILE_ERROR_WEIGHT_RANGE
        }

        if (!isSupportedWeight(profile.currentWeightKg)) {
            errors[ProfileField.CURRENT_WEIGHT] = StringKey.PROFILE_ERROR_WEIGHT_RANGE
        }

        if (!isSupportedWeight(profile.targetWeightKg)) {
            errors[ProfileField.TARGET_WEIGHT] = StringKey.PROFILE_ERROR_WEIGHT_RANGE
        } else if (isSupportedWeight(profile.currentWeightKg) && profile.targetWeightKg > profile.currentWeightKg) {
            errors[ProfileField.TARGET_WEIGHT] = StringKey.PROFILE_ERROR_TARGET_ABOVE_CURRENT
        } else if (isTargetBelowSupportedAdultRange(profile.heightCm, profile.targetWeightKg)) {
            errors[ProfileField.TARGET_WEIGHT] = StringKey.PROFILE_ERROR_TARGET_TOO_LOW
        }

        if (profile.dailyCalorieTarget != 0) {
            val minCalories = minimumSupportedCalorieTarget(profile)
            if (profile.dailyCalorieTarget !in minCalories..MAX_CALORIES) {
                errors[ProfileField.CALORIES] = StringKey.PROFILE_ERROR_CALORIES_RANGE
            }
        }

        if (profile.proteinTargetGrams !in 0..MAX_PROTEIN_GRAMS) {
            errors[ProfileField.PROTEIN] = StringKey.PROFILE_ERROR_MACRO_RANGE
        }
        if (profile.fatTargetGrams !in 0..MAX_FAT_GRAMS) {
            errors[ProfileField.FAT] = StringKey.PROFILE_ERROR_MACRO_RANGE
        }
        if (profile.carbTargetGrams !in 0..MAX_CARB_GRAMS) {
            errors[ProfileField.CARBS] = StringKey.PROFILE_ERROR_MACRO_RANGE
        }
        if (profile.waterGoalMl != 0 && profile.waterGoalMl !in MIN_WATER_ML..MAX_WATER_ML) {
            errors[ProfileField.WATER] = StringKey.PROFILE_ERROR_WATER_RANGE
        }

        if (profile.averageDailySteps != null && profile.averageDailySteps !in 0..50000)
            errors[ProfileField.STEPS] = StringKey.PROFILE_ERROR_STEPS
        if (profile.householdMinutes != null && profile.householdMinutes !in 0..480)
            errors[ProfileField.HOUSEHOLD] = StringKey.PROFILE_ERROR_HOUSEHOLD
        return ProfileValidationResult(errors)
    }

    fun validateForCalculations(profile: UserProfile): ProfileValidationResult {
        val errors = linkedMapOf<ProfileField, StringKey>()

        if (profile.age !in MIN_ADULT_AGE..MAX_SUPPORTED_AGE) {
            errors[ProfileField.AGE] = if (profile.age in 1 until MIN_ADULT_AGE) {
                StringKey.PROFILE_ERROR_ADULT_ONLY
            } else {
                StringKey.PROFILE_ERROR_AGE_RANGE
            }
        }
        if (!profile.heightCm.isFinite() || profile.heightCm !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) {
            errors[ProfileField.HEIGHT] = StringKey.PROFILE_ERROR_HEIGHT_RANGE
        }
        if (!isSupportedWeight(profile.currentWeightKg)) {
            errors[ProfileField.CURRENT_WEIGHT] = StringKey.PROFILE_ERROR_WEIGHT_RANGE
        }
        if (!isSupportedWeight(profile.targetWeightKg)) {
            errors[ProfileField.TARGET_WEIGHT] = StringKey.PROFILE_ERROR_WEIGHT_RANGE
        } else if (isSupportedWeight(profile.currentWeightKg) && profile.targetWeightKg > profile.currentWeightKg) {
            errors[ProfileField.TARGET_WEIGHT] = StringKey.PROFILE_ERROR_TARGET_ABOVE_CURRENT
        } else if (isTargetBelowSupportedAdultRange(profile.heightCm, profile.targetWeightKg)) {
            errors[ProfileField.TARGET_WEIGHT] = StringKey.PROFILE_ERROR_TARGET_TOO_LOW
        }

        if (profile.averageDailySteps != null && profile.averageDailySteps !in 0..50000)
            errors[ProfileField.STEPS] = StringKey.PROFILE_ERROR_STEPS
        if (profile.householdMinutes != null && profile.householdMinutes !in 0..480)
            errors[ProfileField.HOUSEHOLD] = StringKey.PROFILE_ERROR_HOUSEHOLD
        return ProfileValidationResult(errors)
    }

    fun normalizeForSave(profile: UserProfile): UserProfile {
        val normalizedCurrent = profile.currentWeightKg.takeIf { isSupportedWeight(it) } ?: profile.currentWeightKg
        return profile.copy(
            name = profile.name.trim().take(MAX_NAME_LENGTH),
            startWeightKg = if (profile.startWeightKg == 0f && isSupportedWeight(normalizedCurrent)) normalizedCurrent else profile.startWeightKg
        )
    }

    fun minimumSupportedCalorieTarget(profile: UserProfile): Int {
        val absoluteFloor = minAdultCalories(profile.gender)
        if (!isSupportedAdultBodyData(profile.age, profile.heightCm, profile.currentWeightKg)) {
            return absoluteFloor
        }
        val bmr = CalorieCalculator.calculateBmr(profile.gender, profile.currentWeightKg, profile.heightCm, profile.age)
        val tdee = CalorieCalculator.calculateTdee(bmr, profile.activityLevel, profile.averageDailySteps?.takeIf { it in 0..50000 }, profile.householdMinutes?.takeIf { it in 0..480 })
        return max(absoluteFloor, (tdee * 0.80f).roundToInt())
    }

    fun isTargetBelowSupportedAdultRange(heightCm: Float, targetWeightKg: Float): Boolean {
        if (!heightCm.isFinite() || heightCm !in MIN_HEIGHT_CM..MAX_HEIGHT_CM || !isSupportedWeight(targetWeightKg)) return false
        val heightM = heightCm / 100f
        val targetBmi = targetWeightKg / (heightM * heightM)
        return targetBmi.isFinite() && targetBmi < 18.5f
    }

    fun isSupportedWeight(weightKg: Float): Boolean =
        weightKg.isFinite() && weightKg in MIN_WEIGHT_KG..MAX_WEIGHT_KG

    fun isSupportedAdultBodyData(age: Int, heightCm: Float, weightKg: Float): Boolean =
        age in MIN_ADULT_AGE..MAX_SUPPORTED_AGE &&
            heightCm.isFinite() && heightCm in MIN_HEIGHT_CM..MAX_HEIGHT_CM &&
            isSupportedWeight(weightKg)
}
