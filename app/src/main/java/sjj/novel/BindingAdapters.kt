package sjj.novel

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.RecyclerView
import sjj.alog.Log
import java.text.FieldPosition

//@BindingMethods(
//        BindingMethod(type = RecyclerView::class,
//                attribute = "app:srcCompat",
//                method = "setImageDrawable"))
@BindingAdapter("app:scrollToPosition")
fun scrollToPosition(view: RecyclerView, position: Int) {
    view.scrollToPosition(position)
}