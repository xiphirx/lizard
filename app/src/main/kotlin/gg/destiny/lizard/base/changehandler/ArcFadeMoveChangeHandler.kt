package gg.destiny.lizard.base.changehandler

import android.transition.*
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandler

class ArcFadeMoveChangeHandler : TransitionChangeHandler() {
  override fun getTransition(container: ViewGroup,
                             from: View?,
                             to: View?,
                             isPush: Boolean): Transition =
      TransitionSet()
          .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
          .addTransition(Fade(Fade.OUT))
          .addTransition(
              TransitionSet()
                  .addTransition(ChangeBounds())
                  .addTransition(ChangeClipBounds())
                  .addTransition(ChangeTransform()))
          .addTransition(Fade(Fade.IN)).apply {
        pathMotion = ArcMotion()
      }
}
