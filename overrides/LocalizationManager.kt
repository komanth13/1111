package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import com.example.data.model.AppFeature
import com.example.data.model.PaymentMethodType
import com.example.data.model.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
enum class AppLanguage(
    val code: String,
    val localeTag: String,
    val displayName: String,
    val flag: String
) {
    RU("ru", "ru-RU", "Русский", "🇷🇺"),
    UK("uk", "uk-UA", "Українська", "🇺🇦"),
    EN("en", "en-US", "English", "🇬🇧");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            val normalized = code.orEmpty().trim().lowercase().replace('_', '-')
            return entries.firstOrNull { language ->
                normalized == language.code ||
                    normalized == language.localeTag.lowercase() ||
                    normalized.startsWith("${language.code}-")
            } ?: RU
        }
    }
}

enum class StringKey {
    // App & Tabs
    APP_NAME,
    TAB_DIARY,
    TAB_WEIGHT,
    TAB_FOODS,
    TAB_INSIGHTS,
    TAB_PROFILE,
    HEADER_PLANS_BTN,
    GUEST_ACCESS_FOREVER,
    GUEST_ACCESS_DAYS_LEFT,
    GUEST_ACCESS_ACTIVE_CODE,

    // Common
    SAVE,
    CANCEL,
    CLOSE,
    DELETE,
    EDIT,
    SEARCH,
    ADD,
    COPY,
    SHARE,
    LOADING,
    ERROR,
    SUCCESS,
    UNLOCKED,
    LOCKED,
    PRO_REQUIRED,
    PREMIUM_REQUIRED,
    ACTIVE_BADGE,

    // Language
    LANGUAGE_TITLE,
    SELECT_LANGUAGE,

    // Subscription & Tiers
    SUBSCRIPTION_TITLE,
    CURRENT_PLAN,
    PLAN_FREE,
    PLAN_SLIM,
    PLAN_PREMIUM,
    PLAN_ADMIN,
    FREE_PRICE,
    SLIM_PRICE,
    PREMIUM_PRICE,
    UPGRADE_PLAN,
    UNLOCK_ALL,
    PAYWALL_TITLE,
    PAYWALL_SUBTITLE,
    PAYWALL_FEATURE_LOCKED_FMT,
    PAYWALL_ALREADY_ACTIVE,
    PAYWALL_BUY_BTN_FMT,
    PAYWALL_SELECT_FREE,
    PAYWALL_HAVE_PROMO,
    PAYWALL_SECURITY_NOTE,
    CHOOSE_PLAN,
    POPULAR_CHOICE,
    BEST_VALUE,

    // Admin Panel
    ADMIN_PANEL_TITLE,
    ADMIN_PANEL_SUBTITLE,
    ADMIN_FILE_IMPORT_TITLE,
    ADMIN_FILE_IMPORT_DESC,
    ADMIN_IMPORT_BUTTON,
    ADMIN_EXPORT_BUTTON,
    ADMIN_RESET_BUTTON,
    ADMIN_TIER_SIMULATOR,
    ADMIN_CONFIG_STATUS,
    ADMIN_CONFIG_SOURCE_DEFAULT,
    ADMIN_CONFIG_SOURCE_FILE,
    ADMIN_CONFIG_UPDATED,
    ADMIN_VIEW_JSON,
    ADMIN_SUCCESS_IMPORT,
    ADMIN_ERROR_IMPORT,
    ADMIN_EXTRA_FOODS_IMPORTED,

    // Diary Screen
    DIARY_CALORIES_LEFT,
    DIARY_CONSUMED,
    DIARY_BURNED,
    DIARY_TARGET,
    DIARY_PROTEIN,
    DIARY_FAT,
    DIARY_CARBS,
    DIARY_MEAL_BREAKFAST,
    DIARY_MEAL_LUNCH,
    DIARY_MEAL_DINNER,
    DIARY_MEAL_SNACKS,
    DIARY_ADD_FOOD,
    DIARY_QUICK_CALORIES,
    DIARY_COPY_YESTERDAY,
    DIARY_FASTING_TIMER,
    DIARY_FASTING_START,
    DIARY_FASTING_STOP,
    DIARY_WATER_TRACKER,
    DIARY_ADD_EXERCISE,
    DIARY_BARCODE_SCANNER,

    // Weight Screen
    WEIGHT_TITLE,
    WEIGHT_CURRENT,
    WEIGHT_START,
    WEIGHT_TARGET,
    WEIGHT_TREND,
    WEIGHT_LOG_BUTTON,
    WEIGHT_CHART_TITLE,
    WEIGHT_MEASUREMENTS_TITLE,
    WEIGHT_WEEKLY_DEFICIT_TITLE,

    // Food Database
    FOOD_SEARCH_PLACEHOLDER,
    FOOD_ALL_CATEGORIES,
    FOOD_CREATE_CUSTOM,

    // Insights
    INSIGHTS_TITLE,
    INSIGHTS_STREAKS,
    INSIGHTS_WEEKLY_CHART,
    INSIGHTS_EXPORT_REPORT,

    // Profile Fields & Form
    PROFILE_TITLE,
    PROFILE_SUBTITLE,
    PROFILE_PERSONAL_DATA,
    PROFILE_GENDER,
    GENDER_MALE,
    GENDER_FEMALE,
    PROFILE_AGE,
    PROFILE_HEIGHT,
    PROFILE_START_WEIGHT,
    PROFILE_CURRENT_WEIGHT,
    PROFILE_TARGET_WEIGHT,
    PROFILE_ACTIVITY_LEVEL,
    PROFILE_GOAL_PACE,
    PROFILE_MACROS_TITLE,
    PROFILE_CALORIES_GOAL,
    PROFILE_PROTEIN_GOAL,
    PROFILE_FAT_GOAL,
    PROFILE_CARBS_GOAL,
    PROFILE_WATER_GOAL,
    PROFILE_AUTO_CALC,
    PROFILE_SAVE_SETTINGS,
    PROFILE_SAVED_SUCCESS,
    PROFILE_VALIDATION_FIX_ERRORS,
    PROFILE_ERROR_NAME_TOO_LONG,
    PROFILE_ERROR_ADULT_ONLY,
    PROFILE_ERROR_AGE_RANGE,
    PROFILE_ERROR_HEIGHT_RANGE,
    PROFILE_ERROR_WEIGHT_RANGE,
    PROFILE_ERROR_TARGET_ABOVE_CURRENT,
    PROFILE_ERROR_TARGET_TOO_LOW,
    PROFILE_ERROR_CALORIES_RANGE,
    PROFILE_ERROR_MACRO_RANGE,
    PROFILE_ERROR_WATER_RANGE,
    PROFILE_SAFE_CALC_NOTE,

    // Activity Levels
    STEPS_LABEL,
    HOUSEHOLD_LABEL,
    LIFESTYLE_HELP,
    LIFESTYLE_AUTO,
    PROFILE_ERROR_STEPS,
    PROFILE_ERROR_HOUSEHOLD,
    PLAN_MAINTENANCE,
    PLAN_DEFICIT,
    PLAN_TARGET,
    PLAN_ESTIMATE,
    PACE_LIMIT,
    ACTIVITY_SEDENTARY,
    ACTIVITY_SEDENTARY_DESC,
    ACTIVITY_LIGHT,
    ACTIVITY_LIGHT_DESC,
    ACTIVITY_MODERATE,
    ACTIVITY_MODERATE_DESC,
    ACTIVITY_ACTIVE,
    ACTIVITY_ACTIVE_DESC,

    // Goal Paces
    PACE_EASY,
    PACE_RECOMMENDED,
    PACE_FAST,
    PACE_MAX,

    // Body Analysis Result Card
    ANALYSIS_RESULT_TITLE,
    ANALYSIS_RESULT_SUBTITLE,
    ANALYSIS_BMI_LABEL,
    BMI_UNDERWEIGHT,
    BMI_NORMAL,
    BMI_OVERWEIGHT,
    BMI_OBESITY,
    ANALYSIS_IDEAL_WEIGHT_RANGE,
    ANALYSIS_DIFF_TO_GOAL,
    ANALYSIS_TO_LOSE,
    ANALYSIS_TO_GAIN,
    ANALYSIS_TDEE_LABEL,
    ANALYSIS_BMR_LABEL,
    ANALYSIS_WEEKS_ESTIMATE,
    ANALYSIS_HELP_BUTTON_TEXT,

    // Explainer Dialog (Pop-up with X)
    EXPLAINER_DIALOG_TITLE,
    EXPLAINER_SECTION_WHAT_IT_MEANS,
    EXPLAINER_SECTION_PROBLEMS,
    EXPLAINER_SECTION_HOW_TO_IMPROVE,
    EXPLAINER_SECTION_HOW_APP_HELPS,
    EXPLAINER_SECTION_MOTIVATION,
    EXPLAINER_GOT_IT_BUTTON,

    // Lead Magnet Banner & Tiers
    LEAD_MAGNET_TITLE,
    LEAD_MAGNET_SUBTITLE,
    LEAD_MAGNET_PERK_1,
    LEAD_MAGNET_PERK_2,
    LEAD_MAGNET_PERK_3,
    LEAD_MAGNET_BUTTON,

    // Diary extras
    DIARY_WATER_HINT,
    DIARY_EXERCISE_TITLE,
    DIARY_EXERCISE_HINT,
    DIARY_NO_ITEMS_YET,
    DIARY_HABITS_TITLE,
    DIARY_HABITS_DESC,
    DIARY_OPEN_HABITS,
    DIARY_CALORIES_OVER,
    DIARY_CURRENT_DEFICIT,
    DIARY_CURRENT_SURPLUS,
    DIARY_MACROS_TITLE,

    // Admin & In-App Update
    ADMIN_TAB_UPDATE,
    ADMIN_INSTALL_APK_BUTTON,
    ADMIN_INSTALL_APK_DESC,
    ADMIN_APK_NO_DELETE_NOTE,

    // Units
    UNIT_KCAL,
    UNIT_GRAM,
    UNIT_ML,
    UNIT_CM,
    UNIT_MIN,

    // Insights extra
    INSIGHTS_SUBTITLE,
    INSIGHTS_FASTING_TITLE,
    INSIGHTS_FASTING_ACTIVE,
    INSIGHTS_FASTING_STOPPED,
    INSIGHTS_FASTING_BTN_START,
    INSIGHTS_FASTING_BTN_STOP,
    INSIGHTS_RULES_TITLE,

    // Weight extra
    WEIGHT_HEADER_SUBTITLE,
    WEIGHT_ADD_BUTTON,
    WEIGHT_LOST_LABEL,
    WEIGHT_REMAINING_LABEL,
    WEIGHT_WEEKS_LABEL,
    WEIGHT_PROGRESS_TO_GOAL,
    WEIGHT_FORECAST_TEXT,
    WEIGHT_HISTORY_TITLE,
    WEIGHT_EMPTY_HISTORY,
    WEIGHT_UNIT_KG,

    // Chart & Trend & Weekly Deficit & Measurements & Habits & Export
    CHART_SUBTITLE,
    CHART_FILTER_7D,
    CHART_FILTER_30D,
    CHART_FILTER_ALL,
    CHART_EMPTY,
    CHART_ACTUAL_WEIGHT,
    TREND_CARD_TITLE,
    TREND_CARD_SUBTITLE,
    TREND_LABEL_TREND,
    TREND_LABEL_SCALE,
    TREND_INFO_GOOD,
    TREND_INFO_NORMAL,
    WEEKLY_DEFICIT_SUBTITLE,
    WEEKLY_FAT_BURN_FORECAST,
    WEEKLY_FAT_BURN_NOTE,
    MEAS_TITLE,
    MEAS_SUBTITLE,
    MEAS_WAIST,
    MEAS_HIPS,
    MEAS_CHEST,
    MEAS_BICEP,
    MEAS_THIGH,
    MEAS_ADD_BTN,
    MEAS_EMPTY_HINT,
    HABIT_STREAK_BADGE,
    HABIT_1,
    HABIT_2,
    HABIT_3,
    HABIT_4,
    HABIT_5,
    EXPORT_SUBTITLE,
    EXPORT_SHARE_BTN,
    EXPORT_COPY_BTN,
    EXPORT_COPIED_TOAST,

    // Dialogs (Add Food, Exercise, Weight, Quick Calories, Barcode, Promo, Payment, Receipt)
    DIALOG_ADD_FOOD_TITLE,
    DIALOG_GRAMS_LABEL,
    DIALOG_EMPTY_FOODS,
    DIALOG_ADD_EXERCISE_TITLE,
    DIALOG_EXERCISE_NAME_LABEL,
    DIALOG_EXERCISE_MINUTES_LABEL,
    DIALOG_EXERCISE_KCAL_LABEL,
    DIALOG_ADD_WEIGHT_TITLE,
    DIALOG_WEIGHT_INPUT_LABEL,
    DIALOG_NOTE_LABEL,
    DIALOG_QUICK_CALORIES_TITLE,
    DIALOG_QUICK_CALORIES_SUBTITLE,
    DIALOG_BARCODE_TITLE,
    DIALOG_BARCODE_SUBTITLE,
    DIALOG_BARCODE_INPUT_LABEL,
    DIALOG_BARCODE_CAMERA_HINT,
    DIALOG_BARCODE_CAMERA_PERMISSION,
    DIALOG_BARCODE_GRANT_PERMISSION,
    DIALOG_BARCODE_NOT_FOUND,
    DIALOG_BARCODE_NETWORK_ERROR,
    DIALOG_BARCODE_SERVICE_ERROR,
    DIALOG_BARCODE_INCOMPLETE,
    DIALOG_BARCODE_INVALID_CODE,
    DIALOG_BARCODE_SOURCE,
    DIALOG_BARCODE_ADD_MANUALLY,
    DIALOG_BARCODE_SAVE_ERROR,
    DIALOG_BARCODE_SCAN_AGAIN,
    DIALOG_BARCODE_CAMERA_ERROR,
    DIALOG_BARCODE_MANUAL_HINT,
    DIALOG_BARCODE_SEARCHING,
    DIALOG_PROMO_TITLE,
    DIALOG_PROMO_SUBTITLE,
    DIALOG_PROMO_INPUT_LABEL,
    DIALOG_PROMO_ACTIVATE_BTN,
    DIALOG_PAYMENT_TITLE,
    DIALOG_PAYMENT_SELECT_METHOD,
    DIALOG_PAYMENT_REQUISITES_HEADER,
    DIALOG_PAYMENT_AMOUNT_TO_PAY,
    DIALOG_PAYMENT_NOTE_LABEL,
    DIALOG_PAYMENT_CONFIRM_BTN,
    DIALOG_PAYMENT_SECURITY_FOOTER,
    DIALOG_RECEIPT_TITLE,
    DIALOG_RECEIPT_SUBTITLE_FMT,
    DIALOG_RECEIPT_PENDING_BADGE,
    DIALOG_RECEIPT_ORDER_ID,
    DIALOG_RECEIPT_DATE,
    DIALOG_RECEIPT_METHOD,
    DIALOG_RECEIPT_TOTAL,
    DIALOG_RECEIPT_DONE_BTN,
    ADMIN_REQUISITES_HEADER,
    ADMIN_REQUISITES_SUBTITLE,
    ADMIN_REQUISITES_ENABLED_METHODS,
    ADMIN_REQUISITES_SAVE_BTN,
    ADMIN_REQUISITES_SAVED_TOAST,

    // Foods extra
    FOODS_TITLE,
    CATALOG_DISHES,
    CATALOG_PRODUCTS,
    CATALOG_ALL_CUISINES,
    CATALOG_EMPTY_HINT,
    DISH_ESTIMATE_NOTE,
    RECIPE_CALCULATED_NOTE,
    RECIPE_MY_RECIPE,
    RECIPE_BUILDER_HINT,
    RECIPE_NAME,
    RECIPE_ADD_INGREDIENT,
    RECIPE_INGREDIENT_NOT_FOUND,
    RECIPE_FINISHED_WEIGHT,
    RECIPE_VALIDATION_HINT,
    RECIPE_SAVE,
    FOODS_SUBTITLE,
    FOODS_SEARCH_HINT,
    FOODS_CAT_ALL,
    FOODS_CAT_MEAT,
    FOODS_CAT_FISH,
    FOODS_CAT_GRAINS,
    FOODS_CAT_DAIRY,
    FOODS_CAT_VEG,
    FOODS_CAT_FRUITS,
    FOODS_CAT_SNACKS,
    FOODS_CREATE_BTN,
    FOOD_CUSTOM_BADGE,
    FOOD_NAME_LABEL,
    FOOD_CATEGORY_LABEL,
    FOOD_KCAL_100G_LABEL,

    // Admin extra
    ADMIN_TAB_FAMILY,
    ADMIN_TAB_REQUISITES,
    ADMIN_TAB_TIERS_JSON,
    ADMIN_FAMILY_TITLE,
    ADMIN_FAMILY_DESC,
    ADMIN_FAMILY_SHARE_BTN,
    ADMIN_COPY_CODE,
    ADMIN_QUICK_GUEST_LINKS,
    ADMIN_GUEST_SLIM_7D,
    ADMIN_GUEST_PREMIUM_7D,
    ADMIN_GUEST_PREMIUM_30D,
    ADMIN_SHARE_ACTION,
    ADMIN_CUSTOM_INVITE_CREATE,
    ADMIN_CUSTOM_INVITE_HIDE,
    ADMIN_ACTIVE_INVITES_LIST,
    ADMIN_DURATION_FOREVER,

    // Insights 5 Tips
    TIP_1_TITLE,
    TIP_1_SUBTITLE,
    TIP_1_CONTENT,
    TIP_2_TITLE,
    TIP_2_SUBTITLE,
    TIP_2_CONTENT,
    TIP_3_TITLE,
    TIP_3_SUBTITLE,
    TIP_3_CONTENT,
    TIP_4_TITLE,
    TIP_4_SUBTITLE,
    TIP_4_CONTENT,
    TIP_5_TITLE,
    TIP_5_SUBTITLE,
    TIP_5_CONTENT,

    // Profile extras
    PROFILE_SMART_CALC_TITLE,
    PROFILE_SMART_CALC_DESC,
    PROFILE_RECALC_BTN,
    PROFILE_CHANGE_PLAN_BTN,
    PROFILE_PAYMENT_BTN,
    PROFILE_PROMO_BTN,

    LANGUAGE_DROPDOWN_DESC,
    PROMO_ACTIVATION_HINT,
    PROMO_INVALID_CODE,
    ADMIN_PROMO_CODE_LABEL,
    ADMIN_SOURCE_LABEL,
    ADMIN_VERSION_LABEL,
    ADMIN_PAYMENT_SETTINGS_TITLE,
    ADMIN_PAYMENT_SETTINGS_DESC,
    ADMIN_PAYMENT_METHODS_ENABLED,
    ADMIN_PAYMENT_METHODS_HINT,
    ADMIN_MONOBANK_SECTION,
    ADMIN_MONOBANK_CARD_LABEL,
    ADMIN_MONOBANK_JAR_LABEL,
    ADMIN_PAYPAL_SECTION,
    ADMIN_PAYPAL_EMAIL_LABEL,
    ADMIN_PAYPAL_ME_LABEL,
    ADMIN_CRYPTO_SECTION,
    ADMIN_USDT_ADDRESS_LABEL,
    ADMIN_USDT_NETWORK_LABEL,
    ADMIN_BTC_ADDRESS_LABEL,
    ADMIN_BANK_SECTION,
    ADMIN_CARD_NUMBER_LABEL,
    ADMIN_CARD_HOLDER_LABEL,
    ADMIN_IBAN_LABEL,
    ADMIN_SUPPORT_CONTACTS,
    ADMIN_TELEGRAM_LABEL,
    ADMIN_SUPPORT_EMAIL_LABEL,
    ADMIN_PAYMENT_DETAILS_SAVED,
    ADMIN_SAVE_PAYMENT_DETAILS,
    SHARE_LABEL,
    COPY_LABEL
}

object LocalizationManager {
    private const val PREFS_NAME = "slimtrack_localization_prefs"
    private const val KEY_LANGUAGE = "selected_language_code"

    private lateinit var preferences: SharedPreferences
    private val _currentLanguage = MutableStateFlow(AppLanguage.RU)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = preferences.getString(KEY_LANGUAGE, AppLanguage.RU.code) ?: AppLanguage.RU.code
        _currentLanguage.value = AppLanguage.fromCode(savedCode)
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        if (::preferences.isInitialized) {
            preferences.edit().putString(KEY_LANGUAGE, language.code).apply()
        }
    }

    fun getString(key: StringKey, language: AppLanguage = _currentLanguage.value): String {
        return translations[language]?.get(key)
            ?: translations[AppLanguage.EN]?.get(key)
            ?: translations[AppLanguage.RU]?.get(key)
            ?: key.name
    }

    /** Used by tests/diagnostics to prevent partially translated releases. */
    fun missingTranslationKeys(language: AppLanguage): List<StringKey> =
        StringKey.entries.filterNot { key -> translations[language]?.containsKey(key) == true }

    private val translations: Map<AppLanguage, Map<StringKey, String>> = mapOf(
        AppLanguage.RU to mapOf(
            StringKey.STEPS_LABEL to "Среднее число шагов в день",
            StringKey.HOUSEHOLD_LABEL to "Активные домашние дела, минут в день",
            StringKey.LIFESTYLE_HELP to "Укажите среднюю активность за обычную неделю. Шаги включают ходьбу дома и на работе. Домашние дела — уборка, работа в саду и другая работа в движении. Пустое поле означает «не знаю».",
            StringKey.LIFESTYLE_AUTO to "Оценка по шагам и домашним делам; одинаковая нагрузка не учитывается дважды. Для физической работы или спорта можно выбрать уровень вручную — поля выше очистятся.",
            StringKey.PROFILE_ERROR_STEPS to "Введите целое число шагов от 0 до 50 000 или оставьте поле пустым.",
            StringKey.PROFILE_ERROR_HOUSEHOLD to "Введите от 0 до 480 минут или оставьте поле пустым.",
            StringKey.PLAN_MAINTENANCE to "Поддержание веса (оценка)",
            StringKey.PLAN_DEFICIT to "Фактический дефицит",
            StringKey.PLAN_TARGET to "Норма на день",
            StringKey.PLAN_ESTIMATE to "Это стартовая оценка. Сверяйте её с изменением среднего веса за 2–3 недели. Выбранный темп ограничивается фактическим дефицитом и нижней границей калорий.",
            StringKey.PACE_LIMIT to "Дефицит: не более 20% расхода, с учётом нижней границы калорий. Темп ниже — пожелание, а не обещание.",
            StringKey.APP_NAME to "SlimTrack",
            StringKey.TAB_DIARY to "Дневник",
            StringKey.TAB_WEIGHT to "Вес",
            StringKey.TAB_FOODS to "База",
            StringKey.TAB_INSIGHTS to "Советы",
            StringKey.TAB_PROFILE to "Профиль",
            StringKey.HEADER_PLANS_BTN to "Тарифы ⭐",
            StringKey.GUEST_ACCESS_FOREVER to "Бессрочно 👑",
            StringKey.GUEST_ACCESS_DAYS_LEFT to "Осталось дн.:",
            StringKey.GUEST_ACCESS_ACTIVE_CODE to "Активен • Код:",

            StringKey.SAVE to "Сохранить",
            StringKey.CANCEL to "Отмена",
            StringKey.CLOSE to "Закрыть",
            StringKey.DELETE to "Удалить",
            StringKey.EDIT to "Редактировать",
            StringKey.SEARCH to "Поиск",
            StringKey.ADD to "Добавить",
            StringKey.COPY to "Копировать",
            StringKey.SHARE to "Поделиться",
            StringKey.LOADING to "Загрузка...",
            StringKey.ERROR to "Ошибка",
            StringKey.SUCCESS to "Успешно",
            StringKey.UNLOCKED to "Доступно",
            StringKey.LOCKED to "Заблокировано",
            StringKey.PRO_REQUIRED to "Требуется Slim ($20)",
            StringKey.PREMIUM_REQUIRED to "Требуется Premium ($50)",
            StringKey.ACTIVE_BADGE to "Активен",

            StringKey.LANGUAGE_TITLE to "Язык приложения",
            StringKey.SELECT_LANGUAGE to "Выберите язык интерфейса",

            StringKey.SUBSCRIPTION_TITLE to "Тарифные планы",
            StringKey.CURRENT_PLAN to "Текущий тариф",
            StringKey.PLAN_FREE to "Free (Бесплатный)",
            StringKey.PLAN_SLIM to "Slim ($20)",
            StringKey.PLAN_PREMIUM to "Premium ($50)",
            StringKey.PLAN_ADMIN to "Администратор (VIP)",
            StringKey.FREE_PRICE to "$0",
            StringKey.SLIM_PRICE to "$20",
            StringKey.PREMIUM_PRICE to "$50",
            StringKey.UPGRADE_PLAN to "Улучшить тариф",
            StringKey.UNLOCK_ALL to "Открыть все функции",
            StringKey.PAYWALL_TITLE to "Все возможности SlimTrack",
            StringKey.PAYWALL_SUBTITLE to "Выберите подходящий тариф для быстрого и комфортного снижения веса",
            StringKey.PAYWALL_FEATURE_LOCKED_FMT to "Функция доступна в тарифе",
            StringKey.PAYWALL_ALREADY_ACTIVE to "Тариф уже активен ✓",
            StringKey.PAYWALL_BUY_BTN_FMT to "Оплатить",
            StringKey.PAYWALL_SELECT_FREE to "Выбрать бесплатный тариф",
            StringKey.PAYWALL_HAVE_PROMO to "🎁 Есть промокод или гостевая ссылка?",
            StringKey.PAYWALL_SECURITY_NOTE to "🔒 Мгновенная активация и выдача квитанции",
            StringKey.CHOOSE_PLAN to "Выбрать тариф",
            StringKey.POPULAR_CHOICE to "Популярный 🔥",
            StringKey.BEST_VALUE to "VIP 👑",

            StringKey.ADMIN_PANEL_TITLE to "Панель администратора",
            StringKey.ADMIN_PANEL_SUBTITLE to "Управление доступом, оплатой и обновлениями",
            StringKey.ADMIN_FILE_IMPORT_TITLE to "Импорт файла конфигурации (.json)",
            StringKey.ADMIN_FILE_IMPORT_DESC to "Загрузите JSON-файл для обновления тарифов или базы продуктов",
            StringKey.ADMIN_IMPORT_BUTTON to "Загрузить .JSON",
            StringKey.ADMIN_EXPORT_BUTTON to "Копировать JSON",
            StringKey.ADMIN_RESET_BUTTON to "Сброс настроек",
            StringKey.ADMIN_TIER_SIMULATOR to "Тестовый симулятор тарифа",
            StringKey.ADMIN_CONFIG_STATUS to "Статус конфигурации",
            StringKey.ADMIN_CONFIG_SOURCE_DEFAULT to "Встроенная",
            StringKey.ADMIN_CONFIG_SOURCE_FILE to "Из файла",
            StringKey.ADMIN_CONFIG_UPDATED to "Обновлено",
            StringKey.ADMIN_VIEW_JSON to "Показать JSON",
            StringKey.ADMIN_SUCCESS_IMPORT to "Конфигурация успешно применена!",
            StringKey.ADMIN_ERROR_IMPORT to "Ошибка чтения файла конфигурации",
            StringKey.ADMIN_EXTRA_FOODS_IMPORTED to "Импортировано продуктов",

            StringKey.DIARY_CALORIES_LEFT to "Осталось",
            StringKey.DIARY_CONSUMED to "Съедено",
            StringKey.DIARY_BURNED to "Сожжено",
            StringKey.DIARY_TARGET to "Норма",
            StringKey.DIARY_PROTEIN to "Белки",
            StringKey.DIARY_FAT to "Жиры",
            StringKey.DIARY_CARBS to "Углеводы",
            StringKey.DIARY_MEAL_BREAKFAST to "Завтрак",
            StringKey.DIARY_MEAL_LUNCH to "Обед",
            StringKey.DIARY_MEAL_DINNER to "Ужин",
            StringKey.DIARY_MEAL_SNACKS to "Перекус",
            StringKey.DIARY_ADD_FOOD to "Добавить",
            StringKey.DIARY_QUICK_CALORIES to "Быстрые ккал",
            StringKey.DIARY_COPY_YESTERDAY to "Вчерашнее",
            StringKey.DIARY_FASTING_TIMER to "Интервальное голодание",
            StringKey.DIARY_FASTING_START to "Старт",
            StringKey.DIARY_FASTING_STOP to "Стоп",
            StringKey.DIARY_WATER_TRACKER to "Трекер воды",
            StringKey.DIARY_ADD_EXERCISE to "Активность",
            StringKey.DIARY_BARCODE_SCANNER to "Штрих-код",

            StringKey.WEIGHT_TITLE to "Динамика веса",
            StringKey.WEIGHT_CURRENT to "Текущий",
            StringKey.WEIGHT_START to "Старт",
            StringKey.WEIGHT_TARGET to "Цель",
            StringKey.WEIGHT_TREND to "Трендовый вес",
            StringKey.WEIGHT_LOG_BUTTON to "Записать вес",
            StringKey.WEIGHT_CHART_TITLE to "График изменения веса",
            StringKey.WEIGHT_MEASUREMENTS_TITLE to "Замеры тела",
            StringKey.WEIGHT_WEEKLY_DEFICIT_TITLE to "Недельный дефицит калорий",

            StringKey.FOOD_SEARCH_PLACEHOLDER to "Поиск продуктов по названию...",
            StringKey.FOOD_ALL_CATEGORIES to "Все категории",
            StringKey.FOOD_CREATE_CUSTOM to "Свой продукт",

            StringKey.INSIGHTS_TITLE to "Аналитика и советы",
            StringKey.INSIGHTS_STREAKS to "Трекер привычек",
            StringKey.INSIGHTS_WEEKLY_CHART to "Баланс за 7 дней",
            StringKey.INSIGHTS_EXPORT_REPORT to "Экспорт отчета для тренера / врача",

            StringKey.PROFILE_TITLE to "Профиль и параметры",
            StringKey.PROFILE_SUBTITLE to "Персональный расчет дефицита калорий",
            StringKey.PROFILE_PERSONAL_DATA to "Личные параметры тела",
            StringKey.PROFILE_GENDER to "Пол",
            StringKey.GENDER_MALE to "Мужской",
            StringKey.GENDER_FEMALE to "Женский",
            StringKey.PROFILE_AGE to "Возраст (лет)",
            StringKey.PROFILE_HEIGHT to "Рост (см)",
            StringKey.PROFILE_START_WEIGHT to "Начальный вес (кг)",
            StringKey.PROFILE_CURRENT_WEIGHT to "Текущий вес (кг)",
            StringKey.PROFILE_TARGET_WEIGHT to "Желаемый вес (кг)",
            StringKey.PROFILE_ACTIVITY_LEVEL to "Образ жизни и подвижность",
            StringKey.PROFILE_GOAL_PACE to "Желаемый темп похудения",
            StringKey.PROFILE_MACROS_TITLE to "Дневные нормы питания (БЖУ)",
            StringKey.PROFILE_CALORIES_GOAL to "Цель калорий (ккал)",
            StringKey.PROFILE_PROTEIN_GOAL to "Белки (г)",
            StringKey.PROFILE_FAT_GOAL to "Жиры (г)",
            StringKey.PROFILE_CARBS_GOAL to "Углеводы (г)",
            StringKey.PROFILE_WATER_GOAL to "Норма воды (мл)",
            StringKey.PROFILE_AUTO_CALC to "Пересчитать нормы",
            StringKey.PROFILE_SAVE_SETTINGS to "Сохранить параметры",
            StringKey.PROFILE_SAVED_SUCCESS to "Параметры сохранены! ✓",
            StringKey.PROFILE_VALIDATION_FIX_ERRORS to "Проверьте выделенные поля перед сохранением.",
            StringKey.PROFILE_ERROR_NAME_TOO_LONG to "Имя слишком длинное.",
            StringKey.PROFILE_ERROR_ADULT_ONLY to "Автоматические расчёты SlimTrack доступны только для пользователей 18+.",
            StringKey.PROFILE_ERROR_AGE_RANGE to "Введите возраст от 18 до 100 лет.",
            StringKey.PROFILE_ERROR_HEIGHT_RANGE to "Введите рост от 120 до 230 см.",
            StringKey.PROFILE_ERROR_WEIGHT_RANGE to "Введите вес от 30 до 250 кг.",
            StringKey.PROFILE_ERROR_TARGET_ABOVE_CURRENT to "Для режима снижения веса цель не должна быть выше текущего веса.",
            StringKey.PROFILE_ERROR_TARGET_TOO_LOW to "Эта цель выходит за поддерживаемый диапазон для самостоятельного снижения веса.",
            StringKey.PROFILE_ERROR_CALORIES_RANGE to "Введите поддерживаемую дневную цель калорий.",
            StringKey.PROFILE_ERROR_MACRO_RANGE to "Введите поддерживаемое значение макронутриента.",
            StringKey.PROFILE_ERROR_WATER_RANGE to "Введите цель воды от 500 до 6000 мл.",
            StringKey.PROFILE_SAFE_CALC_NOTE to "Авторасчёт работает только для взрослых и ограничивает чрезмерный дефицит.",

            StringKey.ACTIVITY_SEDENTARY to "Сидячий образ жизни",
            StringKey.ACTIVITY_SEDENTARY_DESC to "Менее 3000 шагов, большую часть дня сидя",
            StringKey.ACTIVITY_LIGHT to "Легкая активность",
            StringKey.ACTIVITY_LIGHT_DESC to "3000–6000 шагов, прогулки, обычные домашние дела",
            StringKey.ACTIVITY_MODERATE to "Умеренная активность",
            StringKey.ACTIVITY_MODERATE_DESC to "6000–10000 шагов, много времени на ногах, активные дела",
            StringKey.ACTIVITY_ACTIVE to "Высокая активность",
            StringKey.ACTIVITY_ACTIVE_DESC to "Более 10000 шагов, физическая работа или регулярный спорт",

            StringKey.PACE_EASY to "Мягкий (-0.25 кг/нед)",
            StringKey.PACE_RECOMMENDED to "Оптимальный (-0.5 кг/нед)",
            StringKey.PACE_FAST to "Быстрый (-0.75 кг/нед)",
            StringKey.PACE_MAX to "Максимум (-1.0 кг/нед)",

            StringKey.ANALYSIS_RESULT_TITLE to "Анализ параметров тела",
            StringKey.ANALYSIS_RESULT_SUBTITLE to "Расчет ИМТ, расхода калорий и сроков цели",
            StringKey.ANALYSIS_BMI_LABEL to "Индекс массы тела (ИМТ)",
            StringKey.BMI_UNDERWEIGHT to "Дефицит массы",
            StringKey.BMI_NORMAL to "Норма веса ✓",
            StringKey.BMI_OVERWEIGHT to "Избыточный вес",
            StringKey.BMI_OBESITY to "Ожирение",
            StringKey.ANALYSIS_IDEAL_WEIGHT_RANGE to "Здоровый вес",
            StringKey.ANALYSIS_DIFF_TO_GOAL to "До цели",
            StringKey.ANALYSIS_TO_LOSE to "Сбросить",
            StringKey.ANALYSIS_TO_GAIN to "Набрать",
            StringKey.ANALYSIS_TDEE_LABEL to "Суточный расход (TDEE)",
            StringKey.ANALYSIS_BMR_LABEL to "Базовый обмен (BMR)",
            StringKey.ANALYSIS_WEEKS_ESTIMATE to "Прогноз срока",
            StringKey.ANALYSIS_HELP_BUTTON_TEXT to "Разбор ❔",

            StringKey.EXPLAINER_DIALOG_TITLE to "Разбор ваших параметров",
            StringKey.EXPLAINER_SECTION_WHAT_IT_MEANS to "1. Что означают показатели",
            StringKey.EXPLAINER_SECTION_PROBLEMS to "2. На что это влияет",
            StringKey.EXPLAINER_SECTION_HOW_TO_IMPROVE to "3. Как улучшить результат",
            StringKey.EXPLAINER_SECTION_HOW_APP_HELPS to "4. Как помогает SlimTrack",
            StringKey.EXPLAINER_SECTION_MOTIVATION to "5. Мотивация",
            StringKey.EXPLAINER_GOT_IT_BUTTON to "Понятно, к цели! ✓",

            StringKey.LEAD_MAGNET_TITLE to "Откройте 100% возможностей PRO",
            StringKey.LEAD_MAGNET_SUBTITLE to "Сканер штрих-кодов, разбор тела, фастинг и авто-БЖУ",
            StringKey.LEAD_MAGNET_PERK_1 to "Сканер штрих-кодов и быстрый ввод калорий",
            StringKey.LEAD_MAGNET_PERK_2 to "Медицинский разбор ИМТ и прогноз в неделях",
            StringKey.LEAD_MAGNET_PERK_3 to "Таймер голодания 16:8 и экспорт отчетов",
            StringKey.LEAD_MAGNET_BUTTON to "Тарифы и скидки ⭐",

            StringKey.DIARY_WATER_HINT to "Ускоряет обмен веществ",
            StringKey.DIARY_EXERCISE_TITLE to "Активность и спорт",
            StringKey.DIARY_EXERCISE_HINT to "Увеличивает лимит калорий",
            StringKey.DIARY_NO_ITEMS_YET to "Нет записей",
            StringKey.DIARY_HABITS_TITLE to "Трекер привычек",
            StringKey.DIARY_HABITS_DESC to "Ежедневный чек-лист дисциплины",
            StringKey.DIARY_OPEN_HABITS to "Открыть",
            StringKey.DIARY_CALORIES_OVER to "превышено",
            StringKey.DIARY_CURRENT_DEFICIT to "Дефицит:",
            StringKey.DIARY_CURRENT_SURPLUS to "Профицит:",
            StringKey.DIARY_MACROS_TITLE to "Макронутриенты (БЖУ)",

            StringKey.ADMIN_TAB_UPDATE to "Обновление (.APK)",
            StringKey.ADMIN_INSTALL_APK_BUTTON to "Установить обновление (.apk)",
            StringKey.ADMIN_INSTALL_APK_DESC to "Выберите скачанный APK для обновления поверх без потери данных",
            StringKey.ADMIN_APK_NO_DELETE_NOTE to "💡 Удалять приложение не нужно! Android обновит его поверх и сохранит все записи.",

            StringKey.UNIT_KCAL to "ккал",
            StringKey.UNIT_GRAM to "г",
            StringKey.UNIT_ML to "мл",
            StringKey.UNIT_CM to "см",
            StringKey.UNIT_MIN to "мин",

            StringKey.INSIGHTS_SUBTITLE to "Научный подход к снижению веса",
            StringKey.INSIGHTS_FASTING_TITLE to "Интервальное голодание 16:8",
            StringKey.INSIGHTS_FASTING_ACTIVE to "Фаза активного голодания",
            StringKey.INSIGHTS_FASTING_STOPPED to "Таймер остановлен",
            StringKey.INSIGHTS_FASTING_BTN_START to "Старт",
            StringKey.INSIGHTS_FASTING_BTN_STOP to "Стоп",
            StringKey.INSIGHTS_RULES_TITLE to "5 правил устойчивого похудения",

            StringKey.WEIGHT_HEADER_SUBTITLE to "Контроль массы тела и объемов",
            StringKey.WEIGHT_ADD_BUTTON to "Записать вес",
            StringKey.WEIGHT_LOST_LABEL to "Сброшено",
            StringKey.WEIGHT_REMAINING_LABEL to "До цели",
            StringKey.WEIGHT_WEEKS_LABEL to "Недель",
            StringKey.WEIGHT_PROGRESS_TO_GOAL to "Прогресс к цели",
            StringKey.WEIGHT_FORECAST_TEXT to "Прогноз до цели: ≈",
            StringKey.WEIGHT_HISTORY_TITLE to "История взвешиваний",
            StringKey.WEIGHT_EMPTY_HISTORY to "Нет записей. Нажмите «Записать вес»!",
            StringKey.WEIGHT_UNIT_KG to "кг",

            StringKey.CHART_SUBTITLE to "Динамика массы тела",
            StringKey.CHART_FILTER_7D to "7 дн.",
            StringKey.CHART_FILTER_30D to "30 дн.",
            StringKey.CHART_FILTER_ALL to "Все",
            StringKey.CHART_EMPTY to "Нет данных. Запишите свой вес!",
            StringKey.CHART_ACTUAL_WEIGHT to "Вес",
            StringKey.TREND_CARD_TITLE to "Трендовый вес",
            StringKey.TREND_CARD_SUBTITLE to "Сглаженная линия без скачков воды",
            StringKey.TREND_LABEL_TREND to "Тренд",
            StringKey.TREND_LABEL_SCALE to "На весах",
            StringKey.TREND_INFO_GOOD to "Задержка воды скрывает отвес — сжигание жира идет по плану!",
            StringKey.TREND_INFO_NORMAL to "Вес стабильно снижается благодаря дефициту калорий.",
            StringKey.WEEKLY_DEFICIT_SUBTITLE to "Суммарный баланс за 7 дней",
            StringKey.WEEKLY_FAT_BURN_FORECAST to "Сжигание жира за неделю: ≈",
            StringKey.WEEKLY_FAT_BURN_NOTE to "7 700 ккал дефицита = 1 кг жировой ткани",
            StringKey.MEAS_TITLE to "Замеры объемов тела",
            StringKey.MEAS_SUBTITLE to "Объемы в сантиметрах",
            StringKey.MEAS_WAIST to "Талия",
            StringKey.MEAS_HIPS to "Бедра",
            StringKey.MEAS_CHEST to "Грудь",
            StringKey.MEAS_BICEP to "Бицепс",
            StringKey.MEAS_THIGH to "Бедро",
            StringKey.MEAS_ADD_BTN to "Записать",
            StringKey.MEAS_EMPTY_HINT to "Запишите объемы талии и бедер для контроля прогресса",
            StringKey.HABIT_STREAK_BADGE to "дн. подряд",
            StringKey.HABIT_1 to "Выпить дневную норму воды",
            StringKey.HABIT_2 to "Выполнить норму по белку",
            StringKey.HABIT_3 to "Уложиться в дефицит калорий",
            StringKey.HABIT_4 to "Минимум 8 000 шагов / спорт",
            StringKey.HABIT_5 to "Без еды за 3 часа до сна",
            StringKey.EXPORT_SUBTITLE to "Сводка прогресса по весу и КБЖУ",
            StringKey.EXPORT_SHARE_BTN to "Поделиться",
            StringKey.EXPORT_COPY_BTN to "Копировать",
            StringKey.EXPORT_COPIED_TOAST to "Отчет скопирован!",

            StringKey.DIALOG_ADD_FOOD_TITLE to "Добавить в",
            StringKey.DIALOG_GRAMS_LABEL to "Вес порции (г)",
            StringKey.DIALOG_EMPTY_FOODS to "Продукты не найдены",
            StringKey.DIALOG_ADD_EXERCISE_TITLE to "Добавить активность",
            StringKey.DIALOG_EXERCISE_NAME_LABEL to "Название тренировки",
            StringKey.DIALOG_EXERCISE_MINUTES_LABEL to "Длительность (мин)",
            StringKey.DIALOG_EXERCISE_KCAL_LABEL to "Сожжено (ккал)",
            StringKey.DIALOG_ADD_WEIGHT_TITLE to "Записать вес",
            StringKey.DIALOG_WEIGHT_INPUT_LABEL to "Ваш вес (кг)",
            StringKey.DIALOG_NOTE_LABEL to "Заметка (необязательно)",
            StringKey.DIALOG_QUICK_CALORIES_TITLE to "Быстрые калории",
            StringKey.DIALOG_QUICK_CALORIES_SUBTITLE to "Мгновенный ввод калорий без поиска блюда",
            StringKey.DIALOG_BARCODE_TITLE to "Сканер штрих-кода",
            StringKey.DIALOG_BARCODE_SUBTITLE to "Наведите камеру на штрих-код EAN/UPC или введите код вручную",
            StringKey.DIALOG_BARCODE_INPUT_LABEL to "Штрих-код",
            StringKey.DIALOG_BARCODE_CAMERA_HINT to "Поместите штрих-код в рамку",
            StringKey.DIALOG_BARCODE_CAMERA_PERMISSION to "Для сканирования нужен доступ к камере. Штрих-коды обрабатываются на устройстве.",
            StringKey.DIALOG_BARCODE_GRANT_PERMISSION to "Разрешить камеру",
            StringKey.DIALOG_BARCODE_NOT_FOUND to "Товар не найден в онлайн-базе. Можно добавить данные с упаковки.",
            StringKey.DIALOG_BARCODE_NETWORK_ERROR to "Нет соединения с онлайн-базой. Проверьте интернет и повторите поиск.",
            StringKey.DIALOG_BARCODE_SERVICE_ERROR to "Онлайн-база временно недоступна. Повторите поиск позже.",
            StringKey.DIALOG_BARCODE_INCOMPLETE to "Товар найден, но данные калорий и БЖУ неполные. Введите значения с упаковки на 100 г.",
            StringKey.DIALOG_BARCODE_INVALID_CODE to "Введите штрих-код из 8, 12, 13 или 14 цифр.",
            StringKey.DIALOG_BARCODE_SOURCE to "Онлайн-каталог: Open Food Facts • ODbL",
            StringKey.DIALOG_BARCODE_ADD_MANUALLY to "Добавить данные с упаковки",
            StringKey.DIALOG_BARCODE_SAVE_ERROR to "Не удалось сохранить продукт. Повторите попытку.",
            StringKey.DIALOG_BARCODE_SCAN_AGAIN to "Сканировать снова",
            StringKey.DIALOG_BARCODE_CAMERA_ERROR to "Не удалось запустить камеру. Можно ввести код вручную.",
            StringKey.DIALOG_BARCODE_MANUAL_HINT to "Или введите код вручную",
            StringKey.DIALOG_BARCODE_SEARCHING to "Ищем продукт онлайн…",
            StringKey.DIALOG_PROMO_TITLE to "Промокод или ссылка",
            StringKey.DIALOG_PROMO_SUBTITLE to "Введите код доступа для активации Slim / Premium",
            StringKey.DIALOG_PROMO_INPUT_LABEL to "Промокод или ссылка",
            StringKey.DIALOG_PROMO_ACTIVATE_BTN to "Активировать доступ ✓",
            StringKey.DIALOG_PAYMENT_TITLE to "Оплата подписки",
            StringKey.DIALOG_PAYMENT_SELECT_METHOD to "Выберите способ оплаты:",
            StringKey.DIALOG_PAYMENT_REQUISITES_HEADER to "Реквизиты для перевода:",
            StringKey.DIALOG_PAYMENT_AMOUNT_TO_PAY to "Сумма к оплате:",
            StringKey.DIALOG_PAYMENT_NOTE_LABEL to "Примечание / Фамилия / TXID",
            StringKey.DIALOG_PAYMENT_CONFIRM_BTN to "Я оплатил(а) — Создать заявку на проверку",
            StringKey.DIALOG_PAYMENT_SECURITY_FOOTER to "🔒 Доступ активируется только после подтверждения оплаты",
            StringKey.DIALOG_RECEIPT_TITLE to "Заявка на проверку создана",
            StringKey.DIALOG_RECEIPT_SUBTITLE_FMT to "Отправьте заявку в поддержку. Доступ пока не активирован",
            StringKey.DIALOG_RECEIPT_PENDING_BADGE to "НА ПРОВЕРКЕ",
            StringKey.DIALOG_RECEIPT_ORDER_ID to "Номер заказа:",
            StringKey.DIALOG_RECEIPT_DATE to "Дата и время:",
            StringKey.DIALOG_RECEIPT_METHOD to "Способ оплаты:",
            StringKey.DIALOG_RECEIPT_TOTAL to "Итого:",
            StringKey.DIALOG_RECEIPT_DONE_BTN to "Готово",
            StringKey.ADMIN_REQUISITES_HEADER to "⚙️ Настройка реквизитов оплаты",
            StringKey.ADMIN_REQUISITES_SUBTITLE to "Укажите ваши карты и кошельки для приема оплаты",
            StringKey.ADMIN_REQUISITES_ENABLED_METHODS to "Включенные способы оплаты:",
            StringKey.ADMIN_REQUISITES_SAVE_BTN to "Сохранить реквизиты ✓",
            StringKey.ADMIN_REQUISITES_SAVED_TOAST to "Реквизиты сохранены!",

            StringKey.FOODS_TITLE to "Каталог",
            StringKey.CATALOG_DISHES to "Блюда",
            StringKey.CATALOG_PRODUCTS to "Продукты",
            StringKey.CATALOG_ALL_CUISINES to "Все кухни",
            StringKey.CATALOG_EMPTY_HINT to "Ничего не найдено. Попробуйте другое название или сбросьте фильтры. Свое блюдо можно добавить во вкладке «Мой рецепт».",
            StringKey.DISH_ESTIMATE_NOTE to "Ориентировочные КБЖУ стандартного рецепта. Состав, масло и способ приготовления меняют результат.",
            StringKey.RECIPE_CALCULATED_NOTE to "Расчет по вашим ингредиентам и готовому весу. Точность зависит от введенных данных.",
            StringKey.RECIPE_MY_RECIPE to "Мой рецепт",
            StringKey.RECIPE_BUILDER_HINT to "Добавьте ингредиенты и их вес. Выбирайте правильное состояние: сырой или приготовленный продукт. Учитывайте съеденное масло. Затем взвесьте все готовое блюдо без посуды, включая воду в супе.",
            StringKey.RECIPE_NAME to "Название блюда",
            StringKey.RECIPE_ADD_INGREDIENT to "Найти ингредиент",
            StringKey.RECIPE_INGREDIENT_NOT_FOUND to "Ингредиент не найден. Его можно сначала добавить во вкладке «Свой продукт» по данным с упаковки.",
            StringKey.RECIPE_FINISHED_WEIGHT to "Вес всего готового блюда, г",
            StringKey.RECIPE_VALIDATION_HINT to "Укажите положительный вес каждого ингредиента и всего готового блюда. Проверьте итоговый вес.",
            StringKey.RECIPE_SAVE to "Сохранить блюдо",
            StringKey.FOODS_SUBTITLE to "Калорийность и БЖУ на 100 г",
            StringKey.FOODS_SEARCH_HINT to "Найти блюдо или продукт…",
            StringKey.FOODS_CAT_ALL to "Все",
            StringKey.FOODS_CAT_MEAT to "Мясо и птица",
            StringKey.FOODS_CAT_FISH to "Рыба",
            StringKey.FOODS_CAT_GRAINS to "Крупы",
            StringKey.FOODS_CAT_DAIRY to "Молочные продукты",
            StringKey.FOODS_CAT_VEG to "Овощи",
            StringKey.FOODS_CAT_FRUITS to "Фрукты",
            StringKey.FOODS_CAT_SNACKS to "Орехи и снеки",
            StringKey.FOODS_CREATE_BTN to "Свой продукт",
            StringKey.FOOD_CUSTOM_BADGE to "Свой",
            StringKey.FOOD_NAME_LABEL to "Название продукта",
            StringKey.FOOD_CATEGORY_LABEL to "Категория",
            StringKey.FOOD_KCAL_100G_LABEL to "Ккал на 100 г",

            StringKey.ADMIN_TAB_FAMILY to "Семья & Гости",
            StringKey.ADMIN_TAB_REQUISITES to "Реквизиты",
            StringKey.ADMIN_TAB_TIERS_JSON to "Тарифы & JSON",
            StringKey.ADMIN_FAMILY_TITLE to "Семейный VIP доступ НАВСЕГДА 👑",
            StringKey.ADMIN_FAMILY_DESC to "Бессрочный доступ ко всем функциям для близких",
            StringKey.ADMIN_FAMILY_SHARE_BTN to "Отправить семье",
            StringKey.ADMIN_COPY_CODE to "Код",
            StringKey.ADMIN_QUICK_GUEST_LINKS to "Быстрые гостевые инвайты:",
            StringKey.ADMIN_GUEST_SLIM_7D to "Гостевой Slim (7 дней)",
            StringKey.ADMIN_GUEST_PREMIUM_7D to "Пробный Premium (7 дней)",
            StringKey.ADMIN_GUEST_PREMIUM_30D to "Полный Premium (30 дней)",
            StringKey.ADMIN_SHARE_ACTION to "Поделиться",
            StringKey.ADMIN_CUSTOM_INVITE_CREATE to "Создать свой промокод",
            StringKey.ADMIN_CUSTOM_INVITE_HIDE to "Скрыть конструктор",
            StringKey.ADMIN_ACTIVE_INVITES_LIST to "Активные коды",
            StringKey.ADMIN_DURATION_FOREVER to "Бессрочно 👑",

            StringKey.TIP_1_TITLE to "1. Дефицит калорий — основа",
            StringKey.TIP_1_SUBTITLE to "Закон энергетического баланса",
            StringKey.TIP_1_CONTENT to "Расходуйте на 400–500 ккал больше, чем потребляете. Это обеспечивает стабильное сжигание жира без голодовок и срывов.",
            StringKey.TIP_2_TITLE to "2. Белок защищает мышцы",
            StringKey.TIP_2_SUBTITLE to "Норма: 1.6 – 2.0 г на 1 кг веса",
            StringKey.TIP_2_CONTENT to "Белковая пища (птица, рыба, яйца, творог) сохраняет мышцы при похудении и надолго устраняет чувство голода.",
            StringKey.TIP_3_TITLE to "3. Вода и колебания веса",
            StringKey.TIP_3_SUBTITLE to "Почему вес меняется за сутки",
            StringKey.TIP_3_CONTENT to "Соль и углеводы временно задерживают воду. Пейте 30 мл воды на 1 кг веса для снятия отеков и высокого метаболизма.",
            StringKey.TIP_4_TITLE to "4. Бытовая активность (NEAT)",
            StringKey.TIP_4_SUBTITLE to "Шаги важнее редких тренировок",
            StringKey.TIP_4_CONTENT to "8 000 – 10 000 шагов в день сжигают до 400 ккал без усталости центральной нервной системы и роста аппетита.",
            StringKey.TIP_5_TITLE to "5. Как пройти плато веса",
            StringKey.TIP_5_SUBTITLE to "Если вес стоит 2 недели",
            StringKey.TIP_5_CONTENT to "Учитывайте масла и соусы, следите за замерами талии в сантиметрах и спите не менее 7–8 часов.",

            StringKey.PROFILE_SMART_CALC_TITLE to "Авто-расчет по формуле",
            StringKey.PROFILE_SMART_CALC_DESC to "Формула Миффлина-Сан Жеора под ваш вес",
            StringKey.PROFILE_RECALC_BTN to "Рассчитать",
            StringKey.PROFILE_CHANGE_PLAN_BTN to "Тарифы",
            StringKey.PROFILE_PAYMENT_BTN to "Оплата",
            StringKey.PROFILE_PROMO_BTN to "Промокод",
            StringKey.LANGUAGE_DROPDOWN_DESC to "Список языков",
            StringKey.PROMO_ACTIVATION_HINT to "Введите промокод или гостевую ссылку для активации Slim или Premium",
            StringKey.PROMO_INVALID_CODE to "Неверный код",
            StringKey.ADMIN_PROMO_CODE_LABEL to "Промокод (VIP-FRIEND)",
            StringKey.ADMIN_SOURCE_LABEL to "Источник:",
            StringKey.ADMIN_VERSION_LABEL to "Версия:",
            StringKey.ADMIN_PAYMENT_SETTINGS_TITLE to "⚙️ Настройка платежей",
            StringKey.ADMIN_PAYMENT_SETTINGS_DESC to "Задайте реквизиты для ручной оплаты подписки. Доступ активируется только после подтвержденной проверки платежа.",
            StringKey.ADMIN_PAYMENT_METHODS_ENABLED to "Включенные способы оплаты:",
            StringKey.ADMIN_PAYMENT_METHODS_HINT to "Отключите ненужные способы оплаты, чтобы показывать пользователю только актуальные варианты.",
            StringKey.ADMIN_MONOBANK_SECTION to "Monobank (Украина 🇺🇦)",
            StringKey.ADMIN_MONOBANK_CARD_LABEL to "Номер карты Monobank",
            StringKey.ADMIN_MONOBANK_JAR_LABEL to "Ссылка на банку Monobank",
            StringKey.ADMIN_PAYPAL_SECTION to "PayPal (международные платежи 🌐)",
            StringKey.ADMIN_PAYPAL_EMAIL_LABEL to "Email аккаунта PayPal",
            StringKey.ADMIN_PAYPAL_ME_LABEL to "Ссылка PayPal.Me",
            StringKey.ADMIN_CRYPTO_SECTION to "Криптовалюта (USDT и Bitcoin)",
            StringKey.ADMIN_USDT_ADDRESS_LABEL to "Адрес кошелька USDT",
            StringKey.ADMIN_USDT_NETWORK_LABEL to "Сеть USDT (TRC-20, BEP-20, ERC-20)",
            StringKey.ADMIN_BTC_ADDRESS_LABEL to "Адрес кошелька Bitcoin (BTC)",
            StringKey.ADMIN_BANK_SECTION to "Банковская карта и IBAN",
            StringKey.ADMIN_CARD_NUMBER_LABEL to "Номер карты Visa / MasterCard",
            StringKey.ADMIN_CARD_HOLDER_LABEL to "Имя владельца карты латиницей",
            StringKey.ADMIN_IBAN_LABEL to "Расчетный счет IBAN",
            StringKey.ADMIN_SUPPORT_CONTACTS to "Контакты поддержки для платежей",
            StringKey.ADMIN_TELEGRAM_LABEL to "Telegram поддержки",
            StringKey.ADMIN_SUPPORT_EMAIL_LABEL to "Email поддержки",
            StringKey.ADMIN_PAYMENT_DETAILS_SAVED to "Реквизиты оплаты сохранены",
            StringKey.ADMIN_SAVE_PAYMENT_DETAILS to "Сохранить реквизиты оплаты ✓",
            StringKey.SHARE_LABEL to "Поделиться",
            StringKey.COPY_LABEL to "Копировать"
        ),

        AppLanguage.UK to mapOf(
            StringKey.STEPS_LABEL to "Середня кількість кроків на день",
            StringKey.HOUSEHOLD_LABEL to "Активні домашні справи, хвилин на день",
            StringKey.LIFESTYLE_HELP to "Укажіть середню активність за звичайний тиждень. Кроки включають ходьбу вдома та на роботі. Домашні справи — прибирання, робота в саду та інша робота в русі. Порожнє поле означає «не знаю».",
            StringKey.LIFESTYLE_AUTO to "Оцінка за кроками та домашніми справами; однакове навантаження не враховується двічі. Для фізичної роботи або спорту можна вибрати рівень вручну — поля вище очистяться.",
            StringKey.PROFILE_ERROR_STEPS to "Введіть ціле число кроків від 0 до 50 000 або залиште поле порожнім.",
            StringKey.PROFILE_ERROR_HOUSEHOLD to "Введіть від 0 до 480 хвилин або залиште поле порожнім.",
            StringKey.PLAN_MAINTENANCE to "Підтримання ваги (оцінка)",
            StringKey.PLAN_DEFICIT to "Фактичний дефіцит",
            StringKey.PLAN_TARGET to "Норма на день",
            StringKey.PLAN_ESTIMATE to "Це стартова оцінка. Звіряйте її зі зміною середньої ваги за 2–3 тижні. Обраний темп обмежується фактичним дефіцитом і нижньою межею калорій.",
            StringKey.PACE_LIMIT to "Дефіцит: не більше 20% витрат, з урахуванням нижньої межі калорій. Темп нижче — побажання, а не обіцянка.",
            StringKey.APP_NAME to "SlimTrack",
            StringKey.TAB_DIARY to "Щоденник",
            StringKey.TAB_WEIGHT to "Вага",
            StringKey.TAB_FOODS to "База",
            StringKey.TAB_INSIGHTS to "Поради",
            StringKey.TAB_PROFILE to "Профіль",
            StringKey.HEADER_PLANS_BTN to "Тарифи ⭐",
            StringKey.GUEST_ACCESS_FOREVER to "Безстроково 👑",
            StringKey.GUEST_ACCESS_DAYS_LEFT to "Залишилось дн.:",
            StringKey.GUEST_ACCESS_ACTIVE_CODE to "Активний • Код:",

            StringKey.SAVE to "Зберегти",
            StringKey.CANCEL to "Скасувати",
            StringKey.CLOSE to "Закрити",
            StringKey.DELETE to "Видалити",
            StringKey.EDIT to "Редагувати",
            StringKey.SEARCH to "Пошук",
            StringKey.ADD to "Додати",
            StringKey.COPY to "Копіювати",
            StringKey.SHARE to "Поділитися",
            StringKey.LOADING to "Завантаження...",
            StringKey.ERROR to "Помилка",
            StringKey.SUCCESS to "Успішно",
            StringKey.UNLOCKED to "Доступно",
            StringKey.LOCKED to "Заблоковано",
            StringKey.PRO_REQUIRED to "Потрібен Slim ($20)",
            StringKey.PREMIUM_REQUIRED to "Потрібен Premium ($50)",
            StringKey.ACTIVE_BADGE to "Активний",

            StringKey.LANGUAGE_TITLE to "Мова додатку",
            StringKey.SELECT_LANGUAGE to "Оберіть мову інтерфейсу",

            StringKey.SUBSCRIPTION_TITLE to "Тарифні плани",
            StringKey.CURRENT_PLAN to "Поточний тариф",
            StringKey.PLAN_FREE to "Free (Безкоштовний)",
            StringKey.PLAN_SLIM to "Slim ($20)",
            StringKey.PLAN_PREMIUM to "Premium ($50)",
            StringKey.PLAN_ADMIN to "Адміністратор (VIP)",
            StringKey.FREE_PRICE to "$0",
            StringKey.SLIM_PRICE to "$20",
            StringKey.PREMIUM_PRICE to "$50",
            StringKey.UPGRADE_PLAN to "Покращити тариф",
            StringKey.UNLOCK_ALL to "Відкрити всі функції",
            StringKey.PAYWALL_TITLE to "Всі можливості SlimTrack",
            StringKey.PAYWALL_SUBTITLE to "Оберіть відповідний тариф для швидкого та комфортного схуднення",
            StringKey.PAYWALL_FEATURE_LOCKED_FMT to "Функція доступна в тарифі",
            StringKey.PAYWALL_ALREADY_ACTIVE to "Тариф вже активний ✓",
            StringKey.PAYWALL_BUY_BTN_FMT to "Сплатити",
            StringKey.PAYWALL_SELECT_FREE to "Обрати безкоштовний тариф",
            StringKey.PAYWALL_HAVE_PROMO to "🎁 Є промокод або гостьове посилання?",
            StringKey.PAYWALL_SECURITY_NOTE to "🔒 Миттєва активація та електронна квитанція",
            StringKey.CHOOSE_PLAN to "Обрати тариф",
            StringKey.POPULAR_CHOICE to "Популярний 🔥",
            StringKey.BEST_VALUE to "VIP 👑",

            StringKey.ADMIN_PANEL_TITLE to "Панель адміністратора",
            StringKey.ADMIN_PANEL_SUBTITLE to "Керування доступом, оплатою та оновленнями",
            StringKey.ADMIN_FILE_IMPORT_TITLE to "Імпорт файлу конфігурації (.json)",
            StringKey.ADMIN_FILE_IMPORT_DESC to "Завантажте JSON-файл для оновлення тарифів або бази продуктів",
            StringKey.ADMIN_IMPORT_BUTTON to "Завантажити .JSON",
            StringKey.ADMIN_EXPORT_BUTTON to "Копіювати JSON",
            StringKey.ADMIN_RESET_BUTTON to "Скинути налаштування",
            StringKey.ADMIN_TIER_SIMULATOR to "Тестовий симулятор тарифу",
            StringKey.ADMIN_CONFIG_STATUS to "Статус конфігурації",
            StringKey.ADMIN_CONFIG_SOURCE_DEFAULT to "Вбудована",
            StringKey.ADMIN_CONFIG_SOURCE_FILE to "Із файлу",
            StringKey.ADMIN_CONFIG_UPDATED to "Оновлено",
            StringKey.ADMIN_VIEW_JSON to "Показати JSON",
            StringKey.ADMIN_SUCCESS_IMPORT to "Конфігурацію успішно застосовано!",
            StringKey.ADMIN_ERROR_IMPORT to "Помилка читання файлу конфігурації",
            StringKey.ADMIN_EXTRA_FOODS_IMPORTED to "Імпортовано продуктів",

            StringKey.DIARY_CALORIES_LEFT to "Залишилось",
            StringKey.DIARY_CONSUMED to "З'їдено",
            StringKey.DIARY_BURNED to "Спалено",
            StringKey.DIARY_TARGET to "Норма",
            StringKey.DIARY_PROTEIN to "Білки",
            StringKey.DIARY_FAT to "Жири",
            StringKey.DIARY_CARBS to "Вуглеводи",
            StringKey.DIARY_MEAL_BREAKFAST to "Сніданок",
            StringKey.DIARY_MEAL_LUNCH to "Обід",
            StringKey.DIARY_MEAL_DINNER to "Вечеря",
            StringKey.DIARY_MEAL_SNACKS to "Перекус",
            StringKey.DIARY_ADD_FOOD to "Додати",
            StringKey.DIARY_QUICK_CALORIES to "Швидкі ккал",
            StringKey.DIARY_COPY_YESTERDAY to "Вчорашнє",
            StringKey.DIARY_FASTING_TIMER to "Інтервальне голодування",
            StringKey.DIARY_FASTING_START to "Старт",
            StringKey.DIARY_FASTING_STOP to "Стоп",
            StringKey.DIARY_WATER_TRACKER to "Трекер води",
            StringKey.DIARY_ADD_EXERCISE to "Активність",
            StringKey.DIARY_BARCODE_SCANNER to "Штрих-код",

            StringKey.WEIGHT_TITLE to "Динаміка ваги",
            StringKey.WEIGHT_CURRENT to "Поточна",
            StringKey.WEIGHT_START to "Старт",
            StringKey.WEIGHT_TARGET to "Ціль",
            StringKey.WEIGHT_TREND to "Трендова вага",
            StringKey.WEIGHT_LOG_BUTTON to "Записати вагу",
            StringKey.WEIGHT_CHART_TITLE to "Графік зміни ваги",
            StringKey.WEIGHT_MEASUREMENTS_TITLE to "Виміри тіла",
            StringKey.WEIGHT_WEEKLY_DEFICIT_TITLE to "Тижневий дефіцит калорій",

            StringKey.FOOD_SEARCH_PLACEHOLDER to "Пошук продуктів за назвою...",
            StringKey.FOOD_ALL_CATEGORIES to "Всі категорії",
            StringKey.FOOD_CREATE_CUSTOM to "Свій продукт",

            StringKey.INSIGHTS_TITLE to "Аналітика та поради",
            StringKey.INSIGHTS_STREAKS to "Трекер звичок",
            StringKey.INSIGHTS_WEEKLY_CHART to "Баланс за 7 днів",
            StringKey.INSIGHTS_EXPORT_REPORT to "Експорт звіту для тренера / лікаря",

            StringKey.PROFILE_TITLE to "Профіль та параметри",
            StringKey.PROFILE_SUBTITLE to "Персональний розрахунок дефіциту калорій",
            StringKey.PROFILE_PERSONAL_DATA to "Особисті параметри тіла",
            StringKey.PROFILE_GENDER to "Стать",
            StringKey.GENDER_MALE to "Чоловіча",
            StringKey.GENDER_FEMALE to "Жіноча",
            StringKey.PROFILE_AGE to "Вік (років)",
            StringKey.PROFILE_HEIGHT to "Зріст (см)",
            StringKey.PROFILE_START_WEIGHT to "Початкова вага (кг)",
            StringKey.PROFILE_CURRENT_WEIGHT to "Поточна вага (кг)",
            StringKey.PROFILE_TARGET_WEIGHT to "Бажана вага (кг)",
            StringKey.PROFILE_ACTIVITY_LEVEL to "Спосіб життя та рухливість",
            StringKey.PROFILE_GOAL_PACE to "Бажаний темп схуднення",
            StringKey.PROFILE_MACROS_TITLE to "Денні норми харчування (БЖВ)",
            StringKey.PROFILE_CALORIES_GOAL to "Ціль калорій (ккал)",
            StringKey.PROFILE_PROTEIN_GOAL to "Білки (г)",
            StringKey.PROFILE_FAT_GOAL to "Жири (г)",
            StringKey.PROFILE_CARBS_GOAL to "Вуглеводи (г)",
            StringKey.PROFILE_WATER_GOAL to "Норма води (мл)",
            StringKey.PROFILE_AUTO_CALC to "Перерахувати норми",
            StringKey.PROFILE_SAVE_SETTINGS to "Зберегти параметри",
            StringKey.PROFILE_SAVED_SUCCESS to "Параметри збережено! ✓",
            StringKey.PROFILE_VALIDATION_FIX_ERRORS to "Перевірте виділені поля перед збереженням.",
            StringKey.PROFILE_ERROR_NAME_TOO_LONG to "Ім’я задовге.",
            StringKey.PROFILE_ERROR_ADULT_ONLY to "Автоматичні розрахунки SlimTrack доступні лише для користувачів 18+.",
            StringKey.PROFILE_ERROR_AGE_RANGE to "Введіть вік від 18 до 100 років.",
            StringKey.PROFILE_ERROR_HEIGHT_RANGE to "Введіть зріст від 120 до 230 см.",
            StringKey.PROFILE_ERROR_WEIGHT_RANGE to "Введіть вагу від 30 до 250 кг.",
            StringKey.PROFILE_ERROR_TARGET_ABOVE_CURRENT to "Для режиму зниження ваги ціль не має бути вищою за поточну вагу.",
            StringKey.PROFILE_ERROR_TARGET_TOO_LOW to "Ця ціль виходить за підтримуваний діапазон для самостійного зниження ваги.",
            StringKey.PROFILE_ERROR_CALORIES_RANGE to "Введіть підтримувану денну ціль калорій.",
            StringKey.PROFILE_ERROR_MACRO_RANGE to "Введіть підтримуване значення макронутрієнта.",
            StringKey.PROFILE_ERROR_WATER_RANGE to "Введіть ціль води від 500 до 6000 мл.",
            StringKey.PROFILE_SAFE_CALC_NOTE to "Авторозрахунок працює лише для дорослих і обмежує надмірний дефіцит.",

            StringKey.ACTIVITY_SEDENTARY to "Сидячий спосіб життя",
            StringKey.ACTIVITY_SEDENTARY_DESC to "Менше 3000 кроків, більшу частину дня сидячи",
            StringKey.ACTIVITY_LIGHT to "Легка активність",
            StringKey.ACTIVITY_LIGHT_DESC to "3000–6000 кроків, прогулянки, звичайні домашні справи",
            StringKey.ACTIVITY_MODERATE to "Помірна активність",
            StringKey.ACTIVITY_MODERATE_DESC to "6000–10000 кроків, багато часу на ногах, активні справи",
            StringKey.ACTIVITY_ACTIVE to "Висока активність",
            StringKey.ACTIVITY_ACTIVE_DESC to "Понад 10000 кроків, фізична робота або регулярний спорт",

            StringKey.PACE_EASY to "М'який (-0.25 кг/тижд)",
            StringKey.PACE_RECOMMENDED to "Оптимальний (-0.5 кг/тижд)",
            StringKey.PACE_FAST to "Швидкий (-0.75 кг/тижд)",
            StringKey.PACE_MAX to "Максимум (-1.0 кг/тижд)",

            StringKey.ANALYSIS_RESULT_TITLE to "Аналіз параметрів тіла",
            StringKey.ANALYSIS_RESULT_SUBTITLE to "Розрахунок ІМТ, витрат калорій та терміну цілі",
            StringKey.ANALYSIS_BMI_LABEL to "Індекс маси тіла (ІМТ)",
            StringKey.BMI_UNDERWEIGHT to "Дефіцит маси",
            StringKey.BMI_NORMAL to "Норма ваги ✓",
            StringKey.BMI_OVERWEIGHT to "Надлишкова вага",
            StringKey.BMI_OBESITY to "Ожиріння",
            StringKey.ANALYSIS_IDEAL_WEIGHT_RANGE to "Здорова вага",
            StringKey.ANALYSIS_DIFF_TO_GOAL to "До цілі",
            StringKey.ANALYSIS_TO_LOSE to "Скинути",
            StringKey.ANALYSIS_TO_GAIN to "Набрати",
            StringKey.ANALYSIS_TDEE_LABEL to "Добові витрати (TDEE)",
            StringKey.ANALYSIS_BMR_LABEL to "Базовий обмін (BMR)",
            StringKey.ANALYSIS_WEEKS_ESTIMATE to "Прогноз терміну",
            StringKey.ANALYSIS_HELP_BUTTON_TEXT to "Розбір ❔",

            StringKey.EXPLAINER_DIALOG_TITLE to "Розбір ваших параметрів",
            StringKey.EXPLAINER_SECTION_WHAT_IT_MEANS to "1. Що означають показники",
            StringKey.EXPLAINER_SECTION_PROBLEMS to "2. На що це впливає",
            StringKey.EXPLAINER_SECTION_HOW_TO_IMPROVE to "3. Як покращити результат",
            StringKey.EXPLAINER_SECTION_HOW_APP_HELPS to "4. Як допомагає SlimTrack",
            StringKey.EXPLAINER_SECTION_MOTIVATION to "5. Мотивація",
            StringKey.EXPLAINER_GOT_IT_BUTTON to "Зрозуміло, до цілі! ✓",

            StringKey.LEAD_MAGNET_TITLE to "Відкрийте 100% можливостей PRO",
            StringKey.LEAD_MAGNET_SUBTITLE to "Сканер штрих-кодів, аналіз тіла, фастинг та авто-БЖВ",
            StringKey.LEAD_MAGNET_PERK_1 to "Сканер штрих-кодів та швидке додавання ккал",
            StringKey.LEAD_MAGNET_PERK_2 to "Медичний розбір ІМТ та прогноз у тижнях",
            StringKey.LEAD_MAGNET_PERK_3 to "Таймер голодування 16:8 та експорт звітів",
            StringKey.LEAD_MAGNET_BUTTON to "Тарифи та знижки ⭐",

            StringKey.DIARY_WATER_HINT to "Прискорює обмін речовин",
            StringKey.DIARY_EXERCISE_TITLE to "Активність та спорт",
            StringKey.DIARY_EXERCISE_HINT to "Збільшує ліміт калорій",
            StringKey.DIARY_NO_ITEMS_YET to "Немає записів",
            StringKey.DIARY_HABITS_TITLE to "Трекер звичок",
            StringKey.DIARY_HABITS_DESC to "Щоденний чек-лист дисципліни",
            StringKey.DIARY_OPEN_HABITS to "Відкрити",
            StringKey.DIARY_CALORIES_OVER to "перевищено",
            StringKey.DIARY_CURRENT_DEFICIT to "Дефіцит:",
            StringKey.DIARY_CURRENT_SURPLUS to "Профіцит:",
            StringKey.DIARY_MACROS_TITLE to "Макронутрієнти (БЖВ)",

            StringKey.ADMIN_TAB_UPDATE to "Оновлення (.APK)",
            StringKey.ADMIN_INSTALL_APK_BUTTON to "Встановити оновлення (.apk)",
            StringKey.ADMIN_INSTALL_APK_DESC to "Оберіть завантажений APK для оновлення поверх без втрати даних",
            StringKey.ADMIN_APK_NO_DELETE_NOTE to "💡 Видаляти додаток не потрібно! Android оновить його поверх і збереже всі записи.",

            StringKey.UNIT_KCAL to "ккал",
            StringKey.UNIT_GRAM to "г",
            StringKey.UNIT_ML to "мл",
            StringKey.UNIT_CM to "см",
            StringKey.UNIT_MIN to "хв",

            StringKey.INSIGHTS_SUBTITLE to "Науковий підхід до зниження ваги",
            StringKey.INSIGHTS_FASTING_TITLE to "Інтервальне голодування 16:8",
            StringKey.INSIGHTS_FASTING_ACTIVE to "Фаза активного голодування",
            StringKey.INSIGHTS_FASTING_STOPPED to "Таймер зупинено",
            StringKey.INSIGHTS_FASTING_BTN_START to "Старт",
            StringKey.INSIGHTS_FASTING_BTN_STOP to "Стоп",
            StringKey.INSIGHTS_RULES_TITLE to "5 правил сталого схуднення",

            StringKey.WEIGHT_HEADER_SUBTITLE to "Контроль маси тіла та об'ємів",
            StringKey.WEIGHT_ADD_BUTTON to "Записати вагу",
            StringKey.WEIGHT_LOST_LABEL to "Скинуто",
            StringKey.WEIGHT_REMAINING_LABEL to "До цілі",
            StringKey.WEIGHT_WEEKS_LABEL to "Тижнів",
            StringKey.WEIGHT_PROGRESS_TO_GOAL to "Прогрес до цілі",
            StringKey.WEIGHT_FORECAST_TEXT to "Прогноз до цілі: ≈",
            StringKey.WEIGHT_HISTORY_TITLE to "Історія зважувань",
            StringKey.WEIGHT_EMPTY_HISTORY to "Немає записів. Натисніть «Записати вагу»!",
            StringKey.WEIGHT_UNIT_KG to "кг",

            StringKey.CHART_SUBTITLE to "Динаміка маси тіла",
            StringKey.CHART_FILTER_7D to "7 дн.",
            StringKey.CHART_FILTER_30D to "30 дн.",
            StringKey.CHART_FILTER_ALL to "Всі",
            StringKey.CHART_EMPTY to "Немає даних. Запишіть свою вагу!",
            StringKey.CHART_ACTUAL_WEIGHT to "Вага",
            StringKey.TREND_CARD_TITLE to "Трендова вага",
            StringKey.TREND_CARD_SUBTITLE to "Згладжена лінія без затримки води",
            StringKey.TREND_LABEL_TREND to "Тренд",
            StringKey.TREND_LABEL_SCALE to "На вагах",
            StringKey.TREND_INFO_GOOD to "Затримка води приховує прогрес — спалювання жиру йде за планом!",
            StringKey.TREND_INFO_NORMAL to "Вага стабільно знижується завдяки дефіциту калорій.",
            StringKey.WEEKLY_DEFICIT_SUBTITLE to "Сумарний баланс за 7 днів",
            StringKey.WEEKLY_FAT_BURN_FORECAST to "Спалювання жиру за тиждень: ≈",
            StringKey.WEEKLY_FAT_BURN_NOTE to "7 700 ккал дефіциту = 1 кг жирової тканини",
            StringKey.MEAS_TITLE to "Виміри об'ємів тіла",
            StringKey.MEAS_SUBTITLE to "Об'єми в сантиметрах",
            StringKey.MEAS_WAIST to "Талія",
            StringKey.MEAS_HIPS to "Стегна",
            StringKey.MEAS_CHEST to "Груди",
            StringKey.MEAS_BICEP to "Біцепс",
            StringKey.MEAS_THIGH to "Стегно",
            StringKey.MEAS_ADD_BTN to "Записати",
            StringKey.MEAS_EMPTY_HINT to "Запишіть об'єми талії та стегон для контролю прогресу",
            StringKey.HABIT_STREAK_BADGE to "дн. поспіль",
            StringKey.HABIT_1 to "Випити денну норму води",
            StringKey.HABIT_2 to "Виконати норму за білком",
            StringKey.HABIT_3 to "Вкластися в дефіцит калорій",
            StringKey.HABIT_4 to "Мінімум 8 000 кроків / спорт",
            StringKey.HABIT_5 to "Без їжі за 3 години до сну",
            StringKey.EXPORT_SUBTITLE to "Зведення прогресу за вагою та КБЖВ",
            StringKey.EXPORT_SHARE_BTN to "Поділитися",
            StringKey.EXPORT_COPY_BTN to "Копіювати",
            StringKey.EXPORT_COPIED_TOAST to "Звіт скопійовано!",

            StringKey.DIALOG_ADD_FOOD_TITLE to "Додати у",
            StringKey.DIALOG_GRAMS_LABEL to "Вага порції (г)",
            StringKey.DIALOG_EMPTY_FOODS to "Продукти не знайдено",
            StringKey.DIALOG_ADD_EXERCISE_TITLE to "Додати активність",
            StringKey.DIALOG_EXERCISE_NAME_LABEL to "Назва тренування",
            StringKey.DIALOG_EXERCISE_MINUTES_LABEL to "Тривалість (хв)",
            StringKey.DIALOG_EXERCISE_KCAL_LABEL to "Спалено (ккал)",
            StringKey.DIALOG_ADD_WEIGHT_TITLE to "Записати вагу",
            StringKey.DIALOG_WEIGHT_INPUT_LABEL to "Ваша вага (кг)",
            StringKey.DIALOG_NOTE_LABEL to "Нотатка (необов'язково)",
            StringKey.DIALOG_QUICK_CALORIES_TITLE to "Швидкі калорії",
            StringKey.DIALOG_QUICK_CALORIES_SUBTITLE to "Миттєве введення калорій без пошуку страви",
            StringKey.DIALOG_BARCODE_TITLE to "Сканер штрих-коду",
            StringKey.DIALOG_BARCODE_SUBTITLE to "Наведіть камеру на штрих-код EAN/UPC або введіть код вручну",
            StringKey.DIALOG_BARCODE_INPUT_LABEL to "Штрих-код",
            StringKey.DIALOG_BARCODE_CAMERA_HINT to "Розмістіть штрих-код у рамці",
            StringKey.DIALOG_BARCODE_CAMERA_PERMISSION to "Для сканування потрібен доступ до камери. Штрих-коди обробляються на пристрої.",
            StringKey.DIALOG_BARCODE_GRANT_PERMISSION to "Дозволити камеру",
            StringKey.DIALOG_BARCODE_NOT_FOUND to "Товар не знайдено в онлайн-базі. Можна додати дані з упаковки.",
            StringKey.DIALOG_BARCODE_NETWORK_ERROR to "Немає з’єднання з онлайн-базою. Перевірте інтернет і повторіть пошук.",
            StringKey.DIALOG_BARCODE_SERVICE_ERROR to "Онлайн-база тимчасово недоступна. Повторіть пошук пізніше.",
            StringKey.DIALOG_BARCODE_INCOMPLETE to "Товар знайдено, але дані калорій і БЖВ неповні. Введіть значення з упаковки на 100 г.",
            StringKey.DIALOG_BARCODE_INVALID_CODE to "Введіть штрих-код із 8, 12, 13 або 14 цифр.",
            StringKey.DIALOG_BARCODE_SOURCE to "Онлайн-каталог: Open Food Facts • ODbL",
            StringKey.DIALOG_BARCODE_ADD_MANUALLY to "Додати дані з упаковки",
            StringKey.DIALOG_BARCODE_SAVE_ERROR to "Не вдалося зберегти продукт. Спробуйте ще раз.",
            StringKey.DIALOG_BARCODE_SCAN_AGAIN to "Сканувати знову",
            StringKey.DIALOG_BARCODE_CAMERA_ERROR to "Не вдалося запустити камеру. Можна ввести код вручну.",
            StringKey.DIALOG_BARCODE_MANUAL_HINT to "Або введіть код вручну",
            StringKey.DIALOG_BARCODE_SEARCHING to "Шукаємо продукт онлайн…",
            StringKey.DIALOG_PROMO_TITLE to "Промокод або посилання",
            StringKey.DIALOG_PROMO_SUBTITLE to "Введіть код доступу для активації Slim / Premium",
            StringKey.DIALOG_PROMO_INPUT_LABEL to "Промокод або посилання",
            StringKey.DIALOG_PROMO_ACTIVATE_BTN to "Активувати доступ ✓",
            StringKey.DIALOG_PAYMENT_TITLE to "Оплата підписки",
            StringKey.DIALOG_PAYMENT_SELECT_METHOD to "Оберіть спосіб оплати:",
            StringKey.DIALOG_PAYMENT_REQUISITES_HEADER to "Реквізити для переказу:",
            StringKey.DIALOG_PAYMENT_AMOUNT_TO_PAY to "Сума до сплати:",
            StringKey.DIALOG_PAYMENT_NOTE_LABEL to "Примітка / Прізвище / TXID",
            StringKey.DIALOG_PAYMENT_CONFIRM_BTN to "Я сплатив(ла) — Створити заявку на перевірку",
            StringKey.DIALOG_PAYMENT_SECURITY_FOOTER to "🔒 Доступ активується лише після підтвердження оплати",
            StringKey.DIALOG_RECEIPT_TITLE to "Заявку на перевірку створено",
            StringKey.DIALOG_RECEIPT_SUBTITLE_FMT to "Надішліть заявку в підтримку. Доступ ще не активовано",
            StringKey.DIALOG_RECEIPT_PENDING_BADGE to "НА ПЕРЕВІРЦІ",
            StringKey.DIALOG_RECEIPT_ORDER_ID to "Номер замовлення:",
            StringKey.DIALOG_RECEIPT_DATE to "Дата і час:",
            StringKey.DIALOG_RECEIPT_METHOD to "Спосіб оплати:",
            StringKey.DIALOG_RECEIPT_TOTAL to "Разом:",
            StringKey.DIALOG_RECEIPT_DONE_BTN to "Готово",
            StringKey.ADMIN_REQUISITES_HEADER to "⚙️ Налаштування реквізитів оплати",
            StringKey.ADMIN_REQUISITES_SUBTITLE to "Вкажіть ваші картки та гаманці для прийому оплати",
            StringKey.ADMIN_REQUISITES_ENABLED_METHODS to "Увімкнені способи оплати:",
            StringKey.ADMIN_REQUISITES_SAVE_BTN to "Зберегти реквізити ✓",
            StringKey.ADMIN_REQUISITES_SAVED_TOAST to "Реквізити збережено!",

            StringKey.FOODS_TITLE to "Каталог",
            StringKey.CATALOG_DISHES to "Страви",
            StringKey.CATALOG_PRODUCTS to "Продукти",
            StringKey.CATALOG_ALL_CUISINES to "Усі кухні",
            StringKey.CATALOG_EMPTY_HINT to "Нічого не знайдено. Спробуйте іншу назву або скиньте фільтри. Свою страву можна додати у вкладці «Мій рецепт».",
            StringKey.DISH_ESTIMATE_NOTE to "Орієнтовні КБЖВ стандартного рецепта. Склад, олія та спосіб приготування змінюють результат.",
            StringKey.RECIPE_CALCULATED_NOTE to "Розрахунок за вашими інгредієнтами та готовою вагою. Точність залежить від введених даних.",
            StringKey.RECIPE_MY_RECIPE to "Мій рецепт",
            StringKey.RECIPE_BUILDER_HINT to "Додайте інгредієнти та їхню вагу. Обирайте правильний стан: сирий або приготовлений продукт. Враховуйте спожиту олію. Потім зважте всю готову страву без посуду, включно з водою в супі.",
            StringKey.RECIPE_NAME to "Назва страви",
            StringKey.RECIPE_ADD_INGREDIENT to "Знайти інгредієнт",
            StringKey.RECIPE_INGREDIENT_NOT_FOUND to "Інгредієнт не знайдено. Його можна спочатку додати у вкладці «Свій продукт» за даними з упаковки.",
            StringKey.RECIPE_FINISHED_WEIGHT to "Вага всієї готової страви, г",
            StringKey.RECIPE_VALIDATION_HINT to "Вкажіть додатну вагу кожного інгредієнта й усієї готової страви. Перевірте підсумкову вагу.",
            StringKey.RECIPE_SAVE to "Зберегти страву",
            StringKey.FOODS_SUBTITLE to "Калорійність та БЖВ на 100 г",
            StringKey.FOODS_SEARCH_HINT to "Знайти страву або продукт…",
            StringKey.FOODS_CAT_ALL to "Всі",
            StringKey.FOODS_CAT_MEAT to "М'ясо та птиця",
            StringKey.FOODS_CAT_FISH to "Риба",
            StringKey.FOODS_CAT_GRAINS to "Крупи",
            StringKey.FOODS_CAT_DAIRY to "Молочні продукти",
            StringKey.FOODS_CAT_VEG to "Овочі",
            StringKey.FOODS_CAT_FRUITS to "Фрукти",
            StringKey.FOODS_CAT_SNACKS to "Горіхи та снеки",
            StringKey.FOODS_CREATE_BTN to "Свій продукт",
            StringKey.FOOD_CUSTOM_BADGE to "Свій",
            StringKey.FOOD_NAME_LABEL to "Назва продукту",
            StringKey.FOOD_CATEGORY_LABEL to "Категорія",
            StringKey.FOOD_KCAL_100G_LABEL to "Ккал на 100 г",

            StringKey.ADMIN_TAB_FAMILY to "Сім'я & Гості",
            StringKey.ADMIN_TAB_REQUISITES to "Реквізити",
            StringKey.ADMIN_TAB_TIERS_JSON to "Тарифи & JSON",
            StringKey.ADMIN_FAMILY_TITLE to "Сімейний VIP доступ НАЗАВЖДИ 👑",
            StringKey.ADMIN_FAMILY_DESC to "Безстроковий доступ до всіх функцій для близьких",
            StringKey.ADMIN_FAMILY_SHARE_BTN to "Надіслати сім'ї",
            StringKey.ADMIN_COPY_CODE to "Код",
            StringKey.ADMIN_QUICK_GUEST_LINKS to "Швидкі гостьові інвайти:",
            StringKey.ADMIN_GUEST_SLIM_7D to "Гостьовий Slim (7 днів)",
            StringKey.ADMIN_GUEST_PREMIUM_7D to "Пробний Premium (7 днів)",
            StringKey.ADMIN_GUEST_PREMIUM_30D to "Повний Premium (30 днів)",
            StringKey.ADMIN_SHARE_ACTION to "Поділитися",
            StringKey.ADMIN_CUSTOM_INVITE_CREATE to "Створити свій промокод",
            StringKey.ADMIN_CUSTOM_INVITE_HIDE to "Сховати конструктор",
            StringKey.ADMIN_ACTIVE_INVITES_LIST to "Активні коди",
            StringKey.ADMIN_DURATION_FOREVER to "Безстроково 👑",

            StringKey.TIP_1_TITLE to "1. Дефіцит калорій — основа",
            StringKey.TIP_1_SUBTITLE to "Закон енергетичного балансу",
            StringKey.TIP_1_CONTENT to "Витрачайте на 400–500 ккал більше, ніж споживаєте. Це забезпечує стабільне спалювання жиру без голодувань та зривів.",
            StringKey.TIP_2_TITLE to "2. Білок захищає м'язи",
            StringKey.TIP_2_SUBTITLE to "Норма: 1.6 – 2.0 г на 1 кг ваги",
            StringKey.TIP_2_CONTENT to "Білкова їжа (птиця, риба, яйця, кисломолочний сир) зберігає м'язи при схудненні та надовго тамує голод.",
            StringKey.TIP_3_TITLE to "3. Вода та коливання ваги",
            StringKey.TIP_3_SUBTITLE to "Чому вага змінюється за добу",
            StringKey.TIP_3_CONTENT to "Сіль та вуглеводи тимчасово затримують воду. Пийте 30 мл води на 1 кг ваги для зняття набряків і високого метаболізму.",
            StringKey.TIP_4_TITLE to "4. Побутова активність (NEAT)",
            StringKey.TIP_4_SUBTITLE to "Кроки важливіші за рідкісні тренування",
            StringKey.TIP_4_CONTENT to "8 000 – 10 000 кроків на день спалюють до 400 ккал без втоми нервової системи та зайвого апетиту.",
            StringKey.TIP_5_TITLE to "5. Як подолати плато ваги",
            StringKey.TIP_5_SUBTITLE to "Якщо вага стоїть 2 тижні",
            StringKey.TIP_5_CONTENT to "Враховуйте олії та соуси, стежте за вимірами талії в сантиметрах і спіть не менше 7–8 годин.",

            StringKey.PROFILE_SMART_CALC_TITLE to "Авто-розрахунок за формулою",
            StringKey.PROFILE_SMART_CALC_DESC to "Формула Міффліна-Сан Жеора під вашу вагу",
            StringKey.PROFILE_RECALC_BTN to "Розрахувати",
            StringKey.PROFILE_CHANGE_PLAN_BTN to "Тарифи",
            StringKey.PROFILE_PAYMENT_BTN to "Оплата",
            StringKey.PROFILE_PROMO_BTN to "Промокод",
            StringKey.LANGUAGE_DROPDOWN_DESC to "Список мов",
            StringKey.PROMO_ACTIVATION_HINT to "Введіть промокод або гостьове посилання для активації Slim або Premium",
            StringKey.PROMO_INVALID_CODE to "Невірний код",
            StringKey.ADMIN_PROMO_CODE_LABEL to "Промокод (VIP-FRIEND)",
            StringKey.ADMIN_SOURCE_LABEL to "Джерело:",
            StringKey.ADMIN_VERSION_LABEL to "Версія:",
            StringKey.ADMIN_PAYMENT_SETTINGS_TITLE to "⚙️ Налаштування платежів",
            StringKey.ADMIN_PAYMENT_SETTINGS_DESC to "Вкажіть реквізити для ручної оплати підписки. Доступ активується лише після підтвердженої перевірки платежу.",
            StringKey.ADMIN_PAYMENT_METHODS_ENABLED to "Увімкнені способи оплати:",
            StringKey.ADMIN_PAYMENT_METHODS_HINT to "Вимкніть непотрібні способи оплати, щоб показувати користувачу лише актуальні варіанти.",
            StringKey.ADMIN_MONOBANK_SECTION to "Monobank (Україна 🇺🇦)",
            StringKey.ADMIN_MONOBANK_CARD_LABEL to "Номер картки Monobank",
            StringKey.ADMIN_MONOBANK_JAR_LABEL to "Посилання на банку Monobank",
            StringKey.ADMIN_PAYPAL_SECTION to "PayPal (міжнародні платежі 🌐)",
            StringKey.ADMIN_PAYPAL_EMAIL_LABEL to "Email акаунта PayPal",
            StringKey.ADMIN_PAYPAL_ME_LABEL to "Посилання PayPal.Me",
            StringKey.ADMIN_CRYPTO_SECTION to "Криптовалюта (USDT та Bitcoin)",
            StringKey.ADMIN_USDT_ADDRESS_LABEL to "Адреса гаманця USDT",
            StringKey.ADMIN_USDT_NETWORK_LABEL to "Мережа USDT (TRC-20, BEP-20, ERC-20)",
            StringKey.ADMIN_BTC_ADDRESS_LABEL to "Адреса гаманця Bitcoin (BTC)",
            StringKey.ADMIN_BANK_SECTION to "Банківська картка та IBAN",
            StringKey.ADMIN_CARD_NUMBER_LABEL to "Номер картки Visa / MasterCard",
            StringKey.ADMIN_CARD_HOLDER_LABEL to "Ім'я власника картки латиницею",
            StringKey.ADMIN_IBAN_LABEL to "Розрахунковий рахунок IBAN",
            StringKey.ADMIN_SUPPORT_CONTACTS to "Контакти підтримки для платежів",
            StringKey.ADMIN_TELEGRAM_LABEL to "Telegram підтримки",
            StringKey.ADMIN_SUPPORT_EMAIL_LABEL to "Email підтримки",
            StringKey.ADMIN_PAYMENT_DETAILS_SAVED to "Реквізити оплати збережено",
            StringKey.ADMIN_SAVE_PAYMENT_DETAILS to "Зберегти реквізити оплати ✓",
            StringKey.SHARE_LABEL to "Поділитися",
            StringKey.COPY_LABEL to "Копіювати"
        ),

        AppLanguage.EN to mapOf(
            StringKey.STEPS_LABEL to "Average daily steps",
            StringKey.HOUSEHOLD_LABEL to "Active housework, minutes per day",
            StringKey.LIFESTYLE_HELP to "Enter your average over a typical week. Steps include walking at home and work. Housework means cleaning, gardening and other work while moving. Leave blank if unknown.",
            StringKey.LIFESTYLE_AUTO to "Estimate from steps and housework without counting the same movement twice. Select a level manually for physical work or sport; the fields above will be cleared.",
            StringKey.PROFILE_ERROR_STEPS to "Enter a whole number from 0 to 50,000 steps or leave blank.",
            StringKey.PROFILE_ERROR_HOUSEHOLD to "Enter 0 to 480 whole minutes or leave blank.",
            StringKey.PLAN_MAINTENANCE to "Estimated maintenance",
            StringKey.PLAN_DEFICIT to "Applied deficit",
            StringKey.PLAN_TARGET to "Daily target",
            StringKey.PLAN_ESTIMATE to "This is a starting estimate. Review your average weight trend over 2–3 weeks. The selected pace is limited by the applied deficit and calorie floor.",
            StringKey.PACE_LIMIT to "Deficit is capped at 20% of expenditure and the calorie floor. Pace below is a preference, not a promise.",
            StringKey.APP_NAME to "SlimTrack",
            StringKey.TAB_DIARY to "Diary",
            StringKey.TAB_WEIGHT to "Weight",
            StringKey.TAB_FOODS to "Foods",
            StringKey.TAB_INSIGHTS to "Tips",
            StringKey.TAB_PROFILE to "Profile",
            StringKey.HEADER_PLANS_BTN to "Plans ⭐",
            StringKey.GUEST_ACCESS_FOREVER to "Lifetime 👑",
            StringKey.GUEST_ACCESS_DAYS_LEFT to "Days left:",
            StringKey.GUEST_ACCESS_ACTIVE_CODE to "Active • Code:",

            StringKey.SAVE to "Save",
            StringKey.CANCEL to "Cancel",
            StringKey.CLOSE to "Close",
            StringKey.DELETE to "Delete",
            StringKey.EDIT to "Edit",
            StringKey.SEARCH to "Search",
            StringKey.ADD to "Add",
            StringKey.COPY to "Copy",
            StringKey.SHARE to "Share",
            StringKey.LOADING to "Loading...",
            StringKey.ERROR to "Error",
            StringKey.SUCCESS to "Success",
            StringKey.UNLOCKED to "Unlocked",
            StringKey.LOCKED to "Locked",
            StringKey.PRO_REQUIRED to "Slim ($20) Required",
            StringKey.PREMIUM_REQUIRED to "Premium ($50) Required",
            StringKey.ACTIVE_BADGE to "Active",

            StringKey.LANGUAGE_TITLE to "App Language",
            StringKey.SELECT_LANGUAGE to "Select interface language",

            StringKey.SUBSCRIPTION_TITLE to "Subscription Plans",
            StringKey.CURRENT_PLAN to "Current Plan",
            StringKey.PLAN_FREE to "Free Plan",
            StringKey.PLAN_SLIM to "Slim ($20)",
            StringKey.PLAN_PREMIUM to "Premium ($50)",
            StringKey.PLAN_ADMIN to "Admin (VIP)",
            StringKey.FREE_PRICE to "$0",
            StringKey.SLIM_PRICE to "$20",
            StringKey.PREMIUM_PRICE to "$50",
            StringKey.UPGRADE_PLAN to "Upgrade Plan",
            StringKey.UNLOCK_ALL to "Unlock All Features",
            StringKey.PAYWALL_TITLE to "Unlock Full SlimTrack Power",
            StringKey.PAYWALL_SUBTITLE to "Choose the right plan for faster, sustainable weight loss",
            StringKey.PAYWALL_FEATURE_LOCKED_FMT to "Feature available in plan",
            StringKey.PAYWALL_ALREADY_ACTIVE to "Plan already active ✓",
            StringKey.PAYWALL_BUY_BTN_FMT to "Pay for",
            StringKey.PAYWALL_SELECT_FREE to "Select Free Plan",
            StringKey.PAYWALL_HAVE_PROMO to "🎁 Have a promo code or invite link?",
            StringKey.PAYWALL_SECURITY_NOTE to "🔒 Instant activation & digital receipt",
            StringKey.CHOOSE_PLAN to "Select Plan",
            StringKey.POPULAR_CHOICE to "Popular 🔥",
            StringKey.BEST_VALUE to "VIP 👑",

            StringKey.ADMIN_PANEL_TITLE to "Administrator Panel",
            StringKey.ADMIN_PANEL_SUBTITLE to "Manage invites, payments & app updates",
            StringKey.ADMIN_FILE_IMPORT_TITLE to "Import Config File (.json)",
            StringKey.ADMIN_FILE_IMPORT_DESC to "Upload a JSON file to update pricing, features or food database",
            StringKey.ADMIN_IMPORT_BUTTON to "Upload .JSON",
            StringKey.ADMIN_EXPORT_BUTTON to "Copy JSON",
            StringKey.ADMIN_RESET_BUTTON to "Reset Defaults",
            StringKey.ADMIN_TIER_SIMULATOR to "Tier Simulator",
            StringKey.ADMIN_CONFIG_STATUS to "Configuration Status",
            StringKey.ADMIN_CONFIG_SOURCE_DEFAULT to "Built-in",
            StringKey.ADMIN_CONFIG_SOURCE_FILE to "From file",
            StringKey.ADMIN_CONFIG_UPDATED to "Updated",
            StringKey.ADMIN_VIEW_JSON to "Show JSON",
            StringKey.ADMIN_SUCCESS_IMPORT to "Configuration applied!",
            StringKey.ADMIN_ERROR_IMPORT to "Failed to read config file",
            StringKey.ADMIN_EXTRA_FOODS_IMPORTED to "Imported foods",

            StringKey.DIARY_CALORIES_LEFT to "Left",
            StringKey.DIARY_CONSUMED to "Eaten",
            StringKey.DIARY_BURNED to "Burned",
            StringKey.DIARY_TARGET to "Goal",
            StringKey.DIARY_PROTEIN to "Protein",
            StringKey.DIARY_FAT to "Fat",
            StringKey.DIARY_CARBS to "Carbs",
            StringKey.DIARY_MEAL_BREAKFAST to "Breakfast",
            StringKey.DIARY_MEAL_LUNCH to "Lunch",
            StringKey.DIARY_MEAL_DINNER to "Dinner",
            StringKey.DIARY_MEAL_SNACKS to "Snacks",
            StringKey.DIARY_ADD_FOOD to "Add",
            StringKey.DIARY_QUICK_CALORIES to "Quick kcal",
            StringKey.DIARY_COPY_YESTERDAY to "Yesterday",
            StringKey.DIARY_FASTING_TIMER to "Intermittent Fasting",
            StringKey.DIARY_FASTING_START to "Start",
            StringKey.DIARY_FASTING_STOP to "Stop",
            StringKey.DIARY_WATER_TRACKER to "Water Tracker",
            StringKey.DIARY_ADD_EXERCISE to "Activity",
            StringKey.DIARY_BARCODE_SCANNER to "Barcode",

            StringKey.WEIGHT_TITLE to "Weight Progress",
            StringKey.WEIGHT_CURRENT to "Current",
            StringKey.WEIGHT_START to "Start",
            StringKey.WEIGHT_TARGET to "Goal",
            StringKey.WEIGHT_TREND to "Trend Weight",
            StringKey.WEIGHT_LOG_BUTTON to "Log Weight",
            StringKey.WEIGHT_CHART_TITLE to "Weight History Chart",
            StringKey.WEIGHT_MEASUREMENTS_TITLE to "Body Measurements",
            StringKey.WEIGHT_WEEKLY_DEFICIT_TITLE to "Weekly Calorie Deficit",

            StringKey.FOOD_SEARCH_PLACEHOLDER to "Search food items...",
            StringKey.FOOD_ALL_CATEGORIES to "All Categories",
            StringKey.FOOD_CREATE_CUSTOM to "My product",

            StringKey.INSIGHTS_TITLE to "Insights & Tips",
            StringKey.INSIGHTS_STREAKS to "Habit Streaks",
            StringKey.INSIGHTS_WEEKLY_CHART to "7-Day Calorie Balance",
            StringKey.INSIGHTS_EXPORT_REPORT to "Export Report for Coach/Doctor",

            StringKey.PROFILE_TITLE to "Profile & Goals",
            StringKey.PROFILE_SUBTITLE to "Personal Calorie Deficit Calculator",
            StringKey.PROFILE_PERSONAL_DATA to "Personal Body Parameters",
            StringKey.PROFILE_GENDER to "Gender",
            StringKey.GENDER_MALE to "Male",
            StringKey.GENDER_FEMALE to "Female",
            StringKey.PROFILE_AGE to "Age (years)",
            StringKey.PROFILE_HEIGHT to "Height (cm)",
            StringKey.PROFILE_START_WEIGHT to "Start Weight (kg)",
            StringKey.PROFILE_CURRENT_WEIGHT to "Current Weight (kg)",
            StringKey.PROFILE_TARGET_WEIGHT to "Goal Weight (kg)",
            StringKey.PROFILE_ACTIVITY_LEVEL to "Lifestyle & Activity Level",
            StringKey.PROFILE_GOAL_PACE to "Weight Loss Pace",
            StringKey.PROFILE_MACROS_TITLE to "Daily Nutrition Goals (Macros)",
            StringKey.PROFILE_CALORIES_GOAL to "Calorie Goal (kcal)",
            StringKey.PROFILE_PROTEIN_GOAL to "Protein (g)",
            StringKey.PROFILE_FAT_GOAL to "Fat (g)",
            StringKey.PROFILE_CARBS_GOAL to "Carbs (g)",
            StringKey.PROFILE_WATER_GOAL to "Water Goal (ml)",
            StringKey.PROFILE_AUTO_CALC to "Recalculate Goals",
            StringKey.PROFILE_SAVE_SETTINGS to "Save Settings",
            StringKey.PROFILE_SAVED_SUCCESS to "Settings saved! ✓",
            StringKey.PROFILE_VALIDATION_FIX_ERRORS to "Check the highlighted fields before saving.",
            StringKey.PROFILE_ERROR_NAME_TOO_LONG to "Name is too long.",
            StringKey.PROFILE_ERROR_ADULT_ONLY to "SlimTrack automatic calculations are available only for users age 18+.",
            StringKey.PROFILE_ERROR_AGE_RANGE to "Enter an age from 18 to 100.",
            StringKey.PROFILE_ERROR_HEIGHT_RANGE to "Enter a height from 120 to 230 cm.",
            StringKey.PROFILE_ERROR_WEIGHT_RANGE to "Enter a weight from 30 to 250 kg.",
            StringKey.PROFILE_ERROR_TARGET_ABOVE_CURRENT to "In weight-loss mode, the goal cannot be above the current weight.",
            StringKey.PROFILE_ERROR_TARGET_TOO_LOW to "This goal is outside the supported range for self-guided weight loss.",
            StringKey.PROFILE_ERROR_CALORIES_RANGE to "Enter a supported daily calorie target.",
            StringKey.PROFILE_ERROR_MACRO_RANGE to "Enter a supported macronutrient value.",
            StringKey.PROFILE_ERROR_WATER_RANGE to "Enter a water goal from 500 to 6000 ml.",
            StringKey.PROFILE_SAFE_CALC_NOTE to "Auto-calculation is adult-only and limits excessive calorie deficits.",

            StringKey.ACTIVITY_SEDENTARY to "Sedentary Lifestyle",
            StringKey.ACTIVITY_SEDENTARY_DESC to "Under 3,000 steps, mostly sitting",
            StringKey.ACTIVITY_LIGHT to "Light Activity",
            StringKey.ACTIVITY_LIGHT_DESC to "3,000–6,000 steps, walks and routine housework",
            StringKey.ACTIVITY_MODERATE to "Moderate Activity",
            StringKey.ACTIVITY_MODERATE_DESC to "6,000–10,000 steps, much of the day on your feet",
            StringKey.ACTIVITY_ACTIVE to "High Activity",
            StringKey.ACTIVITY_ACTIVE_DESC to "Over 10,000 steps, physical work or regular sport",

            StringKey.PACE_EASY to "Gentle (-0.25 kg/wk)",
            StringKey.PACE_RECOMMENDED to "Optimal (-0.5 kg/wk)",
            StringKey.PACE_FAST to "Fast (-0.75 kg/wk)",
            StringKey.PACE_MAX to "Maximum (-1.0 kg/wk)",

            StringKey.ANALYSIS_RESULT_TITLE to "Body Parameters Analysis",
            StringKey.ANALYSIS_RESULT_SUBTITLE to "BMI, daily calorie burn & timeline forecast",
            StringKey.ANALYSIS_BMI_LABEL to "Body Mass Index (BMI)",
            StringKey.BMI_UNDERWEIGHT to "Underweight",
            StringKey.BMI_NORMAL to "Healthy Weight ✓",
            StringKey.BMI_OVERWEIGHT to "Overweight",
            StringKey.BMI_OBESITY to "Obesity",
            StringKey.ANALYSIS_IDEAL_WEIGHT_RANGE to "Healthy Range",
            StringKey.ANALYSIS_DIFF_TO_GOAL to "To Goal",
            StringKey.ANALYSIS_TO_LOSE to "To lose",
            StringKey.ANALYSIS_TO_GAIN to "To gain",
            StringKey.ANALYSIS_TDEE_LABEL to "Daily Burn (TDEE)",
            StringKey.ANALYSIS_BMR_LABEL to "Basal Rate (BMR)",
            StringKey.ANALYSIS_WEEKS_ESTIMATE to "Est. Timeline",
            StringKey.ANALYSIS_HELP_BUTTON_TEXT to "Details ❔",

            StringKey.EXPLAINER_DIALOG_TITLE to "Personal Body Analysis",
            StringKey.EXPLAINER_SECTION_WHAT_IT_MEANS to "1. What Your Numbers Mean",
            StringKey.EXPLAINER_SECTION_PROBLEMS to "2. Health Impact",
            StringKey.EXPLAINER_SECTION_HOW_TO_IMPROVE to "3. How to Improve Safely",
            StringKey.EXPLAINER_SECTION_HOW_APP_HELPS to "4. How SlimTrack Helps",
            StringKey.EXPLAINER_SECTION_MOTIVATION to "5. Daily Motivation",
            StringKey.EXPLAINER_GOT_IT_BUTTON to "Got it, let's go! ✓",

            StringKey.LEAD_MAGNET_TITLE to "Unlock 100% of SlimTrack PRO",
            StringKey.LEAD_MAGNET_SUBTITLE to "Barcode scanner, body analysis, fasting & auto-macros",
            StringKey.LEAD_MAGNET_PERK_1 to "Barcode scanner & quick calorie entry",
            StringKey.LEAD_MAGNET_PERK_2 to "Clinical BMI analysis & goal week forecast",
            StringKey.LEAD_MAGNET_PERK_3 to "16:8 fasting timer & full report export",
            StringKey.LEAD_MAGNET_BUTTON to "Plans & Offers ⭐",

            StringKey.DIARY_WATER_HINT to "Boosts metabolism & fullness",
            StringKey.DIARY_EXERCISE_TITLE to "Exercise & Activity",
            StringKey.DIARY_EXERCISE_HINT to "Adds burned calories to budget",
            StringKey.DIARY_NO_ITEMS_YET to "No entries yet",
            StringKey.DIARY_HABITS_TITLE to "Habit Tracker",
            StringKey.DIARY_HABITS_DESC to "Daily discipline checklist",
            StringKey.DIARY_OPEN_HABITS to "Open",
            StringKey.DIARY_CALORIES_OVER to "exceeded",
            StringKey.DIARY_CURRENT_DEFICIT to "Deficit:",
            StringKey.DIARY_CURRENT_SURPLUS to "Surplus:",
            StringKey.DIARY_MACROS_TITLE to "Macronutrients",

            StringKey.ADMIN_TAB_UPDATE to "Update (.APK)",
            StringKey.ADMIN_INSTALL_APK_BUTTON to "Install Update (.apk)",
            StringKey.ADMIN_INSTALL_APK_DESC to "Pick the downloaded APK file to update in-place without losing data",
            StringKey.ADMIN_APK_NO_DELETE_NOTE to "💡 No need to uninstall! Android updates the app in-place and preserves all your logs.",

            StringKey.UNIT_KCAL to "kcal",
            StringKey.UNIT_GRAM to "g",
            StringKey.UNIT_ML to "ml",
            StringKey.UNIT_CM to "cm",
            StringKey.UNIT_MIN to "min",

            StringKey.INSIGHTS_SUBTITLE to "Science-backed sustainable weight loss",
            StringKey.INSIGHTS_FASTING_TITLE to "Intermittent Fasting 16:8",
            StringKey.INSIGHTS_FASTING_ACTIVE to "Active fasting window",
            StringKey.INSIGHTS_FASTING_STOPPED to "Timer stopped",
            StringKey.INSIGHTS_FASTING_BTN_START to "Start",
            StringKey.INSIGHTS_FASTING_BTN_STOP to "Stop",
            StringKey.INSIGHTS_RULES_TITLE to "5 Golden Rules of Fat Loss",

            StringKey.WEIGHT_HEADER_SUBTITLE to "Track body weight and measurements",
            StringKey.WEIGHT_ADD_BUTTON to "Log Weight",
            StringKey.WEIGHT_LOST_LABEL to "Lost",
            StringKey.WEIGHT_REMAINING_LABEL to "To Goal",
            StringKey.WEIGHT_WEEKS_LABEL to "Weeks",
            StringKey.WEIGHT_PROGRESS_TO_GOAL to "Goal Progress",
            StringKey.WEIGHT_FORECAST_TEXT to "Est. time to goal: ≈",
            StringKey.WEIGHT_HISTORY_TITLE to "Weight Log History",
            StringKey.WEIGHT_EMPTY_HISTORY to "No entries yet. Tap 'Log Weight'!",
            StringKey.WEIGHT_UNIT_KG to "kg",

            StringKey.CHART_SUBTITLE to "Body weight trend over time",
            StringKey.CHART_FILTER_7D to "7d",
            StringKey.CHART_FILTER_30D to "30d",
            StringKey.CHART_FILTER_ALL to "All",
            StringKey.CHART_EMPTY to "No data yet. Log your weight!",
            StringKey.CHART_ACTUAL_WEIGHT to "Weight",
            StringKey.TREND_CARD_TITLE to "Trend Weight",
            StringKey.TREND_CARD_SUBTITLE to "Smoothed line without water fluctuations",
            StringKey.TREND_LABEL_TREND to "Trend",
            StringKey.TREND_LABEL_SCALE to "Scale",
            StringKey.TREND_INFO_GOOD to "Water retention temporarily hides progress — fat loss is on track!",
            StringKey.TREND_INFO_NORMAL to "Weight is steadily decreasing with your calorie deficit.",
            StringKey.WEEKLY_DEFICIT_SUBTITLE to "7-day cumulative calorie balance",
            StringKey.WEEKLY_FAT_BURN_FORECAST to "Weekly fat burned: ≈",
            StringKey.WEEKLY_FAT_BURN_NOTE to "7,700 kcal deficit = 1 kg of pure body fat",
            StringKey.MEAS_TITLE to "Body Measurements",
            StringKey.MEAS_SUBTITLE to "Circumference in cm",
            StringKey.MEAS_WAIST to "Waist",
            StringKey.MEAS_HIPS to "Hips",
            StringKey.MEAS_CHEST to "Chest",
            StringKey.MEAS_BICEP to "Bicep",
            StringKey.MEAS_THIGH to "Thigh",
            StringKey.MEAS_ADD_BTN to "Log",
            StringKey.MEAS_EMPTY_HINT to "Log waist and hip measurements to track body recomposition",
            StringKey.HABIT_STREAK_BADGE to "day streak",
            StringKey.HABIT_1 to "Drink daily water target",
            StringKey.HABIT_2 to "Hit daily protein target",
            StringKey.HABIT_3 to "Stay within calorie deficit",
            StringKey.HABIT_4 to "8,000+ steps or workout",
            StringKey.HABIT_5 to "No food 3 hours before sleep",
            StringKey.EXPORT_SUBTITLE to "Summary of weight & nutrition logs",
            StringKey.EXPORT_SHARE_BTN to "Share",
            StringKey.EXPORT_COPY_BTN to "Copy",
            StringKey.EXPORT_COPIED_TOAST to "Report copied!",

            StringKey.DIALOG_ADD_FOOD_TITLE to "Add to",
            StringKey.DIALOG_GRAMS_LABEL to "Serving size (g)",
            StringKey.DIALOG_EMPTY_FOODS to "No matching foods found",
            StringKey.DIALOG_ADD_EXERCISE_TITLE to "Add Activity",
            StringKey.DIALOG_EXERCISE_NAME_LABEL to "Workout / Activity name",
            StringKey.DIALOG_EXERCISE_MINUTES_LABEL to "Duration (min)",
            StringKey.DIALOG_EXERCISE_KCAL_LABEL to "Burned (kcal)",
            StringKey.DIALOG_ADD_WEIGHT_TITLE to "Log Weight",
            StringKey.DIALOG_WEIGHT_INPUT_LABEL to "Your weight (kg)",
            StringKey.DIALOG_NOTE_LABEL to "Note (optional)",
            StringKey.DIALOG_QUICK_CALORIES_TITLE to "Quick Calories",
            StringKey.DIALOG_QUICK_CALORIES_SUBTITLE to "Instant calorie entry without searching",
            StringKey.DIALOG_BARCODE_TITLE to "Barcode Scanner",
            StringKey.DIALOG_BARCODE_SUBTITLE to "Point the camera at an EAN/UPC barcode or enter it manually",
            StringKey.DIALOG_BARCODE_INPUT_LABEL to "Barcode",
            StringKey.DIALOG_BARCODE_CAMERA_HINT to "Place the barcode inside the frame",
            StringKey.DIALOG_BARCODE_CAMERA_PERMISSION to "Camera access is required to scan. Barcode recognition happens on the device.",
            StringKey.DIALOG_BARCODE_GRANT_PERMISSION to "Allow camera",
            StringKey.DIALOG_BARCODE_NOT_FOUND to "Product not found in the online catalogue. You can add the details from its label.",
            StringKey.DIALOG_BARCODE_NETWORK_ERROR to "Cannot connect to the online catalogue. Check your internet and try again.",
            StringKey.DIALOG_BARCODE_SERVICE_ERROR to "The online catalogue is temporarily unavailable. Try again later.",
            StringKey.DIALOG_BARCODE_INCOMPLETE to "Product found, but calories or macros are incomplete. Enter the values per 100 g from its label.",
            StringKey.DIALOG_BARCODE_INVALID_CODE to "Enter a barcode with 8, 12, 13 or 14 digits.",
            StringKey.DIALOG_BARCODE_SOURCE to "Online catalogue: Open Food Facts • ODbL",
            StringKey.DIALOG_BARCODE_ADD_MANUALLY to "Add details from the label",
            StringKey.DIALOG_BARCODE_SAVE_ERROR to "Could not save this product. Please try again.",
            StringKey.DIALOG_BARCODE_SCAN_AGAIN to "Scan again",
            StringKey.DIALOG_BARCODE_CAMERA_ERROR to "Could not start the camera. You can enter the code manually.",
            StringKey.DIALOG_BARCODE_MANUAL_HINT to "Or enter the code manually",
            StringKey.DIALOG_BARCODE_SEARCHING to "Searching online…",
            StringKey.DIALOG_PROMO_TITLE to "Promo Code or Invite Link",
            StringKey.DIALOG_PROMO_SUBTITLE to "Enter an invite code to unlock Slim / Premium",
            StringKey.DIALOG_PROMO_INPUT_LABEL to "Promo code or link",
            StringKey.DIALOG_PROMO_ACTIVATE_BTN to "Activate Access ✓",
            StringKey.DIALOG_PAYMENT_TITLE to "Subscription Checkout",
            StringKey.DIALOG_PAYMENT_SELECT_METHOD to "Choose payment method:",
            StringKey.DIALOG_PAYMENT_REQUISITES_HEADER to "Payment details:",
            StringKey.DIALOG_PAYMENT_AMOUNT_TO_PAY to "Amount due:",
            StringKey.DIALOG_PAYMENT_NOTE_LABEL to "Reference / Name / TXID",
            StringKey.DIALOG_PAYMENT_CONFIRM_BTN to "I Paid — Create Verification Request",
            StringKey.DIALOG_PAYMENT_SECURITY_FOOTER to "🔒 Access is activated only after payment verification",
            StringKey.DIALOG_RECEIPT_TITLE to "Verification Request Created",
            StringKey.DIALOG_RECEIPT_SUBTITLE_FMT to "Send the request to support. Access is not active yet",
            StringKey.DIALOG_RECEIPT_PENDING_BADGE to "PENDING",
            StringKey.DIALOG_RECEIPT_ORDER_ID to "Order ID:",
            StringKey.DIALOG_RECEIPT_DATE to "Date & Time:",
            StringKey.DIALOG_RECEIPT_METHOD to "Payment Method:",
            StringKey.DIALOG_RECEIPT_TOTAL to "Total:",
            StringKey.DIALOG_RECEIPT_DONE_BTN to "Done",
            StringKey.ADMIN_REQUISITES_HEADER to "⚙️ Payment Requisites Setup",
            StringKey.ADMIN_REQUISITES_SUBTITLE to "Configure your cards and wallets for receiving payments",
            StringKey.ADMIN_REQUISITES_ENABLED_METHODS to "Enabled payment methods:",
            StringKey.ADMIN_REQUISITES_SAVE_BTN to "Save Requisites ✓",
            StringKey.ADMIN_REQUISITES_SAVED_TOAST to "Payment details saved!",

            StringKey.FOODS_TITLE to "Catalogue",
            StringKey.CATALOG_DISHES to "Dishes",
            StringKey.CATALOG_PRODUCTS to "Products",
            StringKey.CATALOG_ALL_CUISINES to "All cuisines",
            StringKey.CATALOG_EMPTY_HINT to "No matches. Try another name or clear the filters. Add your own dish in My recipe.",
            StringKey.DISH_ESTIMATE_NOTE to "Estimated nutrition for a representative recipe. Ingredients, oil and cooking method affect the result.",
            StringKey.RECIPE_CALCULATED_NOTE to "Calculated from your ingredients and finished weight. Accuracy depends on the entered data.",
            StringKey.RECIPE_MY_RECIPE to "My recipe",
            StringKey.RECIPE_BUILDER_HINT to "Add ingredients and their weights, using the correct raw or cooked state. Include oil retained in the dish. Weigh the entire finished dish without its container, including soup liquid.",
            StringKey.RECIPE_NAME to "Dish name",
            StringKey.RECIPE_ADD_INGREDIENT to "Find an ingredient",
            StringKey.RECIPE_INGREDIENT_NOT_FOUND to "Ingredient not found. Add it first in My product using its label values.",
            StringKey.RECIPE_FINISHED_WEIGHT to "Whole finished dish weight, g",
            StringKey.RECIPE_VALIDATION_HINT to "Enter positive weights for every ingredient and the finished dish. Check the finished weight.",
            StringKey.RECIPE_SAVE to "Save dish",
            StringKey.FOODS_SUBTITLE to "Calories & macros per 100g",
            StringKey.FOODS_SEARCH_HINT to "Find a dish or product…",
            StringKey.FOODS_CAT_ALL to "All",
            StringKey.FOODS_CAT_MEAT to "Meat & Poultry",
            StringKey.FOODS_CAT_FISH to "Fish",
            StringKey.FOODS_CAT_GRAINS to "Grains",
            StringKey.FOODS_CAT_DAIRY to "Dairy",
            StringKey.FOODS_CAT_VEG to "Vegetables",
            StringKey.FOODS_CAT_FRUITS to "Fruits",
            StringKey.FOODS_CAT_SNACKS to "Nuts & Snacks",
            StringKey.FOODS_CREATE_BTN to "Custom Food",
            StringKey.FOOD_CUSTOM_BADGE to "Custom",
            StringKey.FOOD_NAME_LABEL to "Food name",
            StringKey.FOOD_CATEGORY_LABEL to "Category",
            StringKey.FOOD_KCAL_100G_LABEL to "Kcal per 100g",

            StringKey.ADMIN_TAB_FAMILY to "Family & Guests",
            StringKey.ADMIN_TAB_REQUISITES to "Requisites",
            StringKey.ADMIN_TAB_TIERS_JSON to "Plans & JSON",
            StringKey.ADMIN_FAMILY_TITLE to "Lifetime Family VIP Access 👑",
            StringKey.ADMIN_FAMILY_DESC to "Unlimited permanent PRO access for family & close friends",
            StringKey.ADMIN_FAMILY_SHARE_BTN to "Share to Family",
            StringKey.ADMIN_COPY_CODE to "Code",
            StringKey.ADMIN_QUICK_GUEST_LINKS to "Quick Guest Invites:",
            StringKey.ADMIN_GUEST_SLIM_7D to "Guest Slim Pass (7 Days)",
            StringKey.ADMIN_GUEST_PREMIUM_7D to "Trial Premium Pass (7 Days)",
            StringKey.ADMIN_GUEST_PREMIUM_30D to "Full Premium Pass (30 Days)",
            StringKey.ADMIN_SHARE_ACTION to "Share",
            StringKey.ADMIN_CUSTOM_INVITE_CREATE to "Create Custom Promo Code",
            StringKey.ADMIN_CUSTOM_INVITE_HIDE to "Hide Builder",
            StringKey.ADMIN_ACTIVE_INVITES_LIST to "Active Codes",
            StringKey.ADMIN_DURATION_FOREVER to "Lifetime 👑",

            StringKey.TIP_1_TITLE to "1. Calorie Deficit is Key",
            StringKey.TIP_1_SUBTITLE to "The Law of Energy Balance",
            StringKey.TIP_1_CONTENT to "Burn 400–500 kcal more than you eat daily. This drives steady fat loss without extreme hunger or rebounds.",
            StringKey.TIP_2_TITLE to "2. Protein Protects Muscle",
            StringKey.TIP_2_SUBTITLE to "Target: 1.6 – 2.0 g per kg of weight",
            StringKey.TIP_2_CONTENT to "Protein-rich foods (poultry, fish, eggs, cottage cheese) preserve lean muscle mass and keep you full.",
            StringKey.TIP_3_TITLE to "3. Water & Scale Fluctuations",
            StringKey.TIP_3_SUBTITLE to "Why weight shifts overnight",
            StringKey.TIP_3_CONTENT to "Carbs and salt hold water temporarily. Drink 30 ml of water per kg of weight to reduce bloating and boost metabolism.",
            StringKey.TIP_4_TITLE to "4. Daily Non-Exercise Activity (NEAT)",
            StringKey.TIP_4_SUBTITLE to "Daily steps beat occasional workouts",
            StringKey.TIP_4_CONTENT to "8,000 – 10,000 steps per day burn up to 400 kcal effortlessly without spiking appetite.",
            StringKey.TIP_5_TITLE to "5. Breaking a Weight Plateau",
            StringKey.TIP_5_SUBTITLE to "When weight stalls for 2 weeks",
            StringKey.TIP_5_CONTENT to "Track cooking oils and sauces, measure waist circumference in cm, and get 7–8 hours of quality sleep.",

            StringKey.PROFILE_SMART_CALC_TITLE to "Smart Formula Calculator",
            StringKey.PROFILE_SMART_CALC_DESC to "Mifflin-St Jeor tailored to your body",
            StringKey.PROFILE_RECALC_BTN to "Calculate",
            StringKey.PROFILE_CHANGE_PLAN_BTN to "Plans",
            StringKey.PROFILE_PAYMENT_BTN to "Payment",
            StringKey.PROFILE_PROMO_BTN to "Promo Code",
            StringKey.LANGUAGE_DROPDOWN_DESC to "Language list",
            StringKey.PROMO_ACTIVATION_HINT to "Enter a promo code or guest link to activate Slim or Premium",
            StringKey.PROMO_INVALID_CODE to "Invalid code",
            StringKey.ADMIN_PROMO_CODE_LABEL to "Promo code (VIP-FRIEND)",
            StringKey.ADMIN_SOURCE_LABEL to "Source:",
            StringKey.ADMIN_VERSION_LABEL to "Version:",
            StringKey.ADMIN_PAYMENT_SETTINGS_TITLE to "⚙️ Payment settings",
            StringKey.ADMIN_PAYMENT_SETTINGS_DESC to "Set the details for manual subscription payments. Access is activated only after verified payment confirmation.",
            StringKey.ADMIN_PAYMENT_METHODS_ENABLED to "Enabled payment methods:",
            StringKey.ADMIN_PAYMENT_METHODS_HINT to "Disable unused methods so users only see currently available payment options.",
            StringKey.ADMIN_MONOBANK_SECTION to "Monobank (Ukraine 🇺🇦)",
            StringKey.ADMIN_MONOBANK_CARD_LABEL to "Monobank card number",
            StringKey.ADMIN_MONOBANK_JAR_LABEL to "Monobank Jar link",
            StringKey.ADMIN_PAYPAL_SECTION to "PayPal (international 🌐)",
            StringKey.ADMIN_PAYPAL_EMAIL_LABEL to "PayPal account email",
            StringKey.ADMIN_PAYPAL_ME_LABEL to "PayPal.Me link",
            StringKey.ADMIN_CRYPTO_SECTION to "Crypto (USDT and Bitcoin)",
            StringKey.ADMIN_USDT_ADDRESS_LABEL to "USDT wallet address",
            StringKey.ADMIN_USDT_NETWORK_LABEL to "USDT network (TRC-20, BEP-20, ERC-20)",
            StringKey.ADMIN_BTC_ADDRESS_LABEL to "Bitcoin (BTC) wallet address",
            StringKey.ADMIN_BANK_SECTION to "Bank card and IBAN",
            StringKey.ADMIN_CARD_NUMBER_LABEL to "Visa / MasterCard number",
            StringKey.ADMIN_CARD_HOLDER_LABEL to "Cardholder name in Latin characters",
            StringKey.ADMIN_IBAN_LABEL to "IBAN account",
            StringKey.ADMIN_SUPPORT_CONTACTS to "Payment support contacts",
            StringKey.ADMIN_TELEGRAM_LABEL to "Support Telegram",
            StringKey.ADMIN_SUPPORT_EMAIL_LABEL to "Support email",
            StringKey.ADMIN_PAYMENT_DETAILS_SAVED to "Payment details saved",
            StringKey.ADMIN_SAVE_PAYMENT_DETAILS to "Save payment details ✓",
            StringKey.SHARE_LABEL to "Share",
            StringKey.COPY_LABEL to "Copy"
        )
    )

    private val foodTranslationsEn = mapOf(
        "Куриная грудка (отварная)" to "Boiled Chicken Breast",
        "Грудка индейки (запеченная)" to "Baked Turkey Breast",
        "Говядина нежирная" to "Lean Beef",
        "Куриные котлеты на пару" to "Steamed Chicken Cutlets",
        "Лосось / Семга запеченная" to "Baked Salmon",
        "Тунец в собственном соку" to "Canned Tuna in Water",
        "Минтай отварной" to "Boiled Pollock",
        "Креветки отварные" to "Boiled Shrimp",
        "Яйцо куриное вареное (1 шт)" to "Boiled Egg (1 pc)",
        "Яйцо куриное вареное" to "Boiled Egg",
        "Яичный белок" to "Egg Whites",
        "Творог 5%" to "Cottage Cheese 5%",
        "Творог 0.5% (обезжиренный)" to "Low-Fat Cottage Cheese 0.5%",
        "Греческий йогурт 2%" to "Greek Yogurt 2%",
        "Кефир 1%" to "Kefir 1%",
        "Сыр Моцарелла Light" to "Light Mozzarella Cheese",
        "Гречневая каша (вареная)" to "Boiled Buckwheat",
        "Овсянка на воде" to "Oatmeal (with water)",
        "Рис бурый (вареный)" to "Boiled Brown Rice",
        "Рис белый басмати" to "White Basmati Rice",
        "Макароны тв. сортов (вареные)" to "Boiled Durum Pasta",
        "Картофель отварной" to "Boiled Potatoes",
        "Хлеб цельнозерновой" to "Whole Grain Bread",
        "Хлебцы ржаные" to "Rye Crispbread",
        "Огурец свежий" to "Fresh Cucumber",
        "Помидор свежий" to "Fresh Tomato",
        "Брокколи на пару" to "Steamed Broccoli",
        "Салат Айсберг / Шпинат" to "Iceberg Lettuce / Spinach",
        "Болгарский перец" to "Bell Pepper",
        "Яблоко зеленое" to "Green Apple",
        "Банан" to "Banana",
        "Клубника / Черника" to "Strawberries / Blueberries",
        "Апельсин / Грейпфрут" to "Orange / Grapefruit",
        "Авокадо" to "Avocado",
        "Оливковое масло" to "Olive Oil",
        "Миндаль" to "Almonds",
        "Грецкие орехи" to "Walnuts",
        "Протеин сывороточный (Whey)" to "Whey Protein Isolate",
        "Протеиновый батончик без сахара" to "Sugar-Free Protein Bar",
        "Темный шоколад 85%" to "Dark Chocolate 85%",
        "Шоколад темный 85%" to "Dark Chocolate 85%",
        "Быстрые калории" to "Quick Calories",
        "Кофе с молоком / Перекус" to "Coffee with Milk / Snack",
        "Легкий перекус" to "Light Snack",
        "Полноценное блюдо" to "Full Meal",
        "Праздничное блюдо / Десерт" to "Treat / Dessert"
    )

    private val foodTranslationsUk = mapOf(
        "Куриная грудка (отварная)" to "Куряча грудка (відварна)",
        "Грудка индейки (запеченная)" to "Грудка індички (запечена)",
        "Говядина нежирная" to "Яловичина нежирна",
        "Куриные котлеты на пару" to "Курячі котлети на парі",
        "Лосось / Семга запеченная" to "Лосось / Сьомга запечена",
        "Тунец в собственном соку" to "Тунець у власному соку",
        "Минтай отварной" to "Мінтай відварний",
        "Креветки отварные" to "Креветки відварні",
        "Яйцо куриное вареное (1 шт)" to "Яйце куряче варене (1 шт)",
        "Яйцо куриное вареное" to "Яйце куряче варене",
        "Яичный белок" to "Яєчний білок",
        "Творог 5%" to "Кисломолочний сир 5%",
        "Творог 0.5% (обезжиренный)" to "Сир кисломолочний 0.5%",
        "Греческий йогурт 2%" to "Грецький йогурт 2%",
        "Кефир 1%" to "Кефір 1%",
        "Сыр Моцарелла Light" to "Сир Моцарела Light",
        "Гречневая каша (вареная)" to "Гречана каша (варена)",
        "Овсянка на воде" to "Вівсянка на воді",
        "Рис бурый (вареный)" to "Рис бурий (варений)",
        "Рис белый басмати" to "Рис білий басматі",
        "Макароны тв. сортов (вареные)" to "Макарони тв. сортів (варені)",
        "Картофель отварной" to "Картопля відварна",
        "Хлеб цельнозерновой" to "Хліб цільнозерновий",
        "Хлебцы ржаные" to "Хлібці житні",
        "Огурец свежий" to "Огірок свіжий",
        "Помидор свежий" to "Помідор свіжий",
        "Брокколи на пару" to "Броколі на парі",
        "Салат Айсберг / Шпинат" to "Салат Айсберг / Шпинат",
        "Болгарский перец" to "Болгарський перець",
        "Яблоко зеленое" to "Яблуко зелене",
        "Банан" to "Банан",
        "Клубника / Черника" to "Полуниця / Чорниця",
        "Апельсин / Грейпфрут" to "Апельсин / Грейпфрут",
        "Авокадо" to "Авокадо",
        "Оливковое масло" to "Оливкова олія",
        "Миндаль" to "Мигдаль",
        "Грецкие орехи" to "Волоські горіхи",
        "Протеин сывороточный (Whey)" to "Протеїн сироватковий (Whey)",
        "Протеиновый батончик без сахара" to "Протеїновий батончик без цукру",
        "Темный шоколад 85%" to "Темний шоколад 85%",
        "Шоколад темный 85%" to "Шоколад темний 85%",
        "Быстрые калории" to "Швидкі калорії",
        "Кофе с молоком / Перекус" to "Кава з молоком / Перекус",
        "Легкий перекус" to "Легкий перекус",
        "Полноценное блюдо" to "Повноцінна страва",
        "Праздничное блюдо / Десерт" to "Святкова страва / Десерт"
    )

    private val activityTranslationsUk = mapOf(
        "Быстрая ходьба" to "Швидка ходьба",
        "Бег трусцой" to "Біг підтюпцем",
        "Силовая тренировка" to "Силове тренування",
        "Велосипед" to "Велосипед",
        "Плавание" to "Плавання",
        "Домашняя зарядка / Йога" to "Домашня зарядка / Йога"
    )

    private val activityTranslationsEn = mapOf(
        "Быстрая ходьба" to "Brisk Walking",
        "Бег трусцой" to "Jogging",
        "Силовая тренировка" to "Strength Training",
        "Велосипед" to "Cycling",
        "Плавание" to "Swimming",
        "Домашняя зарядка / Йога" to "Home Workout / Yoga"
    )

    private val noteTranslationsUk = mapOf(
        "Старт программы" to "Старт програми",
        "Утренний замер" to "Ранковий вимір",
        "Замер в начале программы" to "Вимір на початку програми",
        "Натощак" to "Натщесерце",
        "После тренировки" to "Після тренування"
    )

    private val noteTranslationsEn = mapOf(
        "Старт программы" to "Program Start",
        "Утренний замер" to "Morning Check-in",
        "Замер в начале программы" to "Initial Measurement",
        "Натощак" to "Fasted Morning",
        "После тренировки" to "Post-Workout"
    )

    fun translateFoodName(rawName: String, lang: AppLanguage = _currentLanguage.value): String {
        com.example.data.catalog.PreparedDishCatalog.translatedName(rawName, lang)?.let { return it }
        return when (lang) {
            AppLanguage.RU -> rawName
            AppLanguage.UK -> foodTranslationsUk[rawName] ?: rawName
            AppLanguage.EN -> foodTranslationsEn[rawName] ?: rawName
        }
    }

    fun translateActivityName(rawName: String, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.RU -> rawName
            AppLanguage.UK -> activityTranslationsUk[rawName] ?: rawName
            AppLanguage.EN -> activityTranslationsEn[rawName] ?: rawName
        }
    }

    fun translateNote(rawNote: String, lang: AppLanguage = _currentLanguage.value): String {
        if (rawNote.isBlank()) return ""
        return when (lang) {
            AppLanguage.RU -> rawNote
            AppLanguage.UK -> noteTranslationsUk[rawNote] ?: rawNote
            AppLanguage.EN -> noteTranslationsEn[rawNote] ?: rawNote
        }
    }

    fun translateCategory(rawCategory: String, lang: AppLanguage = _currentLanguage.value): String {
        val key = when (rawCategory) {
            "Все", "All", "Всі" -> StringKey.FOODS_CAT_ALL
            "Мясо и птица", "Meat & Poultry", "М'ясо та птиця" -> StringKey.FOODS_CAT_MEAT
            "Рыба", "Fish", "Риба" -> StringKey.FOODS_CAT_FISH
            "Крупы", "Grains", "Крупи" -> StringKey.FOODS_CAT_GRAINS
            "Молочные продукты", "Dairy", "Молочні продукти" -> StringKey.FOODS_CAT_DAIRY
            "Овощи", "Vegetables", "Овочі" -> StringKey.FOODS_CAT_VEG
            "Фрукты", "Fruits", "Фрукти" -> StringKey.FOODS_CAT_FRUITS
            "Орехи и снеки", "Nuts & Snacks", "Горіхи та снеки" -> StringKey.FOODS_CAT_SNACKS
            else -> null
        }
        return if (key != null) getString(key, lang) else com.example.data.catalog.PreparedDishCatalog.categoryName(rawCategory, lang)
    }

    fun translateFeatureTitle(feature: AppFeature, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.RU -> feature.titleRu
            AppLanguage.EN -> feature.titleEn
            AppLanguage.UK -> when (feature) {
                AppFeature.FASTING_TIMER -> "Інтервальне голодування"
                AppFeature.BODY_MEASUREMENTS -> "Виміри тіла"
                AppFeature.TREND_WEIGHT -> "Трендова вага"
                AppFeature.WEEKLY_DEFICIT -> "Аналіз дефіциту калорій"
                AppFeature.EXPORT_REPORTS -> "Експорт звітів"
                AppFeature.BARCODE_SCANNER -> "Сканер штрих-кодів"
                AppFeature.HABIT_STREAKS -> "Трекер звичок (Streaks)"
                AppFeature.QUICK_CALORIES -> "Швидкі калорії"
                AppFeature.CUSTOM_FOOD -> "Створення своїх страв"
            }
        }
    }

    fun translatePaymentMethodTitle(method: PaymentMethodType, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.RU -> method.title
            AppLanguage.UK -> when (method) {
                PaymentMethodType.MONOBANK -> "Monobank (Україна 🇺🇦)"
                PaymentMethodType.PAYPAL -> "PayPal (Міжнародний 🌍)"
                PaymentMethodType.CRYPTO_USDT -> "USDT Tether (Криптовалюта 💎)"
                PaymentMethodType.CRYPTO_BTC -> "Bitcoin (BTC ₿)"
                PaymentMethodType.BANK_CARD -> "Банківська картка (Visa / MC 💳)"
                PaymentMethodType.IBAN -> "IBAN / SEPA (Рахунок банку 🏦)"
            }
            AppLanguage.EN -> when (method) {
                PaymentMethodType.MONOBANK -> "Monobank (Ukraine 🇺🇦)"
                PaymentMethodType.PAYPAL -> "PayPal (International 🌍)"
                PaymentMethodType.CRYPTO_USDT -> "USDT Tether (Crypto 💎)"
                PaymentMethodType.CRYPTO_BTC -> "Bitcoin (BTC ₿)"
                PaymentMethodType.BANK_CARD -> "Bank Card (Visa / MC 💳)"
                PaymentMethodType.IBAN -> "IBAN / SEPA (Bank Transfer 🏦)"
            }
        }
    }

    fun translatePaymentMethodDesc(method: PaymentMethodType, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.RU -> method.description
            AppLanguage.UK -> when (method) {
                PaymentMethodType.MONOBANK -> "Переказ на картку або Монобанку"
                PaymentMethodType.PAYPAL -> "Оплата карткою через PayPal або PayPal.Me"
                PaymentMethodType.CRYPTO_USDT -> "TRC-20 / BEP-20 швидкий переказ без комісій"
                PaymentMethodType.CRYPTO_BTC -> "Прямий переказ на Bitcoin адресу"
                PaymentMethodType.BANK_CARD -> "Прямий переказ за номером картки"
                PaymentMethodType.IBAN -> "Офіційний платіж за реквізитами IBAN"
            }
            AppLanguage.EN -> when (method) {
                PaymentMethodType.MONOBANK -> "Direct transfer to Monobank card or Jar"
                PaymentMethodType.PAYPAL -> "Pay with card via PayPal or PayPal.Me"
                PaymentMethodType.CRYPTO_USDT -> "Fast TRC-20 / BEP-20 transfer"
                PaymentMethodType.CRYPTO_BTC -> "Direct transfer to Bitcoin wallet"
                PaymentMethodType.BANK_CARD -> "Direct transfer to Visa / MasterCard"
                PaymentMethodType.IBAN -> "Official IBAN / SEPA bank transfer"
            }
        }
    }

    fun translateTierDescription(tier: SubscriptionTier, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.RU -> when (tier) {
                SubscriptionTier.FREE -> "Базовый контроль веса и учет питания"
                SubscriptionTier.SLIM -> "Продвинутый контроль тела и ускорение похудения"
                SubscriptionTier.PREMIUM -> "Максимальный безлимит + экспорт отчетов"
                SubscriptionTier.ADMIN -> "Полный доступ администратора"
            }
            AppLanguage.UK -> when (tier) {
                SubscriptionTier.FREE -> "Базовий контроль ваги та облік харчування"
                SubscriptionTier.SLIM -> "Просунутий контроль тіла та прискорення схуднення"
                SubscriptionTier.PREMIUM -> "Максимальний безліміт + експорт звітів"
                SubscriptionTier.ADMIN -> "Повний доступ адміністратора"
            }
            AppLanguage.EN -> when (tier) {
                SubscriptionTier.FREE -> "Basic weight tracking & calorie diary"
                SubscriptionTier.SLIM -> "Advanced body tracking & faster fat loss"
                SubscriptionTier.PREMIUM -> "Unlimited PRO access + report export"
                SubscriptionTier.ADMIN -> "Full Administrator Access"
            }
        }
    }

    fun translateTierPeriod(tier: SubscriptionTier, lang: AppLanguage = _currentLanguage.value): String {
        return when (lang) {
            AppLanguage.RU -> when (tier) {
                SubscriptionTier.FREE -> "бесплатно"
                SubscriptionTier.SLIM -> "/ месяц"
                SubscriptionTier.PREMIUM -> "/ навсегда"
                SubscriptionTier.ADMIN -> "VIP"
            }
            AppLanguage.UK -> when (tier) {
                SubscriptionTier.FREE -> "безкоштовно"
                SubscriptionTier.SLIM -> "/ місяць"
                SubscriptionTier.PREMIUM -> "/ назавжди"
                SubscriptionTier.ADMIN -> "VIP"
            }
            AppLanguage.EN -> when (tier) {
                SubscriptionTier.FREE -> "free"
                SubscriptionTier.SLIM -> "/ month"
                SubscriptionTier.PREMIUM -> "/ lifetime"
                SubscriptionTier.ADMIN -> "VIP"
            }
        }
    }

    fun translateTierPerks(tier: SubscriptionTier, lang: AppLanguage = _currentLanguage.value): List<String> {
        return when (lang) {
            AppLanguage.RU -> when (tier) {
                SubscriptionTier.FREE -> listOf(
                    "Дневник питания (КБЖУ) и трекер воды",
                    "Справочник основных продуктов",
                    "Ввод веса и базовый график"
                )
                SubscriptionTier.SLIM -> listOf(
                    "Все функции Free + Сканер штрих-кодов",
                    "Интервальное голодание 16:8 и Быстрые ккал",
                    "Замеры тела, трендовый вес и недельный дефицит",
                    "Трекер полезных привычек (Streaks)"
                )
                SubscriptionTier.PREMIUM, SubscriptionTier.ADMIN -> listOf(
                    "Все возможности тарифа Slim без ограничений",
                    "Экспорт отчетов для врача или тренера",
                    "Медицинский разбор параметров тела (ИМТ, TDEE)",
                    "Приоритетный доступ ко всем обновлениям"
                )
            }
            AppLanguage.UK -> when (tier) {
                SubscriptionTier.FREE -> listOf(
                    "Щоденник харчування (КБЖВ) та трекер води",
                    "Довідник основних продуктів",
                    "Введення ваги та базовий графік"
                )
                SubscriptionTier.SLIM -> listOf(
                    "Всі функції Free + Сканер штрих-кодів",
                    "Інтервальне голодування 16:8 та Швидкі ккал",
                    "Виміри тіла, трендова вага та тижневий дефіцит",
                    "Трекер корисних звичок (Streaks)"
                )
                SubscriptionTier.PREMIUM, SubscriptionTier.ADMIN -> listOf(
                    "Всі можливості тарифу Slim без обмежень",
                    "Експорт звітів для лікаря або тренера",
                    "Медичний розбір параметрів тіла (ІМТ, TDEE)",
                    "Пріоритетний доступ до всіх оновлень"
                )
            }
            AppLanguage.EN -> when (tier) {
                SubscriptionTier.FREE -> listOf(
                    "Calorie & macro diary + water tracker",
                    "Core food nutrition database",
                    "Weight logging & basic progress chart"
                )
                SubscriptionTier.SLIM -> listOf(
                    "Everything in Free + Barcode Scanner",
                    "16:8 Fasting timer & Quick Calories",
                    "Body measurements, trend weight & weekly deficit",
                    "Daily Habit Streaks tracker"
                )
                SubscriptionTier.PREMIUM, SubscriptionTier.ADMIN -> listOf(
                    "All Slim features with zero limits",
                    "Full progress report export for coach/doctor",
                    "Clinical body analysis (BMI, BMR, TDEE)",
                    "Lifetime priority access to all features"
                )
            }
        }
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.RU }

@Composable
fun LocalizationProvider(content: @Composable () -> Unit) {
    val language by LocalizationManager.currentLanguage.collectAsState()
    CompositionLocalProvider(LocalAppLanguage provides language, content = content)
}

@Composable
fun appLanguage(): AppLanguage = LocalAppLanguage.current

@Composable
fun appString(key: StringKey): String =
    LocalizationManager.getString(key, LocalAppLanguage.current)

@Composable
fun localizedText(ru: String, uk: String, en: String): String = when (LocalAppLanguage.current) {
    AppLanguage.RU -> ru
    AppLanguage.UK -> uk
    AppLanguage.EN -> en
}

@Composable
fun localizedFoodName(rawName: String): String =
    LocalizationManager.translateFoodName(rawName, LocalAppLanguage.current)

@Composable
fun localizedActivityName(rawName: String): String =
    LocalizationManager.translateActivityName(rawName, LocalAppLanguage.current)

@Composable
fun localizedNote(rawNote: String): String =
    LocalizationManager.translateNote(rawNote, LocalAppLanguage.current)

@Composable
fun localizedCategory(rawCategory: String): String =
    LocalizationManager.translateCategory(rawCategory, LocalAppLanguage.current)

