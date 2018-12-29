package sjj.novel.view.fragment

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import sjj.alog.Log
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Chapter
import sjj.novel.util.id

class ChapterListViewModel(val bookName: String, val bookAuthor: String) : ViewModel() {
    /**
     * 通知滚动到阅读记录的位置
     */
    val scrollToReadIndex = ObservableBoolean()
    val readIndex = ObservableInt(scrollToReadIndex)
    var chapterList: List<ChapterViewModel> = listOf()

    fun fillViewModel(): Flowable<List<ChapterViewModel>> {
        return novelDataRepository.getBookInBookSource(bookName, bookAuthor).flatMap { book ->
            novelDataRepository.getBookSourceRecord(bookName, bookAuthor).map {
                readIndex.set(it.readIndex)
                book
            }
        }.flatMap {
            novelDataRepository.getChapterIntro(it.url).map {
                chapterList = it.map { c ->
                    ChapterViewModel().apply {
                        chapter = c
                        chapterName.set(c.chapterName)
                        isLoadSuccess.set(c.isLoadSuccess)
                        isReading.set(c.index == readIndex.get())
                    }
                }
                chapterList
            }
        }
    }


    class ChapterViewModel {
        lateinit var chapter: Chapter
        val id by lazy { chapter.url.id }
        val chapterName = ObservableField<String>()
        val isLoadSuccess = ObservableBoolean()
        val isReading = ObservableBoolean()

    }
}