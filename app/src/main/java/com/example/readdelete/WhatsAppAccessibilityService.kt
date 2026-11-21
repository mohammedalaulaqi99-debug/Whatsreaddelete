package com.example.readdelete

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Feasibility-test monitor only.
 *
 * WhatsApp may expose no stable, machine-readable read-receipt state.
 * This service deliberately does NOT infer "read" from arbitrary text,
 * does NOT click message menus, and does NOT delete anything.
 */
class WhatsAppAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        // Diagnostic signal only. Do not log message contents or contact names.
        Log.d("ReadDeleteMonitor", "WhatsApp UI event type=${event.eventType}")
    }

    override fun onInterrupt() {
        Log.i("ReadDeleteMonitor", "Accessibility monitoring interrupted")
    }
}
