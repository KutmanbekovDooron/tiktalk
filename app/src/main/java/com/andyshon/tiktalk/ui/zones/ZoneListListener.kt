package com.andyshon.tiktalk.ui.zones

import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.data.entity.Zone
import com.andyshon.tiktalk.ui.base.BaseFragmentListener

interface ZoneListListener: BaseFragmentListener {
    fun openSingleZone(zone: PlacesResult)
    fun openSettings()
}