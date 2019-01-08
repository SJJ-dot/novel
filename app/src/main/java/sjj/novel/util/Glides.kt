package sjj.novel.util

import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ViewTarget
import sjj.novel.R


val requestOptions by lazy { RequestOptions.placeholderOf(R.drawable.ic_laokuoteng).centerCrop() }

//fun bookCover).into