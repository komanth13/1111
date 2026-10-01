package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainScreen
import com.example.ui.theme.SlimTrackTheme
import com.example.ui.viewmodel.FitnessViewModel
import com.example.ui.viewmodel.FitnessViewModelFactory
import com.example.util.LocalizationManager
import com.example.util.LocalizationProvider
import com.example.util.StringKey

class MainActivity : ComponentActivity() {
    private val appContainer get() = (application as SlimTrackApplication).container
    private val adminConfigManager get() = appContainer.adminConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingInvite(intent)


        setContent {
            SlimTrackTheme {
                LocalizationProvider {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val viewModel: FitnessViewModel = viewModel(
                            factory = FitnessViewModelFactory(appContainer.fitnessRepository, adminConfigManager, appContainer.accountManager)
                        )
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appContainer.accountManager.refresh()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingInvite(intent)
    }

    private fun handleIncomingInvite(intent: Intent?) {
        val uri = intent?.data ?: return
        try {
            val result = adminConfigManager.activateCodeOrUrl(uri.toString())
            result.onSuccess { access ->
                Toast.makeText(
                    this,
                    "🎉 ${LocalizationManager.getString(StringKey.SUCCESS)}: ${access.label}",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    this,
                    error.message ?: LocalizationManager.getString(StringKey.PROMO_INVALID_CODE),
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
