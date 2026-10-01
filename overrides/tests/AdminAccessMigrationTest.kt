package com.example.security

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.BuildConfig
import com.example.data.db.AppDatabase
import com.example.data.model.AdminPaymentDetails
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.SubscriptionTier
import com.example.data.repository.AdminConfigManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdminAccessMigrationTest {
    private lateinit var context: Context
    @Before fun before() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("slimtrack_admin_config_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        SecurityPolicy.updateAccountAccess(AccountAccess())
    }

    @Test fun debugApkDoesNotGrantAdministratorOrSimulation() {
        assertTrue(BuildConfig.DEBUG)
        assertFalse(SecurityPolicy.adminToolsEnabled)
        assertFalse(SecurityPolicy.localTierSimulationEnabled)
        assertFalse(SecurityPolicy.localPromoActivationEnabled)
        assertEquals(SubscriptionTier.FREE, AdminConfigManager(context).userTier.value)
    }

    @Test fun previousAdminTierAndGuestInvitesCannotSurviveUpgrade() {
        val prefs = context.getSharedPreferences("slimtrack_admin_config_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("active_subscription_tier", SubscriptionTier.ADMIN.id)
            .putString("active_guest_access_data", "{\"tier\":\"premium\"}")
            .putString("saved_guest_invites_list", "[]")
            .putString("saved_app_config_json", "{}")
            .putString("saved_config_source_name", "old-debug")
            .putString("admin_payment_details_data", "{}")
            .commit()
        val manager = AdminConfigManager(context)
        assertEquals(SubscriptionTier.FREE, manager.userTier.value)
        assertNull(manager.activeGuestAccess.value)
        assertTrue(manager.savedInvites.value.isEmpty())
        assertFalse(prefs.contains("active_subscription_tier"))
        assertFalse(prefs.contains("saved_app_config_json"))
        assertTrue(prefs.contains("admin_payment_details_data"))
    }

    @Test fun privilegedMethodsAndPublicPaywallCannotElevateAnonymousUser() {
        val manager = AdminConfigManager(context)
        manager.selectTierFromPaywall(SubscriptionTier.ADMIN)
        manager.selectTierFromPaywall(SubscriptionTier.PREMIUM)
        assertEquals(SubscriptionTier.FREE, manager.userTier.value)
        assertThrows(IllegalStateException::class.java) { manager.setUserTierForDebug(SubscriptionTier.ADMIN) }
        assertThrows(IllegalStateException::class.java) { manager.createInvite(SubscriptionTier.PREMIUM, 7, "test") }
        assertThrows(IllegalStateException::class.java) { manager.updatePaymentDetails(AdminPaymentDetails()) }
        assertThrows(IllegalStateException::class.java) { manager.exportConfigJson() }
        assertTrue(manager.applyJsonConfig("{}", "test").isFailure)
        assertTrue(manager.activateCodeOrUrl("slimtrack://invite?code=FAMILY-VIP&tier=admin").isFailure)
    }

    @Test fun accessMigrationDoesNotDeleteNutritionDiary() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
            val entry = MealEntry(date = "2026-10-01", mealType = MealType.LUNCH, foodName = "Борщ", grams = 300f,
                calories = 180f, protein = 9f, fat = 6f, carbs = 24f)
            database.mealDao().insertMeal(entry)
            AdminConfigManager(context)
            val saved = database.mealDao().getMealsForDateSync("2026-10-01").single()
            assertEquals("Борщ", saved.foodName)
            assertEquals(180f, saved.calories, 0.01f)
        } finally { database.close() }
    }
}
