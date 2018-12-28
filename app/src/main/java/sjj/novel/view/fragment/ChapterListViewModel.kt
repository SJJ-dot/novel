package sjj.novel.view.fragment

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import sjj.alog.Log
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.model.Chapter

class ChapterListViewModel(val bookName: String, val bookAuthor: String) : ViewModel() {

    var chapterList: List<ChapterListViewModel> = listOf()

    fun fillViewModel(): Flowable<List<ChapterViewModel>> {
        return novelDataRepository.getBookInBookSource(bookName, bookAuthor).flatMap {
            Log.e(it)
            novelDataRepository.getChapterIntro(it.url)
        }.map {
            Log.e(it)
            it.map { c ->
                ChapterViewModel().apply {
                    chapter = c
                    chapterName.set(c.chapterName)
                    isLoadSuccess.set(c.isLoadSuccess)
                }
            }
        }
    }


    class ChapterViewModel {
        lateinit var chapter:Chapter
        val chapterName = ObservableField<String>()
        val isLoadSuccess = ObservableBoolean()
    }
}