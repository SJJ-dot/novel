package sjj.novel.data.repository

import com.tencent.mmkv.MMKV
import sjj.novel.data.source.remote.rule.BookParseRule
import sjj.novel.util.gson

/**
 * 小说源存储 后续可能会存到数据库里面
 */
object NovelSourceConfig {
    private val storage by lazy { MMKV.mmkvWithID("FICTION_SOURCE_CONFIG") }
    fun getAllBookParseRule(): List<BookParseRule> {
        return storage.all.map {
            gson.fromJson(it.value.toString(), BookParseRule::class.java)
        }
    }

    fun saveBookParseRule(rule: BookParseRule) {
        storage.putString(rule.topLevelDomain, gson.toJson(rule))
    }

    fun deleteBookParseRule(rule: BookParseRule) {
        storage.remove(rule.topLevelDomain)
    }

}