package gg.destiny.lizard.base.extensions

private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHexString(): String {
  val result = StringBuilder(size * 2)
  forEach {
    val octet = it.toInt()
    val firstIndex = (octet and 0xF0) ushr 4
    val secondIndex = octet and 0x0F
    result.append(HEX_CHARS[firstIndex])
        .append(HEX_CHARS[secondIndex])
  }
  return result.toString()
}
