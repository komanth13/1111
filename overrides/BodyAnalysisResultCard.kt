package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.GoalPace
import com.example.ui.theme.CarbRose
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.util.CalorieCalculator
import com.example.util.ProfileValidator
import com.example.util.StringKey
import com.example.util.appString
import kotlin.math.roundToInt

@Composable
fun BodyAnalysisResultCard(
    age: Int,
    heightCm: Float,
    currentWeightKg: Float,
    targetWeightKg: Float,
    gender: Gender,
    activityLevel: ActivityLevel,
    goalPace: GoalPace,
    modifier: Modifier = Modifier,
    averageDailySteps: Int? = null,
    householdMinutes: Int? = null
) {
    if (!ProfileValidator.isSupportedAdultBodyData(age, heightCm, currentWeightKg) ||
        !ProfileValidator.isSupportedWeight(targetWeightKg) || targetWeightKg > currentWeightKg ||
        ProfileValidator.isTargetBelowSupportedAdultRange(heightCm, targetWeightKg)
    ) return

    var showExplainerDialog by remember { mutableStateOf(false) }

    val targets = CalorieCalculator.calculateRecommendedTargets(gender, age, heightCm,
        currentWeightKg, activityLevel, goalPace, targetWeightKg, averageDailySteps, householdMinutes)
    val bmr = targets.bmr
    val tdee = targets.tdee
    val bmiResult = CalorieCalculator.calculateBmi(currentWeightKg, heightCm)
    val weightDiff = (currentWeightKg - targetWeightKg)
    val heightM = (heightCm / 100f).coerceAtLeast(0.5f)
    val minHealthyKg = ((heightM * heightM) * 18.5f).roundToInt()
    val maxHealthyKg = ((heightM * heightM) * 24.9f).roundToInt()
    val estimatedWeeks = CalorieCalculator.estimateTargetWeeks(currentWeightKg, targetWeightKg, targets.estimatedWeeklyKg)

    val bmiCategoryText = when {
        bmiResult.bmi < 18.5f -> appString(StringKey.BMI_UNDERWEIGHT)
        bmiResult.bmi in 18.5f..24.9f -> appString(StringKey.BMI_NORMAL)
        bmiResult.bmi in 25.0f..29.9f -> appString(StringKey.BMI_OVERWEIGHT)
        else -> appString(StringKey.BMI_OBESITY)
    }

    val bmiColor = when {
        bmiResult.bmi < 18.5f -> ProteinBlue
        bmiResult.bmi in 18.5f..24.9f -> EmeraldPrimary
        bmiResult.bmi in 25.0f..29.9f -> FatAmber
        else -> CarbRose
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("body_analysis_result_card"),
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with Question Mark Badge (❔)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = appString(StringKey.ANALYSIS_RESULT_TITLE),
                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appString(StringKey.ANALYSIS_RESULT_SUBTITLE),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive Question Mark Badge (❔)
                Surface(
                    shape = androidx.compose.material3.MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                        .clickable { showExplainerDialog = true }
                        .testTag("body_analysis_question_mark_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = appString(StringKey.EXPLAINER_DIALOG_TITLE),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "❔",
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BMI Main Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.material3.MaterialTheme.shapes.large)
                    .border(1.dp, bmiColor.copy(alpha = 0.4f), androidx.compose.material3.MaterialTheme.shapes.large),
                color = bmiColor.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appString(StringKey.ANALYSIS_BMI_LABEL),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${bmiResult.bmi} • $bmiCategoryText",
                            style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = bmiColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                            .background(bmiColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "BMI",
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of Key Parameters: Ideal weight, Difference to goal, TDEE, Estimated weeks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Healthy Range
                Card(
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = appString(StringKey.ANALYSIS_IDEAL_WEIGHT_RANGE),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$minHealthyKg - $maxHealthyKg ${appString(StringKey.WEIGHT_UNIT_KG)}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Diff to goal
                Card(
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = appString(StringKey.ANALYSIS_DIFF_TO_GOAL),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (weightDiff > 0) "-%.1f %s".format(weightDiff, appString(StringKey.WEIGHT_UNIT_KG)) else "+%.1f %s".format(-weightDiff, appString(StringKey.WEIGHT_UNIT_KG)),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (weightDiff > 0) EmeraldPrimary else ProteinBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TDEE Burn
                Card(
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = appString(StringKey.ANALYSIS_TDEE_LABEL),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$tdee ${appString(StringKey.UNIT_KCAL)}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Estimated weeks
                Card(
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = appString(StringKey.ANALYSIS_WEEKS_ESTIMATE),
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (estimatedWeeks > 0) "~$estimatedWeeks ${appString(StringKey.WEIGHT_WEEKS_LABEL)}" else if (weightDiff <= 0f) "✓" else "—",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Explainer Pop-up Dialog
    if (showExplainerDialog) {
        BodyAnalysisExplainerDialog(
            age = age,
            heightCm = heightCm,
            currentWeightKg = currentWeightKg,
            targetWeightKg = targetWeightKg,
            gender = gender,
            activityLevel = activityLevel,
            goalPace = goalPace,
            onDismiss = { showExplainerDialog = false },
            averageDailySteps = averageDailySteps,
            householdMinutes = householdMinutes
        )
    }
}
