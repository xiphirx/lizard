package gg.destiny.lizard.base.mvi

import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

interface BaseView<in VM> : MvpView {
  fun render(model: VM)
  fun firstLoad(): Observable<Unit> = Observable.just(Unit)
  fun scheduler(): Scheduler = AndroidSchedulers.mainThread()
}
