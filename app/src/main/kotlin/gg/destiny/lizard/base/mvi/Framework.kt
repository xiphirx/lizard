package gg.destiny.lizard.base.mvi

import android.support.annotation.CallSuper
import android.support.annotation.MainThread
import com.github.ajalt.timberkt.d
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

private typealias IntentRelayPair<V, R> = Pair<PublishRelay<R>, (V) -> Observable<R>>
/**
 * Base MVI framework components. A lot of the concepts here are borrowed from
 * Hannes Dorfmann's Mosby project: https://github.com/sockeqwe/mosby
 */
abstract class BasePresenter<V : BaseView<VM>, VM> {
  private val intentRelayDisposable = CompositeDisposable()
  private val intentRelayPairs = mutableListOf<IntentRelayPair<V, *>>()

  private lateinit var stateRelayDisposable: Disposable
  private val stateRelay = BehaviorRelay.create<VM>()

  private var firstAttach = true

  @CallSuper
  @MainThread
  open fun attachView(view: V) {
    if (firstAttach) {
      bindIntents(view.scheduler())
    }

    d { "View subscribed to behavior" }
    stateRelayDisposable = stateRelay.subscribe { view.render(it) }

    intentRelayPairs.forEach {
      @Suppress("UNCHECKED_CAST")
      bindIntentRelay(view, it as IntentRelayPair<V, Any>)
    }

    firstAttach = false
  }

  @CallSuper
  @MainThread
  open fun detachView(retainInstance: Boolean) {
    d { "Clearing intent relays" }
    intentRelayDisposable.clear()
    stateRelayDisposable.dispose()
  }

  abstract fun bindIntents(scheduler: Scheduler)

  fun <R : Any> intent(binder: (V) -> Observable<R>): Observable<R> {
    val relay = PublishRelay.create<R>()
    d { "Relay created" }
    intentRelayPairs.add(relay to binder)
    return relay
  }

  private fun <R : Any>  bindIntentRelay(view: V, pair: IntentRelayPair<V, R>): Observable<R> {
    val (relay, binder) = pair
    d { "Relay subscribed to observable" }
    intentRelayDisposable.add(binder(view).subscribe(relay))
    return relay
  }

  fun view(observable: Observable<VM>) {
    d { "Behavior subscribed to Relay" }
    observable.subscribe(stateRelay)
  }
}


interface BaseView<in M> {
  fun firstLoad(): Observable<Unit>
  fun render(model: M)
  fun scheduler(): Scheduler
}

