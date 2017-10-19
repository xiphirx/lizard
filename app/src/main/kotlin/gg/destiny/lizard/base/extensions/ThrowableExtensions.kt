package gg.destiny.lizard.base.extensions

fun Throwable.fullMessage(): String {
  return "$message\n${stackTrace?.joinToString(separator = "\n")}"
}
