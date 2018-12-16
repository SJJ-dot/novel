package sjj.novel.details

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import sjj.novel.R
import sjj.novel.data.repository.novelDataRepository
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.data.source.local.localFictionDataSource
import sjj.novel.model.Book
import sjj.novel.util.host
import sjj.novel.util.resStr

class ChooseBookSourceViewModel(val bookName: String, val author: String) : ViewModel() {
    val isRefreshing = ObservableBoolean()

    var bookList = listOf<ChooseBookSourceItemViewModel>()

    fun fillViewModel(): Flowable<List<ChooseBookSourceItemViewModel>> {
        return localFictionDataSource.getBooks(bookName, author).flatMap {
            bookList = it.map {
                val model = ChooseBookSourceItemViewModel()
                model.book = it
                model.bookCover.set(it.bookCoverImgUrl)
                model.bookName.set(it.name)
                model.author.set(R.string.author_.resStr(it.author))
//               model.origin.set(R.string.origin.resStr(it.))
                model
            }
            novelSourceRepository.getAllBookParseRule().map { list ->
                bookList.forEach { model ->
                    val rule = list.find { rule -> model.book.url.host.endsWith(rule.topLevelDomain) }
                    model.origin.set(R.string.origin.resStr(rule?.sourceName))
                }
                bookList
            }.flatMap { modelList ->
                Flowable.fromIterable(modelList).flatMap {m->
                    localFictionDataSource.getLatestChapter(m.book.url).doOnNext { chapter ->
                        m.lastChapter.set(R.string.newest_.resStr(chapter.chapterName))
                    }.toFlowable(BackpressureStrategy.BUFFER)
                }.map {
                    bookList
                }
            }
        }
    }

    fun refresh(): Observable<Book> {
        isRefreshing.set(true)
        return localFictionDataSource.getBooks(bookName, author).firstElement().toObservable().flatMap {
            Observable.fromIterable(it).flatMap {
                novelDataRepository.refreshBook(it.url)
            }
        }.doOnTerminate {
            isRefreshing.set(false)
        }
    }

    fun setBookSource(url: String): Observable<Int> {
        return novelDataRepository.setBookSource(bookName, author, url)
    }

    class ChooseBookSourceItemViewModel {
        lateinit var book: Book
        val bookCover = ObservableField<String>()
        val bookName = ObservableField<String>()
        val author = ObservableField<String>()
        val lastChapter = ObservableField<String>()
        val origin = ObservableField<String>()
    }
}