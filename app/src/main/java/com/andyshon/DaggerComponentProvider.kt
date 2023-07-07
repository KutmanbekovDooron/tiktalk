package com.andyshon

import com.andyshon.tiktalk.App
import com.andyshon.tiktalk.di.component.ApplicationComponent
import com.andyshon.tiktalk.di.component.DaggerApplicationComponent
import com.andyshon.tiktalk.di.module.ApplicationModule


class DaggerComponentProvider(val app: App) {

    private var _appComponent: ApplicationComponent? = null

    val appComponent: ApplicationComponent
        get() {
            if (_appComponent == null) {
                _appComponent = DaggerApplicationComponent.builder()
                    .applicationModule(ApplicationModule(app))
                    .build()
            }
            return _appComponent!!
        }

}