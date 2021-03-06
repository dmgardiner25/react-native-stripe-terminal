package com.acmmobile

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.stripe.stripeterminal.*
import com.facebook.react.bridge.ReactApplicationContext
import android.util.Log

class MainActivity2 : AppCompatActivity(), NavigationListener, TerminalStateManager,
        ReaderDisplayListener, ReaderSoftwareUpdateListener {

    companion object {

        // The code that denotes the request for location permissions
        private const val REQUEST_CODE_LOCATION = 1

        // A string to store if the simulated switch is set
        private const val SIMULATED_SWITCH_VALUE = "com.stripe.example.simulated"
    }

    private var collectCancelable: Cancelable? = null
    private var discoveryCancelable: Cancelable? = null
    private var readerUpdate: ReaderSoftwareUpdate? = null
    private var simulated: Boolean = false

    /**
     * Upon starting, we should verify we have the permissions we need, then start the app
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check that the example app has been configured correctly
        /*if (ApiClient.BACKEND_URL.isEmpty()) {
            throw RuntimeException("You need to set the BACKEND_URL constant in ApiClient.kt " +
                    "before you'll be able to use the example app.")
        }*/

        // Initialize the simulated flag
        if (savedInstanceState != null) {
            simulated = savedInstanceState.getBoolean(SIMULATED_SWITCH_VALUE, false)
        }

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //safeInitialize()
        } else {
            // If we don't have them yet, request them before doing anything else
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
        }
    }

    /**
     * Make sure to save the state of the simulated switch when necessary
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(SIMULATED_SWITCH_VALUE, simulated)
        super.onSaveInstanceState(outState)
    }

    /**
     * Receive the result of our permissions check, and initialize if we can
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // If we receive a response to our permission check, initialize
        if (requestCode == REQUEST_CODE_LOCATION) {
            //init()
        }
    }

    // Navigation callbacks

    /**
     * Callback function called when there's been a request to cancel collect payment method
     */
    override fun onRequestCancelCollectPaymentMethod() {
        collectCancelable?.cancel(CollectPaymentMethodCancellationCallback(this))
    }

    /**
     * Callback function called when there's been a request to cancel discovery
     */
    override fun onRequestCancelDiscovery() {
        discoveryCancelable?.cancel(DiscoveryCancellationCallback(this))
    }

    /**
     * Callback function called when there's been a request to check for updates
     */
    override fun onRequestCheckForUpdate() {
        Terminal.getInstance().checkForUpdate(CheckForUpdateCallback(this))
    }

    /**
     * Callback function called once disconnect has been selected by the [ConnectedReaderFragment]
     */
    override fun onRequestDisconnect() {
        Terminal.getInstance().disconnectReader(DisconnectCallback(this))
    }

    /**
     * Callback function called once discovery has been selected by the [TerminalFragment]
     */
    override fun onRequestDiscovery() {
        val fragment = DiscoveryFragment()
        navigateTo(fragment)
        discoveryCancelable = Terminal.getInstance().discoverReaders(DiscoveryConfiguration(0,
                DeviceType.CHIPPER_2X, simulated), fragment, DiscoveryCallback(this))
    }

    /**
     * Callback function called to exit the payment workflow
     */
    override fun onRequestExitWorkflow() {
        if (Terminal.getInstance().connectionStatus == ConnectionStatus.CONNECTED) {
            navigateTo(ConnectedReaderFragment())
        } else {
            navigateTo(TerminalFragment())
        }
    }

    /**
     * Callback function called to start installation of a reader software update
     */
    override fun onRequestInstallUpdate() {
        if (readerUpdate != null) {
            Terminal.getInstance().installUpdate(readerUpdate!!, this, InstallUpdateCallback(this))
        }
    }

    /**
     * Callback function called to start a payment by the [PaymentFragment]
     */
    override fun onRequestPayment(amount: Int, currency: String) {
        val params = PaymentIntentParameters.Builder()
                .setAmount(amount)
                .setCurrency(currency.toLowerCase())
                .build()
        navigateTo(EventFragment())
        Terminal.getInstance().createPaymentIntent(params, CreatePaymentIntentCallback(this))
    }

    /**
     * Callback function called once the payment workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectPaymentWorkflow() {
        navigateTo(PaymentFragment())
    }

    /**
     * Callback function called once a [Reader] has been selected by the [DiscoveryFragment]
     */
    override fun onSelectReader(reader: Reader) {
        navigateTo(ConnectingFragment())
        Terminal.getInstance().connectReader(reader, ConnectionCallback(this))
    }

    /**
     * Callback function called once the read card workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectReadReusableCardWorkflow() {
        navigateTo(EventFragment())
        Terminal.getInstance().readReusableCard(ReadReusableCardParameters.NULL, this,
                ReadReusableCardCallback(this))
    }

    /**
     * Callback function called once the update reader workflow has been selected by the
     * [ConnectedReaderFragment]
     */
    override fun onSelectUpdateWorkflow() {
        navigateTo(UpdateReaderFragment())
    }

    /**
     * Callback function called when the simulated switch has been toggled in the [TerminalFragment]
     */
    override fun onToggleSimulatedSwitch(isOn: Boolean) {
        simulated = isOn
    }

    // Terminal event callbacks

    /**
     * Callback function called when collect payment method has been canceled
     */
    override fun onCancelCollectPaymentMethod() {
        collectCancelable = null
        navigateTo(ConnectedReaderFragment())
    }

    /**
     * Callback function called when discovery has been canceled
     */
    override fun onCancelDiscovery() {
        discoveryCancelable = null
        navigateTo(TerminalFragment().setSimulatedSwitch(simulated))
    }

    /**
     * Callback function called on completion of [Terminal.collectPaymentMethod]
     */
    override fun onCollectPaymentMethod(paymentIntent: PaymentIntent) {
        displayEvent("Collected PaymentMethod", "terminal.collectPaymentMethod")
        Terminal.getInstance().processPayment(paymentIntent, ProcessPaymentCallback(this))
        collectCancelable = null
    }

    /**
     * Callback function called on completion of [Terminal.connectReader]
     */
    override fun onConnectReader() {
        navigateTo(ConnectedReaderFragment())
    }

    /**
     * Callback function called on completion of [Terminal.createPaymentIntent]
     */
    override fun onCreatePaymentIntent(paymentIntent: PaymentIntent) {
        displayEvent("Created PaymentIntent", "terminal.createPaymentIntent")
        collectCancelable = Terminal.getInstance().collectPaymentMethod(paymentIntent, this,
                CollectPaymentMethodCallback(this))
    }

    /**
     * Callback function called on completion of [Terminal.disconnectReader]
     */
    override fun onDisconnectReader() {
        navigateTo(TerminalFragment().setSimulatedSwitch(simulated))
    }

    /**
     * Callback function called on completion of [Terminal.discoverReaders]
     */
    override fun onDiscoverReaders() {
        discoveryCancelable = null
    }

    /**
     * Callback function called whenever a [Terminal] method fails
     */
    override fun onFailure(e: TerminalException) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is EventFragment) {
            displayEvent(e.errorMessage, e.errorCode.toString())
            runOnUiThread {
                fragment.completeFlow()
            }
        } else if (fragment is ConnectingFragment) {
            navigateTo(TerminalFragment())
        }
    }

    /**
     * Callback function called on completion of [Terminal.installUpdate]
     */
    override fun onInstallReaderSoftwareUpdate() {
        // Tell the UpdateReaderFragment that the update flow has completed
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is UpdateReaderFragment) {
            runOnUiThread {
                fragment.onCompleteUpdate()
            }
        }
    }

    /**
     * Callback function called on completion of [Terminal.processPayment]
     */
    override fun onProcessPayment(paymentIntent: PaymentIntent) {
        displayEvent("Processed payment", "terminal.processPayment")
        ApiClient.capturePaymentIntent(paymentIntent.id!!)
        displayEvent("Captured PaymentIntent", "backend.capturePaymentIntent")

        // Tell the EventFragment that the flow has completed
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is EventFragment) {
            runOnUiThread {
                fragment.completeFlow()
            }
        }
    }

    /**
     * Callback function called on completion of [Terminal.readReusableCard]
     */
    override fun onReadReusableCard(paymentMethod: PaymentMethod) {
        displayEvent("Created PaymentMethod: ${paymentMethod.id}",
                "terminal.readReusableCard")

        // Tell the EventFragment that the flow has completed
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is EventFragment) {
            runOnUiThread {
                fragment.completeFlow()
            }
        }
    }

    /**
     * Callback function called on completion of [Terminal.checkForUpdate]
     */
    override fun onReturnReaderSoftwareUpdate(update: ReaderSoftwareUpdate?) {
        readerUpdate = update

        // Tell the UpdateReaderFragment that an update is available
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is UpdateReaderFragment) {
            runOnUiThread {
                fragment.onUpdateAvailable(update)
            }
        }
    }

    // Reader output callbacks

    /**
     * Callback function called when the [Reader] needs to display a message
     */
    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        displayEvent(message.toString(), "listener.onRequestReaderDisplayMessage")
    }

    /**
     * Callback function called when the [Reader] is ready for input
     */
    override fun onRequestReaderInput(options: ReaderInputOptions) {
        displayEvent(options.toString(), "listener.onRequestReaderInput")
    }

    // Reader software update callbacks

    override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
        // Tell the UpdateReaderFragment about the progress
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is UpdateReaderFragment) {
            runOnUiThread {
                fragment.onUpdateProgress(progress)
            }
        }
    }

    /**
     * Display [Terminal] events in the EventFragment if it's visible
     */
    private fun displayEvent(message: String, method: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if (fragment is EventFragment) {
            runOnUiThread {
                fragment.displayEvent(message, method)
            }
        }
    }

    /**
     * A version of initialize that is safe to call multiple times, and can be called
     * even if the Terminal has been initialized previously
     */
    @TargetApi(Build.VERSION_CODES.CUR_DEVELOPMENT)
    fun safeInitialize(URL: String, reactContext: ReactApplicationContext) {
        try {
            init(URL, reactContext)
            // Check for location permissions
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                // If we don't have them yet, request them before doing anything else
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                //reactContext.currentActivity.requestPermissions(this, permissions, REQUEST_CODE_LOCATION)
            }
            // Terminal has already been initialized, ignore...*/
        } catch (e: IllegalStateException) {
            // Terminal has not been initialized, so do so now
            //init("https://stripeterminalbackendtest.herokuapp.com")
        }
    }

    /**
     * Initialize the [Terminal] and go to the [TerminalFragment]
     */
    private fun init(URL: String, reactContext: ReactApplicationContext) {
        // Initialize the Terminal as soon as possible
        try {
            val applicationContext = reactContext.getApplicationContext()
            Terminal.initTerminal(applicationContext, LogLevel.VERBOSE, TokenProvider(URL),
                    TerminalEventListener())
        } catch (e: TerminalException) {
            throw RuntimeException("Location services are required in order to initialize " +
                    "the Terminal.", e)
        }

        navigateTo(TerminalFragment().setSimulatedSwitch(simulated))
    }

    /**
     * Navigate to the given fragment.
     *
     * @param fragment Fragment to navigate to.
     */
    fun navigateTo(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }
}
