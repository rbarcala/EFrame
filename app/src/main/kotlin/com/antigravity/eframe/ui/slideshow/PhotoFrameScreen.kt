package com.antigravity.eframe.ui.slideshow

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.antigravity.eframe.data.AppSettings
import com.antigravity.eframe.data.DriveRepository
import com.antigravity.eframe.data.TransportRepository
import com.antigravity.eframe.modules.ModuleManager
import com.antigravity.eframe.modules.ModulePosition
import com.antigravity.eframe.sensor.ProximitySensorManager
import com.antigravity.eframe.ui.settings.SettingsDialog
import com.antigravity.eframe.ui.transport.SubteDetailModal
import com.antigravity.eframe.ui.weather.WeatherDetailModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NativePhotoPresenter(
    private val frameLayout: FrameLayout,
    private val viewA: ImageView,
    private val viewB: ImageView,
    private val scrimView: View
) {
    private var isUsingA = true

    fun crossfadeTo(drawable: Drawable, durationMs: Long = 400) {
        val incoming = if (isUsingA) viewB else viewA
        val outgoing = if (isUsingA) viewA else viewB

        incoming.setImageDrawable(drawable)
        incoming.visibility = View.VISIBLE
        incoming.alpha = 0f

        incoming.animate()
            .alpha(1f)
            .setDuration(durationMs)
            .withLayer()
            .start()

        outgoing.animate()
            .alpha(0f)
            .setDuration(durationMs)
            .withLayer()
            .withEndAction {
                outgoing.visibility = View.GONE
                outgoing.setImageDrawable(null) // free memory immediately
            }
            .start()

        isUsingA = !isUsingA
    }

    fun setDimmed(dimmed: Boolean) {
        scrimView.animate()
            .alpha(if (dimmed) 0.98f else 0f)
            .setDuration(350)
            .withLayer()
            .start()
    }
}

@Composable
fun PhotoFrameScreen(
    appSettings: AppSettings,
    driveRepository: DriveRepository = remember { DriveRepository() },
    transportRepository: TransportRepository = remember { TransportRepository() }
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val config by appSettings.config.collectAsState()
    val moduleManager = remember { ModuleManager(appSettings) }

    // Sync module manager with live config
    LaunchedEffect(config) {
        moduleManager.syncWithConfig(config)
    }

    var photos by remember { mutableStateOf(driveRepository.samplePhotos) }
    var currentPhotoIndex by remember { mutableIntStateOf(0) }
    var isLoadingPhotos by remember { mutableStateOf(false) }

    // Inactivity & Screen Dimming state
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isScreenDimmed by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showWeatherModal by remember { mutableStateOf(false) }
    var showSubteModal by remember { mutableStateOf(false) }
    var showProximityBadge by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Screen dimensions for downsampling bitmaps
    val metrics = remember { context.resources.displayMetrics }
    val screenWidth = remember { metrics.widthPixels }
    val screenHeight = remember { metrics.heightPixels }

    // Native presenter holder
    var presenter by remember { mutableStateOf<NativePhotoPresenter?>(null) }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .allowRgb565(true)
            .build()
    }

    val isAnyModalOpen = showSettingsDialog || showWeatherModal || showSubteModal

    // Wake-up function: restores brightness and dismisses black scrim
    fun wakeUpScreen() {
        lastInteractionTime = System.currentTimeMillis()
        if (isScreenDimmed) {
            isScreenDimmed = false
            presenter?.setDimmed(false)
            activity?.window?.let { window ->
                val lp = window.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = lp
            }
        }
    }

    // Keep screen awake while modal is open
    LaunchedEffect(isAnyModalOpen) {
        if (isAnyModalOpen) {
            wakeUpScreen()
        }
    }

    // Load and decode single photo optimized to exact display dimensions
    suspend fun loadPhotoDrawable(url: String): Drawable? = withContext(Dispatchers.IO) {
        try {
            val req = ImageRequest.Builder(context)
                .data(url)
                .size(screenWidth, screenHeight)
                .scale(Scale.FILL)
                .precision(Precision.EXACT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .allowRgb565(true)
                .build()
            imageLoader.execute(req).drawable
        } catch (e: Exception) {
            null
        }
    }

    // Reload photos from Drive
    fun reloadPhotos() {
        coroutineScope.launch {
            if (config.driveFolderUrl.isNotBlank()) {
                isLoadingPhotos = true
                val result = driveRepository.fetchPhotosFromFolder(
                    folderUrlOrId = config.driveFolderUrl,
                    apiKey = config.driveApiKey
                )
                isLoadingPhotos = false
                if (result.isSuccess && result.getOrThrow().isNotEmpty()) {
                    photos = result.getOrThrow()
                    currentPhotoIndex = 0
                }
            }
        }
    }

    LaunchedEffect(config.driveFolderUrl) {
        if (config.driveFolderUrl.isNotBlank()) {
            reloadPhotos()
        }
    }

    // Proximity Sensor setup with low CPU delay
    DisposableEffect(config.proximityWakeEnabled) {
        if (!config.proximityWakeEnabled) {
            return@DisposableEffect onDispose {}
        }

        val sensorManager = ProximitySensorManager(context) {
            coroutineScope.launch {
                showProximityBadge = true
                wakeUpScreen()
                delay(1200)
                showProximityBadge = false
            }
        }
        sensorManager.startListening()

        onDispose {
            sensorManager.stopListening()
        }
    }

    // Keep screen on
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Inactivity timeout watcher: sleeps efficiently until timeout
    LaunchedEffect(config.inactivityTimeoutSeconds, lastInteractionTime, isScreenDimmed, isAnyModalOpen) {
        val timeoutSec = config.inactivityTimeoutSeconds
        if (timeoutSec > 0 && !isScreenDimmed && !isAnyModalOpen) {
            val elapsed = System.currentTimeMillis() - lastInteractionTime
            val remaining = (timeoutSec * 1000L) - elapsed
            if (remaining > 0) {
                delay(remaining)
            }
            if (!isAnyModalOpen && System.currentTimeMillis() - lastInteractionTime >= timeoutSec * 1000L) {
                isScreenDimmed = true
                presenter?.setDimmed(true)
                activity?.window?.let { window ->
                    val lp = window.attributes
                    lp.screenBrightness = 0.005f
                    window.attributes = lp
                }
            }
        }
    }

    // Initial photo load when presenter is attached
    LaunchedEffect(presenter, photos) {
        if (presenter != null && photos.isNotEmpty()) {
            val d = loadPhotoDrawable(photos[currentPhotoIndex].url)
            if (d != null) {
                presenter?.crossfadeTo(d, durationMs = 0)
            }
        }
    }

    // Slideshow rotation: native 60fps crossfade
    LaunchedEffect(photos, config.slideDurationSeconds, isScreenDimmed, isAnyModalOpen, presenter) {
        if (photos.isEmpty() || isScreenDimmed || isAnyModalOpen || presenter == null) return@LaunchedEffect

        val durationMs = config.slideDurationSeconds * 1000L
        while (true) {
            delay(durationMs)
            if (photos.size > 1 && !isAnyModalOpen && !isScreenDimmed) {
                val nextIndex = (currentPhotoIndex + 1) % photos.size
                currentPhotoIndex = nextIndex

                // Fetch and decode next photo
                val nextDrawable = loadPhotoDrawable(photos[nextIndex].url)
                if (nextDrawable != null) {
                    presenter?.crossfadeTo(nextDrawable, durationMs = 450)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { wakeUpScreen() },
                    onDoubleTap = {
                        wakeUpScreen()
                        showSettingsDialog = true
                    }
                )
            }
    ) {
        // LAYER 1: 100% Native Hardware-Accelerated Dual ImageView Canvas
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val root = FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                }

                val ivA = ImageView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                val ivB = ImageView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    visibility = View.GONE
                }

                // Ambient vignette layer
                val vignette = View(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.argb(38, 0, 0, 0)) // 15% black
                }

                // Hardware-accelerated sleep scrim
                val scrim = View(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    alpha = 0f
                }

                root.addView(ivA)
                root.addView(ivB)
                root.addView(vignette)
                root.addView(scrim)

                presenter = NativePhotoPresenter(root, ivA, ivB, scrim)
                root
            }
        )

        // LAYER 2: MODULE OVERLAYS (Visible only when awake)
        AnimatedVisibility(
            visible = !isScreenDimmed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top-Start: Clock & Date Module
                moduleManager.getModuleAt(ModulePosition.TOP_START)?.let { mod ->
                    mod.Content(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 24.dp, top = 28.dp),
                        onClick = { wakeUpScreen() }
                    )
                }

                // Top-End: Weather Module (Click to show hourly forecast & refresh live!)
                moduleManager.getModuleAt(ModulePosition.TOP_END)?.let { mod ->
                    mod.Content(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 24.dp, top = 28.dp),
                        onClick = {
                            wakeUpScreen()
                            coroutineScope.launch {
                                moduleManager.weatherModule.refreshWeather()
                            }
                            showWeatherModal = true
                        }
                    )
                }

                // Bottom-Start: Transport / Subte Module (Click to show full subte network!)
                moduleManager.getModuleAt(ModulePosition.BOTTOM_START)?.let { mod ->
                    mod.Content(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp, bottom = 28.dp),
                        onClick = {
                            wakeUpScreen()
                            showSubteModal = true
                        }
                    )
                }

                // Settings Gear Button (Top-Center / Subtle)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 28.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    IconButton(
                        onClick = {
                            wakeUpScreen()
                            showSettingsDialog = true
                        },
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Proximity sensor wake notification badge
        AnimatedVisibility(
            visible = showProximityBadge,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.80f),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "👋 Sensor de proximidad detectado",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        // Photo loading indicator
        if (isLoadingPhotos) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Configuration Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                initialConfig = config,
                transportRepository = transportRepository,
                onDismiss = { showSettingsDialog = false },
                onSaveConfig = { updated ->
                    appSettings.updateConfig(updated)
                },
                onRefreshPhotos = {
                    reloadPhotos()
                }
            )
        }

        // Fullscreen Weather Hourly & 7-Day Modal
        if (showWeatherModal) {
            val weatherData = moduleManager.weatherModule.currentWeather.collectAsState().value
            val lastUpdated = moduleManager.weatherModule.lastUpdatedTime.collectAsState().value
            if (weatherData != null) {
                WeatherDetailModal(
                    weather = weatherData,
                    lastUpdatedTime = lastUpdated,
                    onRefreshNow = {
                        moduleManager.weatherModule.refreshWeather()
                    },
                    onDismiss = { showWeatherModal = false }
                )
            }
        }

        // Fullscreen Subte Network & Stations Modal
        if (showSubteModal) {
            SubteDetailModal(
                repository = transportRepository,
                userConfiguredLineName = config.transportLine,
                userWaitingStation = config.transportStation,
                onDismiss = { showSubteModal = false }
            )
        }
    }
}
