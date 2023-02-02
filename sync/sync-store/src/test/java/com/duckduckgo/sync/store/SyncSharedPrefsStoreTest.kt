/*
 * Copyright (c) 2023 DuckDuckGo
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

package com.duckduckgo.sync.store

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncSharedPrefsStoreTest {
    private lateinit var store: SyncSharedPrefsStore
    private val sharedPrefsProvider =
        TestSharedPrefsProvider(InstrumentationRegistry.getInstrumentation().context)

    @Before
    fun setUp() {
        store = SyncSharedPrefsStore(sharedPrefsProvider)
    }

    @Test
    fun whenUserIdStoredThenValueUpdatedInPrefsStore() {
        assertNull(store.userId)
        store.userId = "test_user"
        assertEquals("test_user", store.userId)
        store.userId = null
        assertNull(store.userId)
    }

    @Test
    fun whenDeviceNameStoredThenValueUpdatedInPrefsStore() {
        assertNull(store.deviceName)
        store.deviceName = "test_device"
        assertEquals("test_device", store.deviceName)
        store.deviceName = null
        assertNull(store.deviceName)
    }

    @Test
    fun whenDeviceIdStoredThenValueUpdatedInPrefsStore() {
        assertNull(store.deviceId)
        store.deviceId = "test_device_id"
        assertEquals("test_device_id", store.deviceId)
        store.deviceId = null
        assertNull(store.deviceId)
    }
}
