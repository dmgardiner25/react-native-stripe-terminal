package com.acmmobile.stripe_terminal

import android.widget.Toast
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

import java.util.HashMap

/**
 * Created by Valdio Veliu on 17/01/2018.
 */

class StripeTerminalModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val DURATION_SHORT_KEY = "SHORT"
    private val DURATION_LONG_KEY = "LONG"

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
    fun show(message: String, duration: Int) {
        Toast.makeText(reactApplicationContext, message, duration).show()
    }
}