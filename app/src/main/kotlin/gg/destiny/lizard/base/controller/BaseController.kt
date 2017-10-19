package gg.destiny.lizard.base.controller

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import gg.destiny.lizard.base.mvi.BasePresenter
import gg.destiny.lizard.base.mvi.BaseView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

abstract class BaseController<V : BaseView<VS>, VS, out P : BasePresenter<V, VS>> : Controller {
  constructor() : super()
  constructor(arguments: Bundle) : super(arguments)

  protected val presenter: P by lazy { createPresenter() }

  @Suppress("UNCHECKED_CAST")
  override fun onAttach(view: View) {
    super.onAttach(view)
    presenter.attachView(this as V)
  }

  @Suppress("UNCHECKED_CAST")
  override fun onDetach(view: View) {
    super.onDetach(view)
    presenter.detachView(!(isBeingDestroyed || isDestroyed))
  }

  abstract fun createPresenter(): P

  fun firstLoad(): Observable<Unit> = Observable.just(Unit)

  fun scheduler(): Scheduler = AndroidSchedulers.mainThread()

  fun withView(block: View.() -> Unit) {
    view?.block()
  }
}
