package org.yourproject.wallet.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun info(message: String) {
        println("${currentTime()} ℹ️  $message")
    }

    fun error(message: String) {
        System.err.println("${currentTime()} ❌ $message")
    }

    fun warn(message: String) {
        println("${currentTime()} ⚠️  $message")
    }

    fun success(message: String) {
        println("${currentTime()} ✅ $message")
    }

    private fun currentTime(): String = LocalDateTime.now().format(formatter)
}