package sjj.novel.data.source.remote.rule

class BookChapterListRule {
    /**
     * 章节列表的url 如果为空的话则为当前的网页的url
     * 这个大概是没有什么用的
     */
    var bookChapterListUrl = ""

    /**
     * 章节列表 循环。缩小范围
     */
    var bookChapterList = ""
    /**
     * 章节url 应当指向一个超链接
     */
    var bookChapterUrl = ""

    /**
     * 章节名的url
     */
    var bookChapterName = ""
    var bookChapterNameRegex = ""

}