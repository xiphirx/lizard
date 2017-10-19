package gg.destiny.lizard.text

import android.text.SpannableString
import android.text.SpannableStringBuilder
import java.util.ArrayDeque

/** Wrapper around [SpannableStringBuilder]'s horrid API */
class Spanner() {
  private val builder = SpannableStringBuilder()
  private val stack = ArrayDeque<Pair<Int, Any>>()

  fun append(charSequence: CharSequence): Spanner {
    builder.append(charSequence)
    return this
  }

  fun append(num: Number) = append(num.toString())

  fun pushSpan(span: Any): Spanner {
    stack.push(builder.length to span)
    return this
  }

  fun pushPopSpan(span: Any) = pushSpan(span).append(".").popSpan()

  fun popSpan(): Spanner {
    val (position, span) = stack.pop()
    builder.setSpan(span, position, builder.length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
    return this
  }

  fun build(): CharSequence {
    while (stack.isNotEmpty()) {
      popSpan()
    }
    return builder
  }
}
