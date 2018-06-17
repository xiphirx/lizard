package gg.destiny.lizard.settings

import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.register
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import gg.destiny.lizard.R
import gg.destiny.lizard.base.controller.BaseController
import gg.destiny.lizard.base.mvi.BaseView
import gg.destiny.lizard.core.settings.BooleanSetting
import gg.destiny.lizard.core.settings.SettingSpec
import gg.destiny.lizard.core.settings.SettingsModel
import io.reactivex.Observable
import kotlinx.android.synthetic.main.controller_settings.view.settings_recycler
import kotlinx.android.synthetic.main.include_app_bar.view.toolbar
import kotlinx.android.synthetic.main.item_settings_boolean.view.settings_item_boolean_container
import kotlinx.android.synthetic.main.item_settings_boolean.view.settings_item_boolean_switch
import kotlinx.android.synthetic.main.item_settings_boolean.view.settings_item_boolean_title
import kotlinx.android.synthetic.main.item_settings_static_text.view.settings_item_static_text_text

interface SettingsView : BaseView<SettingsModel> {
  val booleanSettingToggles: Observable<BooleanSetting>
}

class SettingsController : BaseController<SettingsView, SettingsModel, SettingsPresenter>(), SettingsView {
  override val booleanSettingToggles: Relay<BooleanSetting> = PublishRelay.create()

  private val settingsAdapter = FlexAdapter<SettingSpec<out Any>>().apply {
    register<BooleanSetting>(R.layout.item_settings_boolean) { setting, view, _ ->
      view.apply {
        settings_item_boolean_title.setText(nameOf(setting))
        settings_item_boolean_switch.isChecked = setting.value
        RxView.clicks(settings_item_boolean_container)
            .map { setting }
            .doOnNext { settings_item_boolean_switch.isChecked = !it.value }
            .subscribe(booleanSettingToggles)
      }
    }
  }

  override fun createPresenter() = SettingsPresenter()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.controller_settings, container, false).apply {
      toolbar.setTitle(R.string.settings_toolbar_title)
      settings_recycler.apply {
        adapter = settingsAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
      }
    }
  }

  override fun render(model: SettingsModel) {
    for ((i, spec) in model.settings.withIndex()) {
      if (settingsAdapter.items.getOrNull(i)?.key == spec.key) {
        settingsAdapter.items[i] = spec
      } else {
        settingsAdapter.items.add(i, spec)
      }
    }
  }

  private fun nameOf(spec: SettingSpec<out Any>): Int {
    return when (spec) {
      BooleanSetting.DARK_MODE -> R.string.settings_dark_mode_title
      else -> throw IllegalArgumentException("Unexpected spec ${spec.key}")
    }
  }
}
