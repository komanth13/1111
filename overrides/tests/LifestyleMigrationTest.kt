package com.example.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LifestyleMigrationTest {
    @Test fun upgradePreservesProfileAndDiaryAndStoresLifestyle() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "lifestyle-migration-test"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        DatabaseMigrations.MIGRATION_1_2.migrate(db)
                        db.execSQL("""INSERT INTO user_profile VALUES
                            (1, 'Test', 'FEMALE', 37, 168, 105.5, 105.5, 89, 'ACTIVE', 'RECOMMENDED',
                            2700, 190, 84, 296, 3000, 16, 0, 0)""")
                        db.execSQL("""INSERT INTO meal_entries VALUES
                            (1, '2026-10-04', 'LUNCH', 'Борщ', 300, 180, 9, 6, 24, 0)""")
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                }).build())
        helper.writableDatabase
        helper.close()
        val database = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(*DatabaseMigrations.ALL).allowMainThreadQueries().build()
        try {
            // Opening Room validates the migrated schema against the generated v4 entity schema.
            database.openHelper.writableDatabase.query("SELECT * FROM user_profile").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(2700, c.getInt(c.getColumnIndexOrThrow("dailyCalorieTarget")))
                assertTrue(c.isNull(c.getColumnIndexOrThrow("averageDailySteps")))
                assertTrue(c.isNull(c.getColumnIndexOrThrow("householdMinutes")))
            }
            assertEquals("Борщ", database.mealDao().getMealsForDateSync("2026-10-04").single().foodName)
            database.openHelper.writableDatabase.execSQL(
                "UPDATE user_profile SET averageDailySteps = 6000, householdMinutes = 45 WHERE id = 1")
            database.openHelper.writableDatabase.query("SELECT averageDailySteps, householdMinutes FROM user_profile").use { c ->
                assertTrue(c.moveToFirst()); assertEquals(6000, c.getInt(0)); assertEquals(45, c.getInt(1))
            }
        } finally { database.close(); context.deleteDatabase(name) }
    }
}
