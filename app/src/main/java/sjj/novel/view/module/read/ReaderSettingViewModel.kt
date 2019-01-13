package sjj.novel.view.module.read

import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import sjj.novel.data.repository.novelDataRepository

class ReaderSettingViewModel(val bookName: String, val bookAuthor: String) : ViewModel() {
    val pageCount = ObservableInt()
    val pagePos = ObservableInt()
    val pageLoaderStatus = ObservableInt()
}