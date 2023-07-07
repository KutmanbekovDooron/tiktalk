package com.andyshon.tiktalk.di.module

import com.andyshon.tiktalk.di.scope.PresentationScope
import com.andyshon.tiktalk.ui.base.DialogProvider
import com.andyshon.tiktalk.ui.base.presentation.PresentationComponentProvider
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.Module
import dagger.Provides

@Module
class PresentationModule(val uiProvider: PresentationComponentProvider) {

    @Provides
    @PresentationScope
    fun provideFragmentManager() = uiProvider.provideSupportFragmentManager()

    @Provides
    @PresentationScope
    fun provideDialog(): DialogProvider = DialogProvider()

    @Provides
    @PresentationScope
    fun provideCompositeDisposable() = uiProvider.getDestroyDisposable()

    @Provides
    @PresentationScope
    fun provideRxPermissions(): RxPermissions = RxPermissions(uiProvider.provideActivity())
}