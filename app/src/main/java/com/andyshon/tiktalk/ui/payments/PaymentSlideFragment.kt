package com.andyshon.tiktalk.ui.payments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andyshon.tiktalk.R
import com.andyshon.tiktalk.utils.extensions.color
import com.andyshon.tiktalk.utils.extensions.loadRoundCornersImage
import kotlinx.android.synthetic.main.fragment_payment_slide.*
import org.jetbrains.anko.backgroundColor

class PaymentSlideFragment : Fragment() {

    companion object {
        fun newInstance(image: String): Fragment {
            val bundle = Bundle().apply { putString("image", image) }
            val f = PaymentSlideFragment()
            f.arguments = bundle
            return f
        }
    }

    private var image = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_slide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { image = it.getString("image") }

        if (image.isNotEmpty()) {
            imageView.loadRoundCornersImage(
                radius = resources.getDimensionPixelSize(R.dimen.radius_100),
                url = image
            )
        }
        else {
            imageView.backgroundColor = this color R.color.colorBtnGrey
        }
    }
}
