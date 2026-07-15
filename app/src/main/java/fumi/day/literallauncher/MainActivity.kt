package fumi.day.literallauncher

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import fumi.day.literallauncher.ui.LauncherViewModel
import fumi.day.literallauncher.ui.LiteralLauncherApp

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LauncherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LauncherViewModel::class.java]
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawable(ColorDrawable(0))
        setContent {
            val showNavBar by viewModel.showNavBar.collectAsState()
            ApplySystemBarsVisibility(showNavBar)
            LiteralLauncherApp(viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        applySystemBarsVisibility(window, window.decorView, viewModel.showNavBar.value)
        viewModel.refreshApps()
    }
}

private fun applySystemBarsVisibility(window: Window, view: View, showNavBar: Boolean) {
    val controller = WindowCompat.getInsetsController(window, view)
    controller.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    controller.hide(WindowInsetsCompat.Type.statusBars())
    if (showNavBar) {
        controller.show(WindowInsetsCompat.Type.navigationBars())
    } else {
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

@Composable
private fun ApplySystemBarsVisibility(showNavBar: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current
    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        applySystemBarsVisibility(activity.window, view, showNavBar)
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
