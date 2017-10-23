package gg.destiny.lizard.base.mvi

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class BasePresenter<V : BaseView<VM>, VM> : MviBasePresenter<V, VM>() {
  override fun bindIntents() {
    subscribeViewState(
        bindIntents(AndroidSchedulers.mainThread()),
        { view, model -> view.render(model) })
  }

  abstract fun bindIntents(scheduler: Scheduler): Observable<VM>
}
