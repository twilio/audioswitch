package com.twilio.audioswitch.comparators

import com.twilio.audioswitch.AudioDevice

class AudioDevicePriorityComparator(val order: List<Class<out AudioDevice>>) : Comparator<AudioDevice> {
    override fun compare(o1: AudioDevice?, o2: AudioDevice?): Int {
        if (o1 == null && o2 == null) {
            return 0
        }
        if (o1 == null) {
            return -1
        }
        if (o2 == null) {
            return 1
        }

        val o1Clazz = o1.javaClass
        val o2Clazz = o2.javaClass
        if (o1Clazz == o2Clazz) {
            return 0
        }

        val clazz = this.order.find { it == o1Clazz || it == o2Clazz }
        if (clazz == o1Clazz) {
            return -1
        }
        return 1
    }
}