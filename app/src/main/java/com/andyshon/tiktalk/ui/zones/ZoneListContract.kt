package com.andyshon.tiktalk.ui.zones

import com.andyshon.tiktalk.data.entity.PlacesResult
import com.andyshon.tiktalk.ui.base.BaseContract

interface ZoneListContract: BaseContract {

    interface View: BaseContract.View {

        fun showPlaces()
        fun updatePlaceUsersCount(pos: Int, count: Int)
        fun openSingleZone(place: PlacesResult)
    }
}