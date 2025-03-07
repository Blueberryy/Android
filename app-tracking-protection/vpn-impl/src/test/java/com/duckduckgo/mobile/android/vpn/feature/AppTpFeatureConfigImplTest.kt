/*
 * Copyright (c) 2022 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.mobile.android.vpn.feature

import com.duckduckgo.app.CoroutineTestRule
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.BuildFlavor
import com.duckduckgo.mobile.android.vpn.remote_config.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AppTpFeatureConfigImplTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private lateinit var config: AppTpFeatureConfigImpl
    private val appBuildConfig: AppBuildConfig = mock()
    private val vpnRemoteConfigDatabase: VpnRemoteConfigDatabase = mock()
    private lateinit var toggleDao: VpnConfigTogglesDao

    @Before
    fun setup() {
        toggleDao = FakeToggleConfigDao()
        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.PLAY)
        whenever(vpnRemoteConfigDatabase.vpnConfigTogglesDao()).thenReturn(toggleDao)

        config = AppTpFeatureConfigImpl(
            coroutineRule.testScope,
            appBuildConfig,
            vpnRemoteConfigDatabase,
            coroutineRule.testDispatcherProvider,
        )
    }

    @Test
    fun whenDbTablesAreEmptyThenReturnToggleDefaultValue() {
        AppTpSetting.values().forEach { setting ->
            when (setting) {
                AppTpSetting.AlwaysSetDNS -> assertFalse(config.isEnabled(setting))
                AppTpSetting.CPUMonitoring -> assertFalse(config.isEnabled(setting))
                AppTpSetting.ProtectGames -> assertFalse(config.isEnabled(setting))
                AppTpSetting.ExceptionLists -> assertTrue(config.isEnabled(setting))
                AppTpSetting.RestartOnConnectivityLoss -> assertTrue(config.isEnabled(setting))
            }
        }
    }

    @Test
    fun whenTogglesAreSetThenReturnedSetValue() {
        var enabled = true
        AppTpSetting.values().forEach {
            config.setEnabled(it, enabled)
            enabled = !enabled
        }

        enabled = true
        AppTpSetting.values().forEach {
            assertEquals(enabled, config.isEnabled(it))
            enabled = !enabled
        }
    }

    @Test
    fun whenDbTableIsPrepopulatedThenLoadInMemoryCache() = runTest {
        AppTpSetting.values().forEach {
            toggleDao.insert(VpnConfigToggle(it.value, enabled = true, isManualOverride = false))
        }

        val config = AppTpFeatureConfigImpl(
            coroutineRule.testScope,
            appBuildConfig,
            vpnRemoteConfigDatabase,
            coroutineRule.testDispatcherProvider,
        )

        AppTpSetting.values().forEach {
            assertTrue(config.isEnabled(it))
        }
    }

    @Test
    fun whenNotInternalBuildThenNeverSetManualOverride() {
        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.PLAY)

        config.setEnabled(SettingName { "feature" }, true, isManualOverride = true)

        val toggle = toggleDao.getConfigToggles().first()
        assertEquals("feature", toggle.name)
        assertFalse(toggle.isManualOverride)
    }

    @Test
    fun whenInternalBuildThenRespectManualOverride() {
        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.INTERNAL)

        config.setEnabled(SettingName { "feature" }, true, isManualOverride = true)

        val toggle = toggleDao.getConfigToggles().first()
        assertEquals("feature", toggle.name)
        assertTrue(toggle.isManualOverride)
    }

    @Test
    fun whenInternalBuildThenProperlyHandleManualOverrides() {
        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.INTERNAL)

        config.setEnabled(AppTpSetting.CPUMonitoring, true, isManualOverride = true)
        config.setEnabled(AppTpSetting.CPUMonitoring, false, isManualOverride = true)

        config.setEnabled(AppTpSetting.AlwaysSetDNS, true, isManualOverride = false)
        config.setEnabled(AppTpSetting.AlwaysSetDNS, false, isManualOverride = true)

        assertFalse(config.isEnabled(AppTpSetting.CPUMonitoring))
        assertFalse(config.isEnabled(AppTpSetting.AlwaysSetDNS))
    }

    @Test
    fun whenNotInternalBuildThenAlwaysOverride() {
        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.PLAY)

        config.setEnabled(AppTpSetting.CPUMonitoring, true, isManualOverride = true)
        config.setEnabled(AppTpSetting.CPUMonitoring, false, isManualOverride = true)

        config.setEnabled(AppTpSetting.AlwaysSetDNS, true, isManualOverride = false)
        config.setEnabled(AppTpSetting.AlwaysSetDNS, false, isManualOverride = true)

        assertFalse(config.isEnabled(AppTpSetting.CPUMonitoring))
        assertFalse(config.isEnabled(AppTpSetting.AlwaysSetDNS))
    }
}
