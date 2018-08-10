package gg.destiny.lizard.base.controller

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import gg.destiny.lizard.base.mvi.BaseView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

abstract class BaseController<V : MvpView, VS, P : MviBasePresenter<V, VS>>
  : Controller, BaseView<VS> {
  constructor() : super()
  constructor(arguments: Bundle) : super(arguments)

  @Inject
  lateinit var presenter: P
  protected val layout
    get() = view ?: throw IllegalStateException("Accessing layout before its ready")

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

  override fun firstLoad(): Observable<Unit> = Observable.just(Unit)

  override fun scheduler(): Scheduler = AndroidSchedulers.mainThread()

  fun withView(block: View.() -> Unit) {
    view?.block()
  }
}
