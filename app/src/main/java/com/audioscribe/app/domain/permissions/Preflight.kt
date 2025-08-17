package com.audioscribe.app.domain.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.Manifest.permission.RECORD_AUDIO

sealed class PreflightIssue {
	data object MicPermission : PreflightIssue()
	data object AccessibilityOff : PreflightIssue()
	data object OverlayOff : PreflightIssue()
	data object BatteryOptimized : PreflightIssue()
}

// Feature flags – set to true if your build requires these services
private const val requiresAccessibility = false
private const val requiresOverlay = false
private const val requiresDontOptimize = false

fun checkPreflight(ctx: Context): List<PreflightIssue> {
	val issues = mutableListOf<PreflightIssue>()
	if (ContextCompat.checkSelfPermission(ctx, RECORD_AUDIO) != PERMISSION_GRANTED)
		issues += PreflightIssue.MicPermission
	if (requiresAccessibility && !isAccessibilityEnabled(ctx))
		issues += PreflightIssue.AccessibilityOff
	if (requiresOverlay && !Settings.canDrawOverlays(ctx))
		issues += PreflightIssue.OverlayOff
	if (requiresDontOptimize && !isIgnoringBatteryOptimizations(ctx))
		issues += PreflightIssue.BatteryOptimized
	return issues
}

@Suppress("UNUSED_PARAMETER")
private fun isAccessibilityEnabled(ctx: Context): Boolean {
	// Not required for this app currently; return true by default
	return true
}

private fun isIgnoringBatteryOptimizations(ctx: Context): Boolean {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
		pm.isIgnoringBatteryOptimizations(ctx.packageName)
	} else true
}

// Helpers to open relevant settings; callers should handle results and re-check issues
fun intentFor(issue: PreflightIssue, ctx: Context): Intent? = when (issue) {
	PreflightIssue.MicPermission -> null // handled via runtime permission launcher
	PreflightIssue.AccessibilityOff -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
	PreflightIssue.OverlayOff -> Intent(
		Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
		Uri.parse("package:${ctx.packageName}")
	)
	PreflightIssue.BatteryOptimized -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
			data = Uri.parse("package:${ctx.packageName}")
		}
	} else null
}
