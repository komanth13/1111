package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ActivityLevel
import com.example.data.model.Gender
import com.example.data.model.GoalPace
import com.example.ui.theme.CarbRose
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatAmber
import com.example.ui.theme.ProteinBlue
import com.example.util.AppLanguage
import com.example.util.CalorieCalculator
import com.example.util.ProfileValidator
import com.example.util.LocalizationManager
import com.example.util.StringKey
import com.example.util.appString
import kotlin.math.roundToInt

@Composable
fun BodyAnalysisExplainerDialog(
    age: Int,
    heightCm: Float,
    currentWeightKg: Float,
    targetWeightKg: Float,
    gender: Gender,
    activityLevel: ActivityLevel,
    goalPace: GoalPace,
    onDismiss: () -> Unit,
    averageDailySteps: Int? = null,
    householdMinutes: Int? = null
) {
    if (!ProfileValidator.isSupportedAdultBodyData(age, heightCm, currentWeightKg) ||
        !ProfileValidator.isSupportedWeight(targetWeightKg) || targetWeightKg > currentWeightKg ||
        ProfileValidator.isTargetBelowSupportedAdultRange(heightCm, targetWeightKg)
    ) return

    val currentLang = com.example.util.appLanguage()

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 760.dp)
                .clip(RoundedCornerShape(26.dp))
                .testTag("body_analysis_explainer_container"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Title and explicit Close Cross (X)
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = appString(StringKey.EXPLAINER_DIALOG_TITLE),
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${appString(StringKey.PROFILE_AGE)}: $age • ${appString(StringKey.PROFILE_HEIGHT)}: ${heightCm.toInt()} ${appString(StringKey.UNIT_CM)} • ${appString(StringKey.WEIGHT_CURRENT)}: $currentWeightKg ${appString(StringKey.WEIGHT_UNIT_KG)}",
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Explicit Close Cross button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_analysis_explainer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = appString(StringKey.CLOSE),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Block 1: What It Means (Current Stats Breakdown)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.material3.MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appString(StringKey.EXPLAINER_SECTION_WHAT_IT_MEANS),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.UK -> "• Ваш ІМТ складає ${bmiResult.bmi} (${when {
                                            bmiResult.bmi < 18.5f -> "Дефіцит маси"
                                            bmiResult.bmi in 18.5f..24.9f -> "Здорова норма ✓"
                                            bmiResult.bmi in 25f..29.9f -> "Надлишкова вага"
                                            else -> "Ожиріння"
                                        }}).\n" +
                                        "• Здоровий медичний діапазон для вашого зросту: від $minHealthyKg до $maxHealthyKg кг.\n" +
                                        "• Базовий обмін у спокої (BMR): $bmr ккал/день.\n" +
                                        "• Добова витрата з урахуванням вашої активності (TDEE): $tdee ккал/день.\n" +
                                        "• До мети (${targetWeightKg} кг): ${if (weightDiff > 0) "потрібно комфортно скинути %.1f кг".format(weightDiff) else "потрібно набрати %.1f кг".format(-weightDiff)} (~$estimatedWeeks тижнів)."
                                        AppLanguage.EN -> "• Your BMI is ${bmiResult.bmi} (${when {
                                            bmiResult.bmi < 18.5f -> "Underweight"
                                            bmiResult.bmi in 18.5f..24.9f -> "Healthy Normal ✓"
                                            bmiResult.bmi in 25f..29.9f -> "Overweight"
                                            else -> "Obesity"
                                        }}).\n" +
                                        "• Recommended healthy weight for your height: $minHealthyKg – $maxHealthyKg kg.\n" +
                                        "• Basal Metabolic Rate at rest (BMR): $bmr kcal/day.\n" +
                                        "• Total Daily Energy Expenditure (TDEE): $tdee kcal/day.\n" +
                                        "• Difference to goal (${targetWeightKg} kg): ${if (weightDiff > 0) "need to shed %.1f kg".format(weightDiff) else "need to gain %.1f kg".format(-weightDiff)} (~$estimatedWeeks weeks)."
                                        else -> "• Ваш ИМТ составляет ${bmiResult.bmi} (${when {
                                            bmiResult.bmi < 18.5f -> "Дефицит массы"
                                            bmiResult.bmi in 18.5f..24.9f -> "Здоровая норма ✓"
                                            bmiResult.bmi in 25f..29.9f -> "Избыточный вес"
                                            else -> "Ожирение"
                                        }}).\n" +
                                        "• Здоровый медицинский диапазон для вашего роста: от $minHealthyKg до $maxHealthyKg кг.\n" +
                                        "• Базовый обмен в покое (BMR): $bmr ккал/день.\n" +
                                        "• Суточный расход с учетом подвижности (TDEE): $tdee ккал/день.\n" +
                                        "• Разница до цели (${targetWeightKg} кг): ${if (weightDiff > 0) "нужно комфортно сбросить %.1f кг".format(weightDiff) else "нужно набрать %.1f кг".format(-weightDiff)} (~$estimatedWeeks недель)."
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Block 2: Problems & Impact
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.material3.MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = CarbRose.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbRose.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = CarbRose, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appString(StringKey.EXPLAINER_SECTION_PROBLEMS),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CarbRose
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.UK -> "• Навантаження на суглоби та хребет: кожен зайвий 1 кг маси створює тиск до 4 кг на колінні суглоби при звичайній ходьбі.\n" +
                                        "• Серце та судини: серцю доводиться прокачувати кров через кілометри додаткових капілярів, що підвищує артеріальний тиск.\n" +
                                        "• Енергія та сон: застій лімфи, інсулінові гойдалки та відчуття тяжкості викликають постійну денну втому та розбитість зранку.\n" +
                                        "• Метаболізм: малорухливий спосіб життя сповільнює утилізацію глюкози, відкладаючи вуглеводи прямо у вісцеральний жир."
                                        AppLanguage.EN -> "• Joint & Spine Strain: Every 1 extra kg adds up to 4 kg of pressure on your knee joints with every single step.\n" +
                                        "• Cardiovascular Stress: Heart works overtime pumping blood through extra tissue, raising blood pressure.\n" +
                                        "• Low Energy & Sleep: Blood sugar spikes and fluid retention cause constant afternoon crashes and groggy mornings.\n" +
                                        "• Slower Metabolism: Low physical movement reduces insulin sensitivity and stores excess fuel directly as visceral fat."
                                        else -> "• Нагрузка на суставы и позвоночник: каждый лишний 1 кг веса создает давление до 4 кг на коленные суставы при обычной ходьбе.\n" +
                                        "• Сердце и сосуды: сердцу приходится прокачивать кровь через километры дополнительной ткани, повышая артериальное давление.\n" +
                                        "• Энергия и сон: скачки инсулина, задержка жидкости и тяжесть вызывают постоянную дневную сонливость и разбитость по утрам.\n" +
                                        "• Метаболизм: малоподвижный образ жизни снижает чувствительность к инсулину, запасая углеводы сразу в висцеральный жир."
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Block 3: How to Improve (Action Plan)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.material3.MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appString(StringKey.EXPLAINER_SECTION_HOW_TO_IMPROVE),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.UK -> "1. М'який дефіцит без голодування: дефіцит 15-20% (300-500 ккал) забезпечує стабільне спалювання саме жиру, а не м'язів.\n" +
                                        "2. Норма білка (1.6-1.8 г на кг): дає довге відчуття ситості без зривів та захищає м'язовий каркас.\n" +
                                        "3. Водний режим (30 мл на кг): вимиває токсини та знімає набряки, які часто плутають із жиром.\n" +
                                        "4. Щоденні кроки: звичайна ходьба 7000-10000 кроків спалює більше калорій, ніж рідкісні виснажливі тренування."
                                        AppLanguage.EN -> "1. Gentle Deficit: 15-20% deficit (300-500 kcal) burns fat steadily without hunger pangs or metabolic slowdown.\n" +
                                        "2. Adequate Protein (1.6-1.8g/kg): Keeps you full for hours and prevents muscle loss.\n" +
                                        "3. Hydration (30ml/kg): Flushes fluid retention and prevents false hunger signals.\n" +
                                        "4. Daily NEAT (Steps): 7,000–10,000 daily steps burns more weekly fat than sporadic high-intensity workouts."
                                        else -> "1. Мягкий дефицит без голода: дефицит 15-20% (300-500 ккал) сжигает именно жир, а не мышцы, без срывов и слабости.\n" +
                                        "2. Норма белка (1.6-1.8 г на кг): дает долгое насыщение и защищает мышцы от разрушения.\n" +
                                        "3. Водный баланс (30 мл на кг): выводит отеки, которые часто маскируются под жир, и разгоняет лимфу.\n" +
                                        "4. Шаги и движение: обычная прогулка на 7000-10000 шагов в день сжигает больше калорий, чем редкие тренировки в зале."
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Block 4: How SlimTrack Helps
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.material3.MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = ProteinBlue.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ProteinBlue.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = ProteinBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appString(StringKey.EXPLAINER_SECTION_HOW_APP_HELPS),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ProteinBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.UK -> "• Трендова вага: SlimTrack згладжує щоденні стрибки води, показуючи справжній жировий прогрес без паніки.\n" +
                                        "• Авторозрахунок БЖВ: вам не потрібно рахувати вручну — щоденник контролює баланс нутрієнтів у реальному часі.\n" +
                                        "• Трекер звичок та води: нагадує випити склянку води та формує стабільний здоровий ритм життя."
                                        AppLanguage.EN -> "• Trend Weight: Filters out daily water fluctuations so you see real fat loss without stress.\n" +
                                        "• Auto Macro Calculations: Instant real-time tracking of calories, protein, carbs and fats.\n" +
                                        "• Habit & Water Tracker: Builds sustainable daily micro-habits that stick for life."
                                        else -> "• Трендовый вес: SlimTrack сглаживает ежедневные колебания воды, показывая реальное сжигание жира без стресса.\n" +
                                        "• Авторасчет БЖУ: система автоматически держит вас в безопасном коридоре без необходимости считать вручную.\n" +
                                        "• Трекер воды и привычек: формирует легкие ежедневные ритуалы, которые остаются с вами навсегда."
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Block 5: Motivation
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.material3.MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = FatAmber.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FatAmber.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = FatAmber, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = appString(StringKey.EXPLAINER_SECTION_MOTIVATION),
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FatAmber
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.UK -> "🌟 Пам'ятайте: ваше тіло здатне на неймовірну трансформацію у будь-якому віці! Не прагніть бути ідеальними кожен день — головне не кидати і робити невеликі кроки щодня. Вже через 14 днів регулярного ведення щоденника ви побачите перший мінус на вагах і відчуєте неймовірну легкість. Ми поруч на кожному кроці вашого шляху!"
                                        AppLanguage.EN -> "🌟 Remember: your body is capable of amazing transformations at any age! You don't have to be perfect every day — consistency is what wins. In just 14 days of mindful tracking, you will feel lighter, more energetic and see tangible results. You've got this!"
                                        else -> "🌟 Помните: ваше тело способно на невероятную трансформацию в любом возрасте! Не стремитесь быть идеальными каждый день — главное не бросать и делать простые шаги. Уже через 14 дней регулярного ведения дневника вы заметите первый устойчивый минус на весах и почувствуете приятную легкость. Вы обязательно добьетесь своей цели!"
                                    },
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(com.example.ui.theme.SlimTrackSizes.buttonLarge)
                        .testTag("dismiss_analysis_explainer_button"),
                    shape = androidx.compose.material3.MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = appString(StringKey.EXPLAINER_GOT_IT_BUTTON),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
