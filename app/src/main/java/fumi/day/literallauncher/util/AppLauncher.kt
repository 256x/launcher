package fumi.day.literallauncher.util

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import fumi.day.literallauncher.R

fun launchMuDirect(context: Context, pkg: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return
    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_NO_ANIMATION or
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    )
    val options = try {
        ActivityOptions.makeCustomAnimation(context, R.anim.stay_still, R.anim.stay_still)
    } catch (_: Exception) {
        ActivityOptions.makeCustomAnimation(context, 0, 0)
    }
    context.startActivity(intent, options.toBundle())
    if (context is Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                context.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.stay_still,
                    R.anim.stay_still
                )
            } catch (_: Exception) {
                @Suppress("DEPRECATION")
                context.overridePendingTransition(0, 0)
            }
        } else {
            @Suppress("DEPRECATION")
            context.overridePendingTransition(0, 0)
        }
    }
}

fun expandNotifications(context: Context) {
    try {
        val sbs = context.getSystemService("statusbar")
        val sbm = Class.forName("android.app.StatusBarManager")
        sbm.getMethod("expandNotificationsPanel").invoke(sbs)
    } catch (_: Exception) { }
}
