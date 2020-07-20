package com.twilio.audioswitch.bluetooth

import com.nhaarman.mockitokotlin2.mock
import com.twilio.audioswitch.createHeadset
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test

class BluetoothHeadsetCacheManagerTest {

    private var cacheManager = BluetoothHeadsetCacheManager(mock())

    @Test
    fun `remove removes an existing headset from the cache`() {
        val headset = createHeadset("Headset")
        cacheManager.add(headset)

        cacheManager.remove(headset)

        assertThat(cacheManager.cachedHeadsets.isEmpty(), equalTo(true))
    }

    @Test
    fun `clear should remove all of the headsets from the cache`() {
        val headset1 = createHeadset("Headset 1")
        val headset2 = createHeadset("Headset 2")
        cacheManager.add(headset1)
        cacheManager.add(headset2)
        assertThat(cacheManager.cachedHeadsets.size, equalTo(2))

        cacheManager.clear()

        assertThat(cacheManager.cachedHeadsets.isEmpty(), equalTo(true))
    }

    @Test
    fun `activeHeadset should return the last cached headset`() {
        val headset1 = createHeadset("Headset 1")
        val headset2 = createHeadset("Headset 2")
        cacheManager.add(headset1)
        cacheManager.add(headset2)
        assertThat(cacheManager.cachedHeadsets.size, equalTo(2))

        assertThat(cacheManager.activeHeadset, equalTo(headset2))
    }

    @Test
    fun `activeHeadset should return null if there are no cached headsets`() {
        assertThat(cacheManager.activeHeadset, `is`(nullValue()))
    }
}
