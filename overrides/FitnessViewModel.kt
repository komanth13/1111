package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActiveGuestAccess
import com.example.data.model.AdminPaymentDetails
import com.example.data.model.AppConfig
import com.example.data.model.AppFeature
import com.example.data.model.BodyMeasurement
import com.example.data.model.DailyHabit
import com.example.data.model.ExerciseEntry
import com.example.data.model.FoodItem
import com.example.data.model.GuestInvitePass
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.PaymentMethodType
import com.example.data.model.PaymentReceipt
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserProfile
import com.example.data.model.WeightLog
import com.example.data.repository.AdminConfigManager
import com.example.domain.repository.FitnessRepository
import com.example.domain.analytics.NutritionSummaryCalculator
import com.example.domain.analytics.StreakCalculator
import com.example.domain.analytics.WeeklyAnalyticsBuilder
import com.example.domain.model.DailyNutritionSummary
import com.example.domain.model.DayAnalytics
import com.example.security.SecurityPolicy
import com.example.data.auth.AccountManager
import com.example.util.CalorieCalculator
import com.example.util.ProfileValidationResult
import com.example.util.ProfileValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class FitnessViewModel(
    private val repository: FitnessRepository,
    private val adminConfigManager: AdminConfigManager,
    val accountManager: AccountManager? = null
) : ViewModel() {

    private val weeklyAnalyticsBuilder = WeeklyAnalyticsBuilder(repository::getMealsForDateSync)

    val adminAccess: StateFlow<Boolean> = SecurityPolicy.accountAccess.map { SecurityPolicy.adminToolsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentConfig: StateFlow<AppConfig> = adminConfigManager.currentConfig
    val userTier: StateFlow<SubscriptionTier> = adminConfigManager.userTier
    val activeGuestAccess: StateFlow<ActiveGuestAccess?> = adminConfigManager.activeGuestAccess
    val savedInvites: StateFlow<List<GuestInvitePass>> = adminConfigManager.savedInvites
    val paymentDetails: StateFlow<AdminPaymentDetails> = adminConfigManager.paymentDetails
    val latestReceipt: StateFlow<PaymentReceipt?> = adminConfigManager.latestReceipt

    var showPaywallDialog by mutableStateOf(false)
        private set
    var paywallTriggerFeature by mutableStateOf<AppFeature?>(null)
        private set

    var showAdminPanelDialog by mutableStateOf(false)
        private set

    var showActivatePromoDialog by mutableStateOf(false)
        private set

    var showPaymentCheckoutDialog by mutableStateOf(false)
        private set
    var checkoutTier by mutableStateOf(SubscriptionTier.SLIM)
        private set

    var showPaymentReceiptDialog by mutableStateOf(false)
        private set
    var activeReceiptForDisplay by mutableStateOf<PaymentReceipt?>(null)
        private set

    fun openPaymentCheckout(tier: SubscriptionTier) {
        checkoutTier = tier
        showPaymentCheckoutDialog = true
    }

    fun closePaymentCheckout() {
        showPaymentCheckoutDialog = false
    }

    fun openPaymentReceipt(receipt: PaymentReceipt) {
        activeReceiptForDisplay = receipt
        showPaymentReceiptDialog = true
    }

    fun closePaymentReceipt() {
        showPaymentReceiptDialog = false
        activeReceiptForDisplay = null
    }

    fun submitPayment(
        tier: SubscriptionTier,
        amount: String,
        methodType: PaymentMethodType,
        payerNote: String
    ): PaymentReceipt {
        val receipt = adminConfigManager.submitPaymentForVerification(tier, amount, methodType, payerNote)
        openPaymentReceipt(receipt)
        return receipt
    }

    fun updatePaymentDetails(details: AdminPaymentDetails) {
        adminConfigManager.updatePaymentDetails(details)
    }

    fun openPaywall(feature: AppFeature? = null) {
        paywallTriggerFeature = feature
        showPaywallDialog = true
    }

    fun closePaywall() {
        showPaywallDialog = false
        paywallTriggerFeature = null
    }

    val adminToolsEnabled: Boolean
        get() = SecurityPolicy.adminToolsEnabled

    fun openAdminPanel() {
        if (SecurityPolicy.adminToolsEnabled) {
            showAdminPanelDialog = true
        }
    }

    fun closeAdminPanel() {
        showAdminPanelDialog = false
    }

    val promoActivationEnabled: Boolean
        get() = SecurityPolicy.localPromoActivationEnabled

    fun openActivatePromoDialog() {
        if (SecurityPolicy.localPromoActivationEnabled) {
            showActivatePromoDialog = true
        }
    }

    fun closeActivatePromoDialog() {
        showActivatePromoDialog = false
    }

    fun activatePromoCode(input: String): Result<ActiveGuestAccess> {
        return adminConfigManager.activateCodeOrUrl(input)
    }

    fun revokeGuestAccess() {
        adminConfigManager.revokeGuestAccess()
    }

    fun createGuestInvite(tier: SubscriptionTier, durationDays: Int, label: String, customCode: String? = null): GuestInvitePass {
        return adminConfigManager.createInvite(tier, durationDays, label, customCode)
    }

    fun deleteGuestInvite(code: String) {
        adminConfigManager.deleteInvite(code)
    }

    fun selectTierFromPaywall(tier: SubscriptionTier) {
        adminConfigManager.selectTierFromPaywall(tier)
    }

    fun setUserTierForDebug(tier: SubscriptionTier) {
        adminConfigManager.setUserTierForDebug(tier)
    }

    fun isFeatureUnlocked(feature: AppFeature): Boolean {
        return adminConfigManager.isFeatureUnlocked(feature)
    }

    fun importAdminConfigFile(jsonText: String, fileName: String): Result<AppConfig> {
        val result = adminConfigManager.applyJsonConfig(jsonText, fileName)
        result.onSuccess { config ->
            if (config.extraFoods.isNotEmpty()) {
                viewModelScope.launch {
                    val foods = config.extraFoods.map { dto ->
                        FoodItem(
                            name = dto.name,
                            category = dto.category,
                            calories = dto.calories.toFloat(),
                            protein = dto.protein,
                            fat = dto.fat,
                            carbs = dto.carbs,
                            defaultServingGrams = 100f,
                            isCustom = true
                        )
                    }
                    repository.insertFoods(foods)
                }
            }
        }
        return result
    }

    fun resetConfigToDefaults() {
        adminConfigManager.resetToDefaults()
    }

    fun exportConfigJson(): String {
        return adminConfigManager.exportConfigJson()
    }

    private val _selectedDate = MutableStateFlow(CalorieCalculator.getTodayString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val currentMeals: StateFlow<List<MealEntry>> = _selectedDate
        .flatMapLatest { date -> repository.getMealsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentWaterLogs = _selectedDate
        .flatMapLatest { date -> repository.getWaterLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentExercises: StateFlow<List<ExerciseEntry>> = _selectedDate
        .flatMapLatest { date -> repository.getExercisesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightLogs: StateFlow<List<WeightLog>> = repository.allWeightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWeightLog: StateFlow<WeightLog?> = repository.latestWeightLog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Body Measurements
    val allMeasurements: StateFlow<List<BodyMeasurement>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMeasurement: StateFlow<BodyMeasurement?> = repository.latestMeasurement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Habits & Streaks
    val currentHabit: StateFlow<DailyHabit> = _selectedDate
        .flatMapLatest { date -> repository.getHabitForDate(date) }
        .map { it ?: DailyHabit(date = _selectedDate.value) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyHabit(date = _selectedDate.value))

    val allHabits: StateFlow<List<DailyHabit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStreak: StateFlow<Int> = allHabits.map { habits ->
        StreakCalculator.calculate(habits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Weekly Analytics Days (last 7 days)
    val weeklyDaysAnalytics: StateFlow<List<DayAnalytics>> = combine(
        userProfile,
        _selectedDate,
        com.example.util.LocalizationManager.currentLanguage
    ) { profile, _, lang ->
        weeklyAnalyticsBuilder.build(
            endDate = _selectedDate.value,
            targetKcal = profile.dailyCalorieTarget,
            locale = when (lang) {
                com.example.util.AppLanguage.RU -> Locale("ru")
                com.example.util.AppLanguage.UK -> Locale("uk")
                com.example.util.AppLanguage.EN -> Locale.ENGLISH
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Each screen owns its filters; a database search must not shrink the diary catalogue.
    val foodCatalog: StateFlow<List<FoodItem>> = repository.allFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummary: StateFlow<DailyNutritionSummary> = combine(
        currentMeals,
        currentExercises,
        currentWaterLogs,
        userProfile
    ) { meals, exercises, waters, profile ->
        NutritionSummaryCalculator.calculate(meals, exercises, waters, profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyNutritionSummary())

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun changeDateBy(days: Int) {
        _selectedDate.value = CalorieCalculator.addDaysToDate(_selectedDate.value, days)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    suspend fun findFoodByBarcode(barcode: String): FoodItem? =
        repository.findFoodByBarcode(barcode)

    suspend fun saveBarcodeFood(food: FoodItem): FoodItem =
        food.copy(id = repository.insertFood(food))

    fun addMeal(
        mealType: MealType,
        foodName: String,
        grams: Float,
        calories: Float,
        protein: Float,
        fat: Float,
        carbs: Float
    ) {
        viewModelScope.launch {
            repository.insertMeal(
                MealEntry(
                    date = _selectedDate.value,
                    mealType = mealType,
                    foodName = foodName,
                    grams = grams,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carbs = carbs
                )
            )
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(_selectedDate.value, amountMl)
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            repository.removeLastWater(_selectedDate.value)
        }
    }

    fun addExercise(activityName: String, durationMinutes: Int, caloriesBurned: Int) {
        viewModelScope.launch {
            repository.insertExercise(
                ExerciseEntry(
                    date = _selectedDate.value,
                    activityName = activityName,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned
                )
            )
        }
    }

    fun deleteExercise(id: Long) {
        viewModelScope.launch {
            repository.deleteExercise(id)
        }
    }

    fun logWeight(weightKg: Float, note: String, date: String = _selectedDate.value) {
        viewModelScope.launch {
            repository.insertWeight(
                WeightLog(
                    date = date,
                    weightKg = weightKg,
                    note = note
                )
            )
            // Keep profile weight in sync with a value the user actually entered.
            // For a brand-new profile, the first logged weight also becomes the start weight.
            val current = userProfile.value
            repository.saveUserProfile(
                current.copy(
                    currentWeightKg = weightKg,
                    startWeightKg = if (current.startWeightKg > 0f) current.startWeightKg else weightKg
                )
            )
        }
    }

    fun deleteWeight(id: Long) {
        viewModelScope.launch {
            repository.deleteWeight(id)
        }
    }

    fun saveCustomFood(
        name: String,
        category: String,
        calories: Float,
        protein: Float,
        fat: Float,
        carbs: Float,
        defaultServing: Float = 100f
    ) {
        viewModelScope.launch {
            repository.insertFood(
                FoodItem(
                    name = name,
                    category = category.ifBlank { "Другое" },
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carbs = carbs,
                    defaultServingGrams = defaultServing,
                    isCustom = true
                )
            )
        }
    }

    fun deleteFoodItem(id: Long) {
        viewModelScope.launch {
            repository.deleteFood(id)
        }
    }

    fun updateUserProfile(profile: UserProfile): ProfileValidationResult {
        val normalized = ProfileValidator.normalizeForSave(profile)
        val validation = ProfileValidator.validate(normalized)
        if (!validation.isValid) return validation

        viewModelScope.launch {
            repository.saveUserProfile(normalized)
        }
        return validation
    }

    fun toggleFasting() {
        viewModelScope.launch {
            val current = userProfile.value
            val isNowActive = !current.isFastingActive
            val startTime = if (isNowActive) System.currentTimeMillis() else 0L
            repository.saveUserProfile(
                current.copy(
                    isFastingActive = isNowActive,
                    fastingStartTime = startTime
                )
            )
        }
    }

    fun copyYesterdayMeals() {
        viewModelScope.launch {
            val yesterdayStr = CalorieCalculator.addDaysToDate(_selectedDate.value, -1)
            val yesterdayMeals = repository.getMealsForDateSync(yesterdayStr)
            if (yesterdayMeals.isNotEmpty()) {
                val cloned = yesterdayMeals.map { old ->
                    old.copy(
                        id = 0,
                        date = _selectedDate.value,
                        timestamp = System.currentTimeMillis()
                    )
                }
                repository.insertMeals(cloned)
            }
        }
    }

    fun addQuickMeal(mealType: MealType, foodName: String, calories: Float, protein: Float, fat: Float, carbs: Float) {
        addMeal(
            mealType = mealType,
            foodName = foodName,
            grams = 100f,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs
        )
    }

    fun saveHabit(habit: DailyHabit) {
        viewModelScope.launch {
            repository.saveHabit(habit)
        }
    }

    fun logMeasurement(
        waistCm: Float?,
        hipsCm: Float?,
        chestCm: Float?,
        bicepCm: Float?,
        thighsCm: Float?,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertMeasurement(
                BodyMeasurement(
                    date = _selectedDate.value,
                    waistCm = waistCm,
                    hipsCm = hipsCm,
                    chestCm = chestCm,
                    bicepCm = bicepCm,
                    thighsCm = thighsCm,
                    note = note
                )
            )
        }
    }

    fun autoCalculateAndSaveTargets(profileOverride: UserProfile? = null): ProfileValidationResult {
        val baseProfile = ProfileValidator.normalizeForSave(profileOverride ?: userProfile.value)
        val validation = ProfileValidator.validateForCalculations(baseProfile)
        if (!validation.isValid) return validation

        val targets = CalorieCalculator.calculateRecommendedTargets(
            gender = baseProfile.gender,
            age = baseProfile.age,
            heightCm = baseProfile.heightCm,
            currentWeightKg = baseProfile.currentWeightKg,
            activityLevel = baseProfile.activityLevel,
            goalPace = baseProfile.goalPace,
            targetWeightKg = baseProfile.targetWeightKg
        )
        val updated = baseProfile.copy(
            dailyCalorieTarget = targets.calories,
            proteinTargetGrams = targets.proteinGrams,
            fatTargetGrams = targets.fatGrams,
            carbTargetGrams = targets.carbGrams,
            waterGoalMl = targets.waterMl
        )
        viewModelScope.launch {
            repository.saveUserProfile(updated)
        }
        return validation
    }
}

class FitnessViewModelFactory(
    private val repository: FitnessRepository,
    private val adminConfigManager: AdminConfigManager,
    private val accountManager: AccountManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            return FitnessViewModel(repository, adminConfigManager, accountManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

