package com.example.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit Room migration history.
 *
 * Version 1 -> 2 is intentionally defensive because the original AI Studio project
 * shipped without exported Room schemas and used destructive fallback. The migration
 * rebuilds the known tables into the v2 shape while preserving every compatible value.
 * Missing columns are filled with neutral defaults instead of deleting the database.
 *
 * Version 2 -> 3 is a data-integrity migration. The schema is unchanged; it only removes
 * the old bundled demo history when (and only when) the whole database still matches the
 * untouched demo fingerprint. Any sign of real user activity makes the migration preserve
 * everything.
 */
object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            rebuildTable(
                db = db,
                table = "food_items",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `food_items_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `calories` REAL NOT NULL,
                        `protein` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `carbs` REAL NOT NULL,
                        `defaultServingGrams` REAL NOT NULL,
                        `isCustom` INTEGER NOT NULL,
                        `barcode` TEXT
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "rowid"),
                    CopyColumn("name", "''"),
                    CopyColumn("category", "''"),
                    CopyColumn("calories", "0.0"),
                    CopyColumn("protein", "0.0"),
                    CopyColumn("fat", "0.0"),
                    CopyColumn("carbs", "0.0"),
                    CopyColumn("defaultServingGrams", "100.0"),
                    CopyColumn("isCustom", "0"),
                    CopyColumn("barcode", "NULL", nullable = true)
                )
            )

            rebuildTable(
                db = db,
                table = "meal_entries",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `meal_entries_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `foodName` TEXT NOT NULL,
                        `grams` REAL NOT NULL,
                        `calories` REAL NOT NULL,
                        `protein` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `carbs` REAL NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "rowid"),
                    CopyColumn("date", "''"),
                    CopyColumn("mealType", "'SNACK'"),
                    CopyColumn("foodName", "''"),
                    CopyColumn("grams", "0.0"),
                    CopyColumn("calories", "0.0"),
                    CopyColumn("protein", "0.0"),
                    CopyColumn("fat", "0.0"),
                    CopyColumn("carbs", "0.0"),
                    CopyColumn("timestamp", "0")
                )
            )

            rebuildTable(
                db = db,
                table = "weight_logs",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `weight_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `weightKg` REAL NOT NULL,
                        `note` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "rowid"),
                    CopyColumn("date", "''"),
                    CopyColumn("weightKg", "0.0"),
                    CopyColumn("note", "''"),
                    CopyColumn("timestamp", "0")
                )
            )

            rebuildTable(
                db = db,
                table = "water_logs",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `water_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `amountMl` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "rowid"),
                    CopyColumn("date", "''"),
                    CopyColumn("amountMl", "0"),
                    CopyColumn("timestamp", "0")
                )
            )

            rebuildTable(
                db = db,
                table = "exercise_entries",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `exercise_entries_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `activityName` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `caloriesBurned` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "rowid"),
                    CopyColumn("date", "''"),
                    CopyColumn("activityName", "''"),
                    CopyColumn("durationMinutes", "0"),
                    CopyColumn("caloriesBurned", "0"),
                    CopyColumn("timestamp", "0")
                )
            )

            rebuildTable(
                db = db,
                table = "user_profile",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `user_profile_new` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `gender` TEXT NOT NULL,
                        `age` INTEGER NOT NULL,
                        `heightCm` REAL NOT NULL,
                        `startWeightKg` REAL NOT NULL,
                        `currentWeightKg` REAL NOT NULL,
                        `targetWeightKg` REAL NOT NULL,
                        `activityLevel` TEXT NOT NULL,
                        `goalPace` TEXT NOT NULL,
                        `dailyCalorieTarget` INTEGER NOT NULL,
                        `proteinTargetGrams` INTEGER NOT NULL,
                        `fatTargetGrams` INTEGER NOT NULL,
                        `carbTargetGrams` INTEGER NOT NULL,
                        `waterGoalMl` INTEGER NOT NULL,
                        `fastingHoursTarget` INTEGER NOT NULL,
                        `fastingStartTime` INTEGER NOT NULL,
                        `isFastingActive` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "1"),
                    CopyColumn("name", "''"),
                    CopyColumn("gender", "'MALE'"),
                    CopyColumn("age", "0"),
                    CopyColumn("heightCm", "0.0"),
                    CopyColumn("startWeightKg", "0.0"),
                    CopyColumn("currentWeightKg", "0.0"),
                    CopyColumn("targetWeightKg", "0.0"),
                    CopyColumn("activityLevel", "'MODERATE'"),
                    CopyColumn("goalPace", "'RECOMMENDED'"),
                    CopyColumn("dailyCalorieTarget", "0"),
                    CopyColumn("proteinTargetGrams", "0"),
                    CopyColumn("fatTargetGrams", "0"),
                    CopyColumn("carbTargetGrams", "0"),
                    CopyColumn("waterGoalMl", "0"),
                    CopyColumn("fastingHoursTarget", "16"),
                    CopyColumn("fastingStartTime", "0"),
                    CopyColumn("isFastingActive", "0")
                )
            )

            rebuildTable(
                db = db,
                table = "body_measurements",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `body_measurements_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `waistCm` REAL,
                        `hipsCm` REAL,
                        `chestCm` REAL,
                        `bicepCm` REAL,
                        `thighsCm` REAL,
                        `note` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("id", "rowid"),
                    CopyColumn("date", "''"),
                    CopyColumn("waistCm", "NULL", nullable = true),
                    CopyColumn("hipsCm", "NULL", nullable = true),
                    CopyColumn("chestCm", "NULL", nullable = true),
                    CopyColumn("bicepCm", "NULL", nullable = true),
                    CopyColumn("thighsCm", "NULL", nullable = true),
                    CopyColumn("note", "''"),
                    CopyColumn("timestamp", "0")
                )
            )

            rebuildTable(
                db = db,
                table = "daily_habits",
                createTempSql = """
                    CREATE TABLE IF NOT EXISTS `daily_habits_new` (
                        `date` TEXT NOT NULL,
                        `drankWaterTarget` INTEGER NOT NULL,
                        `hitProteinTarget` INTEGER NOT NULL,
                        `stayedInDeficit` INTEGER NOT NULL,
                        `completedWalkOrWorkout` INTEGER NOT NULL,
                        `noLateSnacking` INTEGER NOT NULL,
                        PRIMARY KEY(`date`)
                    )
                """.trimIndent(),
                columns = listOf(
                    CopyColumn("date", "''"),
                    CopyColumn("drankWaterTarget", "0"),
                    CopyColumn("hitProteinTarget", "0"),
                    CopyColumn("stayedInDeficit", "0"),
                    CopyColumn("completedWalkOrWorkout", "0"),
                    CopyColumn("noLateSnacking", "0")
                )
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // The v2 -> v3 schema is intentionally unchanged. This migration removes the
            // old generated demo history only when the database is still an untouched seed.
            if (looksLikeUntouchedLegacyDemo(db)) {
                db.execSQL("DELETE FROM `meal_entries`")
                db.execSQL("DELETE FROM `weight_logs`")
                db.execSQL("DELETE FROM `water_logs`")
                db.execSQL("DELETE FROM `exercise_entries`")
                db.execSQL("DELETE FROM `body_measurements`")
                db.execSQL("DELETE FROM `daily_habits`")
                db.execSQL("DELETE FROM `user_profile`")

                // Reset auto-increment counters only after deleting the untouched demo rows.
                db.execSQL(
                    """
                    DELETE FROM sqlite_sequence
                    WHERE name IN (
                        'meal_entries', 'weight_logs', 'water_logs',
                        'exercise_entries', 'body_measurements'
                    )
                    """.trimIndent()
                )
            }
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_profile ADD COLUMN averageDailySteps INTEGER")
            db.execSQL("ALTER TABLE user_profile ADD COLUMN householdMinutes INTEGER")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

    private data class CopyColumn(
        val name: String,
        val fallbackSql: String,
        val nullable: Boolean = false
    )

    private fun rebuildTable(
        db: SupportSQLiteDatabase,
        table: String,
        createTempSql: String,
        columns: List<CopyColumn>
    ) {
        val temp = "${table}_new"
        db.execSQL("DROP TABLE IF EXISTS `$temp`")
        db.execSQL(createTempSql)

        if (tableExists(db, table)) {
            val existingColumns = readColumns(db, table)
            val targetNames = columns.joinToString(", ") { "`${it.name}`" }
            val selectExpressions = columns.joinToString(", ") { column ->
                if (existingColumns.contains(column.name)) {
                    if (column.nullable) {
                        "`${column.name}`"
                    } else {
                        "COALESCE(`${column.name}`, ${column.fallbackSql})"
                    }
                } else {
                    column.fallbackSql
                }
            }

            db.execSQL(
                "INSERT INTO `$temp` ($targetNames) SELECT $selectExpressions FROM `$table`"
            )
            db.execSQL("DROP TABLE `$table`")
        }

        db.execSQL("ALTER TABLE `$temp` RENAME TO `$table`")
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '$table' LIMIT 1"
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    private fun readColumns(db: SupportSQLiteDatabase, table: String): Set<String> {
        val result = mutableSetOf<String>()
        db.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0) result += cursor.getString(nameIndex)
            }
        }
        return result
    }

    private fun looksLikeUntouchedLegacyDemo(db: SupportSQLiteDatabase): Boolean {
        val requiredTables = listOf(
            "user_profile",
            "weight_logs",
            "meal_entries",
            "water_logs",
            "body_measurements",
            "daily_habits",
            "exercise_entries"
        )
        if (requiredTables.any { !tableExists(db, it) }) return false

        if (count(db, "SELECT COUNT(*) FROM user_profile") != 1L) return false
        if (count(
                db,
                """
                SELECT COUNT(*) FROM user_profile
                WHERE id = 1
                  AND name = 'Пользователь'
                  AND gender = 'MALE'
                  AND age = 28
                  AND ABS(heightCm - 175.0) < 0.01
                  AND ABS(startWeightKg - 85.0) < 0.01
                  AND ABS(currentWeightKg - 82.3) < 0.01
                  AND ABS(targetWeightKg - 72.0) < 0.01
                  AND activityLevel = 'MODERATE'
                  AND goalPace = 'RECOMMENDED'
                  AND dailyCalorieTarget = 1850
                  AND proteinTargetGrams = 140
                  AND fatTargetGrams = 60
                  AND carbTargetGrams = 180
                  AND waterGoalMl = 2200
                  AND fastingHoursTarget = 16
                  AND fastingStartTime = 0
                  AND isFastingActive = 0
                """.trimIndent()
            ) != 1L
        ) return false

        if (count(db, "SELECT COUNT(*) FROM weight_logs") != 7L) return false
        val expectedWeights = listOf(85.0, 84.4, 83.9, 83.5, 83.1, 82.6, 82.3)
        for (weight in expectedWeights) {
            if (count(
                    db,
                    "SELECT COUNT(*) FROM weight_logs WHERE ABS(weightKg - $weight) < 0.01"
                ) != 1L
            ) return false
        }
        if (count(db, "SELECT COUNT(*) FROM weight_logs WHERE note = 'Старт программы'") != 1L) return false
        if (count(db, "SELECT COUNT(*) FROM weight_logs WHERE note = 'Утренний замер'") != 1L) return false

        if (count(db, "SELECT COUNT(*) FROM meal_entries") != 4L) return false
        val demoMealNames = listOf(
            "Овсянка на воде",
            "Яйцо куриное вареное",
            "Грудка индейки (запеченная)",
            "Гречневая каша (вареная)"
        )
        for (name in demoMealNames) {
            val escaped = name.replace("'", "''")
            if (count(db, "SELECT COUNT(*) FROM meal_entries WHERE foodName = '$escaped'") != 1L) return false
        }

        if (count(db, "SELECT COUNT(*) FROM water_logs") != 2L) return false
        if (count(db, "SELECT COUNT(*) FROM water_logs WHERE amountMl = 500") != 1L) return false
        if (count(db, "SELECT COUNT(*) FROM water_logs WHERE amountMl = 250") != 1L) return false

        if (count(db, "SELECT COUNT(*) FROM body_measurements") != 1L) return false
        if (count(
                db,
                """
                SELECT COUNT(*) FROM body_measurements
                WHERE ABS(waistCm - 84.5) < 0.01
                  AND ABS(hipsCm - 101.0) < 0.01
                  AND ABS(chestCm - 98.0) < 0.01
                  AND ABS(bicepCm - 33.5) < 0.01
                  AND ABS(thighsCm - 58.0) < 0.01
                  AND note = 'Замер в начале программы'
                """.trimIndent()
            ) != 1L
        ) return false

        if (count(db, "SELECT COUNT(*) FROM daily_habits") != 1L) return false
        if (count(
                db,
                """
                SELECT COUNT(*) FROM daily_habits
                WHERE drankWaterTarget = 1
                  AND hitProteinTarget = 1
                  AND stayedInDeficit = 1
                  AND completedWalkOrWorkout = 1
                  AND noLateSnacking = 0
                """.trimIndent()
            ) != 1L
        ) return false

        if (count(db, "SELECT COUNT(*) FROM exercise_entries") != 1L) return false
        if (count(
                db,
                """
                SELECT COUNT(*) FROM exercise_entries
                WHERE activityName = 'Быстрая ходьба'
                  AND durationMinutes = 35
                  AND caloriesBurned = 180
                """.trimIndent()
            ) != 1L
        ) return false

        // All generated diary/activity records were stamped with the same day.
        val mealDate = singleString(db, "SELECT MIN(date) FROM meal_entries") ?: return false
        if (mealDate != singleString(db, "SELECT MAX(date) FROM meal_entries")) return false
        if (mealDate != singleString(db, "SELECT MIN(date) FROM water_logs")) return false
        if (mealDate != singleString(db, "SELECT MAX(date) FROM water_logs")) return false
        if (mealDate != singleString(db, "SELECT date FROM body_measurements LIMIT 1")) return false
        if (mealDate != singleString(db, "SELECT date FROM daily_habits LIMIT 1")) return false
        if (mealDate != singleString(db, "SELECT date FROM exercise_entries LIMIT 1")) return false
        if (mealDate != singleString(
                db,
                "SELECT date FROM weight_logs WHERE note = 'Утренний замер' LIMIT 1"
            )
        ) return false

        return true
    }

    private fun count(db: SupportSQLiteDatabase, sql: String): Long {
        db.query(sql).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        }
    }

    private fun singleString(db: SupportSQLiteDatabase, sql: String): String? {
        db.query(sql).use { cursor ->
            if (!cursor.moveToFirst() || cursor.isNull(0)) return null
            return cursor.getString(0)
        }
    }
}
