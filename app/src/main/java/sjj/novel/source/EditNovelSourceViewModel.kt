package sjj.novel.source

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import io.reactivex.Observable
import io.reactivex.functions.Function
import sjj.novel.data.repository.novelSourceRepository
import sjj.novel.data.source.remote.rule.*
import java.lang.IllegalStateException

class EditNovelSourceViewModel(private val TOP_LEVEL_DOMAIN: String) : ViewModel() {

    val sourceName = ObservableField<String>()
    val tld = ObservableField<String>()
    val baseUrl = ObservableField<String>()
    //搜素规则
    val isGet = ObservableBoolean()
    val isUtf8 = ObservableBoolean()
    val serverUrl = ObservableField<String>()
    val searchKey = ObservableField("searchkey")
    val searchResultViewModels = mutableListOf<SearchResultViewModel>()
    //简介
    val bookUrl = ObservableField<String>()
    val bookName = ObservableField<String>()
    val bookNameRegex = ObservableField<String>()
    val bookAuthor = ObservableField<String>()
    val bookAuthorRegex = ObservableField<String>()
    val bookIntro = ObservableField<String>()
    val bookIntroRegex = ObservableField<String>()
    val bookCoverImgUrl = ObservableField<String>()
    val bookChapterListUrl = ObservableField<String>()
    //章节列表解析
    val bookChapterListScope = ObservableField<String>()
    val bookChapterName = ObservableField<String>()
    val bookChapterNameRegex = ObservableField<String>()
    val bookChapterUrl = ObservableField<String>()
    //章节 内容解析
    val bookChapterContent = ObservableField<String>()


    private var reule: BookParseRule? = null
    private fun getRule(): Observable<BookParseRule> {
        if (reule != null) {
            return Observable.just(reule)
        }
        if (TOP_LEVEL_DOMAIN.isBlank()) {
            reule = BookParseRule()
            return Observable.just(reule)
        } else {
            return novelSourceRepository.getBookParseRule(TOP_LEVEL_DOMAIN).doOnNext {
                reule = it
            }.onErrorResumeNext(Function {
                reule = BookParseRule()
                Observable.just(reule)
            })
        }
    }

    fun fillViewModel(): Observable<BookParseRule> {
        return getRule().doOnNext { rule ->
            sourceName.set(rule.sourceName)
            tld.set(rule.topLevelDomain)
            baseUrl.set(rule.baseUrl)
            rule.searchRule?.also {
                isGet.set(it.method == Method.GET)
                isUtf8.set(it.charset == Charset.UTF8)
                serverUrl.set(it.serverUrl)
                searchKey.set(it.searchKey)

                it.resultRules?.forEach {
                    searchResultViewModels.add(SearchResultViewModel(it))
                }
            }
            //至少需要一个搜索结果解析规则
            if (searchResultViewModels.isEmpty()) {
                searchResultViewModels.add(SearchResultViewModel())
            }

            rule.introRule?.also {
                bookUrl.set(it.bookUrl)
                bookName.set(it.bookName)
                bookNameRegex.set(it.bookNameRegex)
                bookAuthor.set(it.bookAuthor)
                bookAuthorRegex.set(it.bookAuthorRegex)
                bookIntro.set(it.bookIntro)
                bookIntroRegex.set(it.bookIntroRegex)
                bookCoverImgUrl.set(it.bookCoverImgUrl)
                bookChapterListUrl.set(it.bookChapterListUrl)
            }
            rule.chapterListRule?.also {
                bookChapterListScope.set(it.bookChapterList)
                bookChapterName.set(it.bookChapterName)
                bookChapterNameRegex.set(it.bookChapterNameRegex)
                bookChapterUrl.set(it.bookChapterUrl)
            }
            rule.chapterContentRule?.also {
                bookChapterContent.set(it.bookChapterContent)
            }
        }
    }

    fun saveNovelSourceParseRule(): Observable<BookParseRule> {
        return getRule().doOnNext { bookParseRule ->
            bookParseRule.sourceName = sourceName.get()!!
            bookParseRule.topLevelDomain = tld.get()!!
            bookParseRule.baseUrl = baseUrl.get()!!

            val searchRule = bookParseRule.searchRule ?: SearchRule()
            bookParseRule.searchRule = searchRule
            searchRule.method = if (isGet.get()) Method.GET else Method.POST
            searchRule.charset = if (isUtf8.get()) Charset.UTF8 else Charset.GBK
            searchRule.serverUrl = serverUrl.get()!!
            searchRule.searchKey = searchKey.get()!!

            searchRule.resultRules = searchResultViewModels.map { resultViewModel ->
                resultViewModel.fillData(SearchResultRule())
            }
            val rule = bookParseRule.introRule ?: BookIntroRule()
            bookParseRule.introRule = rule
            rule.bookUrl = bookUrl.get()?:""
            rule.bookName = bookName.get()!!
            rule.bookNameRegex = bookNameRegex.get() ?: ""
            rule.bookAuthor = bookAuthor.get()!!
            rule.bookAuthorRegex = bookAuthorRegex.get() ?: ""
            rule.bookIntro = bookIntro.get()!!
            rule.bookIntroRegex = bookIntroRegex.get() ?: ""
            rule.bookCoverImgUrl = bookCoverImgUrl.get()!!
            rule.bookChapterListUrl = bookChapterListUrl.get() ?: ""

            val chapterListRule = bookParseRule.chapterListRule ?: BookChapterListRule()
            bookParseRule.chapterListRule = chapterListRule
            chapterListRule.bookChapterList = bookChapterListScope.get() ?: ""
            chapterListRule.bookChapterName = bookChapterName.get()!!
            chapterListRule.bookChapterNameRegex = bookChapterNameRegex.get() ?: ""
            chapterListRule.bookChapterUrl = bookChapterUrl.get()!!

            val contentRule = bookParseRule.chapterContentRule ?: ChapterContentRule()
            bookParseRule.chapterContentRule = contentRule
            contentRule.bookChapterContent = bookChapterContent.get()!!
        }.flatMap {
            novelSourceRepository.saveBookParseRule(it)
        }
    }

    fun deleteSearchResultViewModel(searchResultViewModel: SearchResultViewModel):Observable<SearchResultViewModel> {
        return Observable.fromCallable {
            if (searchResultViewModels.size < 2) {
                throw IllegalStateException("至少需要一个搜索结果解析规则，禁止删除")
            }
            searchResultViewModels.remove(searchResultViewModel)
            searchResultViewModel
        }
    }


    class SearchResultViewModel(resultRule: SearchResultRule? = null) {
        val bookInfos = ObservableField<String>()
        val name = ObservableField<String>()
        val nameRegex = ObservableField<String>()
        val author = ObservableField<String>()
        val authorRegex = ObservableField<String>()
        val bookUrl = ObservableField<String>()
        var lastChapterUrl = ObservableField<String>()
        var lastChapter = ObservableField<String>()
        var lastChapterRegex = ObservableField<String>()
        var bookCoverImgUrl = ObservableField<String>()
        init {
            resultRule?.also { rule ->
                bookInfos.set(rule.bookInfos)
                name.set(rule.name)
                nameRegex.set(rule.nameRegex)
                author.set(rule.author)
                authorRegex.set(rule.authorRegex)
                bookUrl.set(rule.bookUrl)
                lastChapterUrl.set(rule.lastChapterUrl)
                lastChapter.set(rule.lastChapterName)
                lastChapterRegex.set(rule.lastChapterNameRegex)
                bookCoverImgUrl.set(rule.bookCoverImgUrl)
            }
        }

        fun fillData(resultRule: SearchResultRule): SearchResultRule {
            resultRule.bookInfos = bookInfos.get() ?: ""
            resultRule.name = name.get() ?: ""
            resultRule.nameRegex = nameRegex.get() ?: ""
            resultRule.author = author.get() ?: ""
            resultRule.authorRegex = authorRegex.get() ?: ""
            resultRule.bookUrl = bookUrl.get() ?: ""
            resultRule.lastChapterUrl = lastChapterUrl.get() ?: ""
            resultRule.lastChapterName = lastChapter.get() ?: ""
            resultRule.lastChapterNameRegex = lastChapterRegex.get() ?: ""
            resultRule.bookCoverImgUrl = bookCoverImgUrl.get() ?: ""
            return resultRule
        }
    }

}