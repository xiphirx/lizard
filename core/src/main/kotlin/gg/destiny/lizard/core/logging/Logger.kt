package gg.destiny.lizard.core.logging

object Logger {
  interface Instance {
    fun log(message: String)

    fun log(t: Throwable, message: String)
  }

  var instance: Instance? = null
}

inline fun L(message: () -> String) = log { it.log(message()) }
inline fun L(t: Throwable, message: () -> String) = log { it.log(t, message()) }

@PublishedApi
internal inline fun log(block: (Logger.Instance) -> Unit) {
  Logger.instance?.let { block(it) }
}
