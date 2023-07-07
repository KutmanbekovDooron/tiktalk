package com.andyshon.tiktalk.ui.zoneSingle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.ui.base.recycler.ItemClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.andyshon.tiktalk.data.entity.UserPreview
import com.andyshon.tiktalk.events.PublicRoomOpenUserProfileEvent
import com.andyshon.tiktalk.events.RxEventBus
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.zone_users_bottom_sheet.*

class UsersDropdownBSDFragment : BottomSheetDialogFragment() {

    var users: ArrayList<UserPreview> = arrayListOf()

    var rxEventBus: RxEventBus? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        var bottomSheet : View? = null
        if (dialog != null) {
            bottomSheet = dialog.findViewById(R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val view = view
        view?.post {
            val parent = view.parent as View
            val params = (parent).layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior
            bottomSheetBehavior.peekHeight = view.measuredHeight - view.measuredHeight/5
//            (bottomSheet?.parent as View).setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.zone_users_bottom_sheet, container, false)
    }


    private var adapter: UsersDropdownAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnHideUsersSheet.setOnClickListener {
            this@UsersDropdownBSDFragment.dismiss()
        }

        val gridView = view.findViewById<RecyclerView>(R.id.zoneUsersRecyclerView)
        gridView.layoutManager = GridLayoutManager(activity!!, 3)

        adapter = UsersDropdownAdapter(users, setClickListener())
        gridView.adapter = adapter

        gridView.addItemDecoration(ItemDecorationGridColumns(
            resources.getDimensionPixelSize(R.dimen.grid_list_spacing),
            resources.getInteger(R.integer.grid_list_colums)
        ))
    }

    private fun setClickListener(): ItemClickListener<UserPreview> {
        return object : ItemClickListener<UserPreview> {
            override fun onItemClick(view: View, pos: Int, item: UserPreview) {
                rxEventBus?.post(PublicRoomOpenUserProfileEvent(item.name, item.photo, item.phone))
            }
        }
    }
}