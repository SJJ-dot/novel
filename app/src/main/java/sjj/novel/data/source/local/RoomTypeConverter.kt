package sjj.novel.data.source.local

import android.arch.persistence.room.TypeConverter
import sjj.novel.data.source.remote.rule.BookChapterListRule
import sjj.novel.data.source.remote.rule.BookIntroRule
import sjj.novel.data.source.remote.rule.ChapterContentRule
import sjj.novel.data.source.remote.rule.SearchRule
import sjj.novel.model.Book
import sjj.novel.util.gson


class Converters {
    @TypeConverter
    fun stringToSearchRule(value: String?): SearchRule? = gson.fromJson(value, SearchRule::class.java)

    @TypeConverter
    fun searchRuleToString(data: SearchRule?): String? = gson.toJson(data)


    @TypeConverter
    fun stringToBookIntroRule(value: String?): BookIntroRule? = gson.fromJson(value, BookIntroRule::class.java)

    @TypeConverter
    fun bookIntroRuleToString(data: BookIntroRule?): String? = gson.toJson(data)


    @TypeConverter
    fun stringToBookChapterListRule(value: String?): BookChapterListRule? = gson.fromJson(value, BookChapterListRule::class.java)

    @TypeConverter
    fun bookChapterListRuleToString(data: BookChapterListRule?): String? = gson.toJson(data)


    @TypeConverter
    fun stringToChapterContentRule(value: String?): ChapterContentRule? = gson.fromJson(value, ChapterContentRule::class.java)

    @TypeConverter
    fun chapterContentRuleToString(data: ChapterContentRule?): String? = gson.toJson(data)

    @TypeConverter
    fun bookLoadStateToString(status: Book.LoadState) = status.name

    @TypeConverter
    fun stringToBookLoadState(status: String) = Book.LoadState.valueOf(status)
}