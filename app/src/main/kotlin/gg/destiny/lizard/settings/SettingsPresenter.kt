package gg.destiny.lizard.settings

import gg.destiny.lizard.base.mvi.BasePresenter
import gg.destiny.lizard.core.settings.SettingSpec
import gg.destiny.lizard.core.settings.SettingsModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    private val settingsStorage: SettingsStorage
) : BasePresenter<SettingsView, SettingsModel>() {
  override fun bindIntents(scheduler: Scheduler): Observable<SettingsModel> {
    val firstLoad = intent { it.firstLoad() }
        .flatMap { Observable.fromIterable(settingsStorage.loadExistingSettings()) }

    val booleanToggle = intent { it.booleanSettingToggles }
        .map { it.value = !it.value; it }
        .doOnNext { settingsStorage.updateBooleanSetting(it) }

    return Observable.merge(firstLoad, booleanToggle)
        .observeOn(scheduler)
        .scan(SettingsModel()) { model, state -> reduce(model, state) }
  }

  private fun reduce(model: SettingsModel, newSpec: SettingSpec<out Any>): SettingsModel {
    val newSpecs = mutableListOf<SettingSpec<out Any>>()
    var found = false
    for (oldSpec in model.settings) {
      if (oldSpec.key == newSpec.key) {
        newSpecs.add(newSpec)
        found = true
      } else {
        newSpecs.add(oldSpec)
      }
    }
    if (!found) {
      newSpecs.add(newSpec)
    }
    return SettingsModel(newSpecs)
  }
}
