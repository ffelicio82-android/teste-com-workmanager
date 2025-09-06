package br.com.nukes.testeworkmanager.utils.extensions

import kotlin.math.ceil

fun Long.toMinutes(): Double {
    val minutes = this.toDouble() / 60000.0
    val roundedUp = ceil(minutes * 100) / 100
    return roundedUp
}