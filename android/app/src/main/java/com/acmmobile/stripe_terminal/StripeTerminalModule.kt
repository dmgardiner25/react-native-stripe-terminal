package com.acmmobile.stripe_terminal

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.acmmobile.MainActivity2

import java.util.HashMap

/**
 * Created by Valdio Veliu on 17/01/2018.
 */

class StripeTerminalModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val DURATION_SHORT_KEY = "SHORT"
    private val DURATION_LONG_KEY = "LONG"
    private val context = reactContext

    override fun getName(): String {
        return "StripeTerminalModule"
    }

    override fun getConstants(): kotlin.collections.Map<String, Any> {
        val constants = HashMap<String, Any>()
        constants.put(DURATION_SHORT_KEY, Toast.LENGTH_SHORT)
        constants.put(DURATION_LONG_KEY, Toast.LENGTH_LONG)
        return constants
    }

    @ReactMethod
    fun initialize(URL: String) {
        var activity = MainActivity2()
        activity.safeInitialize(URL, context)
    }
}