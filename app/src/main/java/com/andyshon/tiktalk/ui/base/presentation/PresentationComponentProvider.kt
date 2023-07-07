package com.andyshon.tiktalk.ui.base.presentation

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.reactivex.disposables.CompositeDisposable

interface PresentationComponentProvider {

    fun provideSupportFragmentManager(): FragmentManager

    fun getDestroyDisposable(): CompositeDisposable

    fun provideActivity(): FragmentActivity

}