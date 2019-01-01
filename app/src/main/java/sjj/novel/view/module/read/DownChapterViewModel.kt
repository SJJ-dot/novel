package sjj.novel.view.module.read

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import sjj.alog.Log
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Book
import sjj.novel.model.BookSourceRecord
import sjj.novel.model.Chapter
import sjj.novel.util.SafeLiveData
import sjj.novel.util.ViewModelDispose

class DownChapterViewModel(var bookName: String, var bookAuthor: String) : ViewModelDispose() {

    val book = SafeLiveData<Book>()
        get() {
            if (field.value == null) {
                novelDataRepository.getBookInBookSource(bookName, bookAuthor).subscribe {
                    book.setValue(it)
                }.autoDispose("download chapter load book")
            }
            return field
        }

    val chapterList= SafeLiveData<List<Chapter>>()
        get() {
            val data = field

            if (field.value == null) {
                book.observeForever(object : Observer<Book> {
                    override fun onChanged(bk: Book?) {
                        book.removeObserver(this)
                        if (bk == null) {
                            removeDispose("download chapter load chapters")
                        } else {
                            novelDataRepository.getChapterIntro(bk.url).subscribe {
                                data.setValue(it)
                            }.autoDispose("download chapter load chapters")
                        }
                    }
                })
            }
            return field
        }

    val bookSourceRecord = SafeLiveData<BookSourceRecord>()
        get() {
            if (field.value == null) {
                novelDataRepository.getBookSourceRecord(bookName, bookAuthor).subscribe {
                    field.setValue(it)
                }.autoDispose("download chapter load bookSourceRecord")
            }
            return field
        }
    /**
     * 开始下载的起始章节
     */
    val startChapter: LiveData<Chapter> = Transformations.switchMap(bookSourceRecord) { record ->
        Transformations.map(chapterList) {
            it.getOrNull(record.readIndex) ?: it.lastOrNull()
        } as LiveData<Chapter>
    }

    val endChapter: LiveData<Chapter> = Transformations.map(chapterList) {
        it.lastOrNull()
    }

    override fun onCleared() {

    }
}