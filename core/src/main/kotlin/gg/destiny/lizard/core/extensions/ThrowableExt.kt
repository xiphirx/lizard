package gg.destiny.lizard.core.extensions

fun Throwable.fullMessage() = "$message\n" +
    "${stackTrace?.joinToString("\n")}\n" +
    "${cause?.stackTrace?.joinToString("\n")}"
