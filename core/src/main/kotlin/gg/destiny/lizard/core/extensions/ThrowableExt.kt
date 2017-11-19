package gg.destiny.lizard.core.extensions

fun Throwable.fullMessage() = "$message\n${stackTrace?.joinToString(separator = "\n")}"
