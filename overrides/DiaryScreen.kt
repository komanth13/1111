package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppFeature
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.ui.components.AddExerciseDialog
import com.example.ui.components.AddFoodDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.HabitStreakCard
import com.example.ui.components.LeadMagnetBannerCard
import com.example.ui.components.NutrientProgressGauge
import com.example.ui.components.QuickCaloriesDialog
import com.example.ui.components.WaterTrackerCard
import com.example.ui.components.WeeklyDeficitCard
import com.example.ui.theme.BurnedPurple
import com.example.ui.theme.CarbRose
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.ui.viewmodel.FitnessViewModel
import com.example.util.CalorieCalculator
import com.example.util.StringKey
import com.example.util.appString
import com.example.util.localizedFoodName
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
    viewModel: FitnessViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val meals by viewModel.currentMeals.collectAsStateWithLifecycle()
    val exercises by viewModel.currentExercises.collectAsStateWithLifecycle()
    val foodCatalog by viewModel.foodCatalog.collectAsStateWithLifecycle()
    val currentHabit by viewModel.currentHabit.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val weeklyDays by viewModel.weeklyDaysAnalytics.collectAsStateWithLifecycle()
    val currentTier by viewModel.userTier.collectAsStateWithLifecycle()

    var showAddFoodMealType by remember { mutableStateOf<MealType?>(null) }
    var showQuickCaloriesMealType by remember { mutableStateOf<MealType?>(null) }
    var showBarcodeScannerMealType by remember { mutableStateOf<MealType?>(null) }
    var scannedFood by remember { mutableStateOf<com.example.data.model.FoodItem?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("diary_screen_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Lead Magnet Banner (Tiers & Perks)
        item(key = "lead_magnet_banner") {
            LeadMagnetBannerCard(
                currentTier = currentTier,
                onOpenPaywall = { viewModel.openPaywall() }
            )
        }

        // Date Switcher Header
        item {
            DateHeaderRow(
                currentDateStr = selectedDate,
                onPreviousDay = { viewModel.changeDateBy(-1) },
                onNextDay = { viewModel.changeDateBy(1) },
                onResetToday = { viewModel.selectDate(CalorieCalculator.getTodayString()) }
            )
        }

        // Quick Actions Row (Repeat yesterday, barcode, quick calories)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy yesterday
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { viewModel.copyYesterdayMeals() }
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .testTag("copy_yesterday_meals_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(text = appString(StringKey.DIARY_COPY_YESTERDAY), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Barcode scanner (Gated by BARCODE_SCANNER)
                val isBarcodeUnlocked = viewModel.isFeatureUnlocked(AppFeature.BARCODE_SCANNER)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            if (isBarcodeUnlocked) {
                                showBarcodeScannerMealType = MealType.LUNCH
                            } else {
                                viewModel.openPaywall(AppFeature.BARCODE_SCANNER)
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .testTag("open_barcode_scanner_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(text = appString(StringKey.DIARY_BARCODE_SCANNER), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (!isBarcodeUnlocked) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp), tint = FatAmber)
                        }
                    }
                }

                // Quick calories (Gated by QUICK_CALORIES)
                val isQuickUnlocked = viewModel.isFeatureUnlocked(AppFeature.QUICK_CALORIES)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            if (isQuickUnlocked) {
                                showQuickCaloriesMealType = MealType.SNACK
                            } else {
                                viewModel.openPaywall(AppFeature.QUICK_CALORIES)
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .testTag("open_quick_calories_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = FatAmber)
                        Text(text = "+ ${appString(StringKey.DIARY_QUICK_CALORIES)}", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (!isQuickUnlocked) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp), tint = FatAmber)
                        }
                    }
                }
            }
        }

        // Nutrient Summary Card (Calories Gauge + Macros)
        item {
            NutrientProgressGauge(summary = dailySummary)
        }

        // Habit & Streak Card (Gated by HABIT_STREAKS)
        item {
            val isHabitUnlocked = viewModel.isFeatureUnlocked(AppFeature.HABIT_STREAKS)
            if (isHabitUnlocked) {
                HabitStreakCard(
                    currentStreak = currentStreak,
                    habit = currentHabit,
                    onToggleHabit = { viewModel.saveHabit(it) }
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openPaywall(AppFeature.HABIT_STREAKS) }
                        .testTag("locked_habit_card"),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FatAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = appString(StringKey.DIARY_HABITS_TITLE), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(text = appString(StringKey.DIARY_HABITS_DESC), style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(text = appString(StringKey.DIARY_OPEN_HABITS), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Meal Sections
        MealType.entries.forEach { mealType ->
            val mealsForType = meals.filter { it.mealType == mealType }
            val mealTotalKcal = mealsForType.sumOf { it.calories.toDouble() }.roundToInt()

            item(key = "meal_${mealType.name}") {
                MealSectionCard(
                    mealType = mealType,
                    totalCalories = mealTotalKcal,
                    mealEntries = mealsForType,
                    onAddFoodClick = { scannedFood = null; showAddFoodMealType = mealType },
                    onDeleteMealEntry = { viewModel.deleteMeal(it) }
                )
            }
        }

        // Weekly Deficit Bar Chart Card
        item {
            WeeklyDeficitCard(
                weeklyDays = weeklyDays,
                targetKcal = dailySummary.calorieTarget
            )
        }

        // Water Tracker Card
        item {
            WaterTrackerCard(
                currentMl = dailySummary.waterIntakeMl,
                goalMl = dailySummary.waterGoalMl,
                onAddWater = { viewModel.addWater(it) },
                onRemoveWater = { viewModel.removeLastWater() }
            )
        }

        // Exercise & Calories Burned Card
        item {
            ExerciseSectionCard(
                totalBurned = dailySummary.totalCaloriesBurned,
                exercises = exercises,
                onAddExerciseClick = { showAddExerciseDialog = true },
                onDeleteExercise = { viewModel.deleteExercise(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Food Bottom Sheet
    showAddFoodMealType?.let { mealType ->
        AddFoodDialog(
            mealType = mealType,
            foods = foodCatalog,
            initialFood = scannedFood,
            onDismiss = {
                showAddFoodMealType = null
                scannedFood = null
            },
            onAddMeal = { foodName, grams, calories, protein, fat, carbs ->
                viewModel.addMeal(mealType, foodName, grams, calories, protein, fat, carbs)
            },
            onCreateCustomFood = { name, category, calories, protein, fat, carbs ->
                viewModel.saveCustomFood(name, category, calories, protein, fat, carbs)
            }
        )
    }

    // Quick Calories Dialog
    showQuickCaloriesMealType?.let { mealType ->
        QuickCaloriesDialog(
            mealType = mealType,
            onDismiss = { showQuickCaloriesMealType = null },
            onSaveQuickMeal = { name, kcal, p, f, c ->
                viewModel.addQuickMeal(mealType, name, kcal, p, f, c)
            }
        )
    }

    // Production camera barcode scanner
    showBarcodeScannerMealType?.let { mealType ->
        BarcodeScannerDialog(
            lookupProduct = { barcode -> viewModel.findFoodByBarcode(barcode) },
            saveProduct = { food -> viewModel.saveBarcodeFood(food) },
            onDismiss = { showBarcodeScannerMealType = null },
            onProductFound = { foundProduct ->
                scannedFood = foundProduct
                showBarcodeScannerMealType = null
                showAddFoodMealType = mealType
            }
        )
    }

    // Add Exercise Dialog
    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onSaveExercise = { name, duration, calories ->
                viewModel.addExercise(name, duration, calories)
            }
        )
    }
}

@Composable
private fun DateHeaderRow(
    currentDateStr: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToday: () -> Unit
) {
    val currentLang = com.example.util.appLanguage()
    val displayDate = CalorieCalculator.formatDateDisplay(currentDateStr, currentLang)
    val isToday = currentDateStr == CalorieCalculator.getTodayString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousDay,
            modifier = Modifier.testTag("prev_day_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "<",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                .clickable(onClick = onResetToday)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = null,
                tint = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = displayDate,
                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onNextDay,
            modifier = Modifier.testTag("next_day_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = ">",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MealSectionCard(
    mealType: MealType,
    totalCalories: Int,
    mealEntries: List<MealEntry>,
    onAddFoodClick: () -> Unit,
    onDeleteMealEntry: (Long) -> Unit
) {
    val mealTitle = when (mealType) {
        MealType.BREAKFAST -> appString(StringKey.DIARY_MEAL_BREAKFAST)
        MealType.LUNCH -> appString(StringKey.DIARY_MEAL_LUNCH)
        MealType.DINNER -> appString(StringKey.DIARY_MEAL_DINNER)
        MealType.SNACK -> appString(StringKey.DIARY_MEAL_SNACKS)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meal_card_${mealType.name}"),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Meal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = mealType.icon, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    Text(
                        text = mealTitle,
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (totalCalories > 0) {
                        Text(
                            text = "$totalCalories ${appString(StringKey.UNIT_KCAL)}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .clickable(onClick = onAddFoodClick)
                            .padding(6.dp)
                            .testTag("add_food_btn_${mealType.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = appString(StringKey.DIARY_ADD_FOOD),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Entries List or Empty State
            if (mealEntries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    mealEntries.forEach { entry ->
                        MealEntryItem(
                            entry = entry,
                            onDelete = { onDeleteMealEntry(entry.id) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appString(StringKey.DIARY_NO_ITEMS_YET),
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MealEntryItem(
    entry: MealEntry,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${localizedFoodName(entry.foodName)} (${entry.grams.roundToInt()} ${appString(StringKey.UNIT_GRAM)})",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(end = 6.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${entry.calories.roundToInt()} ${appString(StringKey.UNIT_KCAL)}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = appString(StringKey.DELETE),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Spaced Macro Badges (БЖУ)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EntryMacroBadge(
                label = appString(StringKey.DIARY_PROTEIN),
                value = "${entry.protein.roundToInt()}${appString(StringKey.UNIT_GRAM)}",
                color = ProteinBlue,
                modifier = Modifier.weight(1f)
            )
            EntryMacroBadge(
                label = appString(StringKey.DIARY_FAT),
                value = "${entry.fat.roundToInt()}${appString(StringKey.UNIT_GRAM)}",
                color = FatAmber,
                modifier = Modifier.weight(1f)
            )
            EntryMacroBadge(
                label = appString(StringKey.DIARY_CARBS),
                value = "${entry.carbs.roundToInt()}${appString(StringKey.UNIT_GRAM)}",
                color = CarbRose,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EntryMacroBadge(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(androidx.compose.material3.MaterialTheme.shapes.extraSmall)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${label.take(3)}: ",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Text(
            text = value,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExerciseSectionCard(
    totalBurned: Int,
    exercises: List<com.example.data.model.ExerciseEntry>,
    onAddExerciseClick: () -> Unit,
    onDeleteExercise: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_card"),
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BurnedPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = BurnedPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = appString(StringKey.DIARY_EXERCISE_TITLE),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appString(StringKey.DIARY_EXERCISE_HINT),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (totalBurned > 0) {
                        Text(
                            text = "-$totalBurned ${appString(StringKey.UNIT_KCAL)}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BurnedPurple
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BurnedPurple.copy(alpha = 0.15f))
                            .clickable(onClick = onAddExerciseClick)
                            .padding(6.dp)
                            .testTag("add_exercise_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = appString(StringKey.DIARY_ADD_EXERCISE),
                            tint = BurnedPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    exercises.forEach { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = ex.activityName,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${ex.durationMinutes} min",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "-${ex.caloriesBurned} ${appString(StringKey.UNIT_KCAL)}",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BurnedPurple
                                )
                                IconButton(
                                    onClick = { onDeleteExercise(ex.id) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = appString(StringKey.DELETE),
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
