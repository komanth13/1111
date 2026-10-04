package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender(val titleRu: String) {
    MALE("Мужской"),
    FEMALE("Женский")
}

enum class ActivityLevel(val titleRu: String, val descriptionRu: String, val multiplier: Float) {
    SEDENTARY("Сидячий", "До 3000 шагов в день, преимущественно сидя", 1.2f),
    LIGHT("Легкая активность", "3000–6000 шагов, прогулки и обычные домашние дела", 1.375f),
    MODERATE("Умеренная активность", "6000–10000 шагов, много времени на ногах", 1.55f),
    ACTIVE("Высокая активность", "Более 10000 шагов, физическая работа или регулярный спорт", 1.725f)
}

enum class GoalPace(val titleRu: String, val weeklyKg: Float, val deficitKcal: Int) {
    EASY("Мягкий темп (-0.25 кг/нед)", 0.25f, 250),
    RECOMMENDED("Оптимальный (-0.5 кг/нед)", 0.5f, 500),
    FAST("Интенсивный (-0.75 кг/нед)", 0.75f, 750),
    MAX("Максимальный (-1.0 кг/нед)", 1.0f, 1000)
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val age: Int = 0,
    val heightCm: Float = 0f,
    val startWeightKg: Float = 0f,
    val currentWeightKg: Float = 0f,
    val targetWeightKg: Float = 0f,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val averageDailySteps: Int? = null,
    val householdMinutes: Int? = null,
    val goalPace: GoalPace = GoalPace.RECOMMENDED,
    val dailyCalorieTarget: Int = 0,
    val proteinTargetGrams: Int = 0,
    val fatTargetGrams: Int = 0,
    val carbTargetGrams: Int = 0,
    val waterGoalMl: Int = 0,
    val fastingHoursTarget: Int = 16,
    val fastingStartTime: Long = 0L,
    val isFastingActive: Boolean = false
)
