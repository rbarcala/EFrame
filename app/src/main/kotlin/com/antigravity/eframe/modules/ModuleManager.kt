package com.antigravity.eframe.modules

import com.antigravity.eframe.data.AppSettings
import com.antigravity.eframe.data.FrameConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModuleManager(
    private val appSettings: AppSettings
) {
    val clockModule = ClockModule(
        getIsDetailed = { appSettings.config.value.isDetailedMode },
        getUiScale = { appSettings.config.value.uiScale }
    )

    val weatherModule = WeatherModule(
        getCityName = { appSettings.config.value.weatherCity },
        getLatitude = { appSettings.config.value.weatherLatitude },
        getLongitude = { appSettings.config.value.weatherLongitude },
        getIsDetailed = { appSettings.config.value.isDetailedMode },
        getUiScale = { appSettings.config.value.uiScale }
    )

    val transportModule = TransportModule(
        getSelectedLine = { appSettings.config.value.transportLine },
        getWaitingStation = { appSettings.config.value.transportStation },
        getDestination = { appSettings.config.value.transportDirection },
        getIsDetailed = { appSettings.config.value.isDetailedMode },
        getUiScale = { appSettings.config.value.uiScale }
    )

    val allModules: List<FrameModule> = listOf(
        clockModule,
        weatherModule,
        transportModule
    )

    private val _modulesState = MutableStateFlow(allModules)
    val modulesState: StateFlow<List<FrameModule>> = _modulesState.asStateFlow()

    init {
        syncWithConfig(appSettings.config.value)
    }

    fun syncWithConfig(config: FrameConfig) {
        clockModule.isEnabled = config.isClockEnabled
        weatherModule.isEnabled = config.isWeatherEnabled
        transportModule.isEnabled = config.isTransportEnabled
        _modulesState.value = allModules.toList()
    }

    fun getModuleAt(position: ModulePosition): FrameModule? {
        return allModules.find { it.defaultPosition == position && it.isEnabled }
    }
}
