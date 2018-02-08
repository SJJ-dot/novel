package sjj.fiction.model

import android.database.Observable
import android.databinding.ObservableField

/**
 * Created by sjj on 2018/2/8.
 */
class User {
    val name = ObservableField<String>()
    override fun toString(): String {
        return "User(name=${name.get()})"
    }

}