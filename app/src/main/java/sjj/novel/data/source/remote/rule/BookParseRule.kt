package sjj.novel.data.source.remote.rule

/**
 * 小说网站解析
 */
class BookParseRule {
    /**
     * 书源名称
     */
    var sourceName = ""
    /**
     * 书源的优先级排序 暂时没什么用
     */
    var sequence = 1.0;

    /**
     * 网站标识（顶级域名）
     */
    var topLevelDomain: String = ""
    /**
     * 网站域名地址
     */
    var baseUrl: String = ""
    /**
     * 小说搜索规则
     */
    var searchRule: SearchRule? = null
    /**
     * 书籍简介
     */
    var introRule: BookIntroRule? = null

    /**
     * 章节列表解析规则
     */
    var chapterListRule: BookChapterListRule? = null

    /**
     * 章节内容解析
     */
    var chapterContentRule: ChapterContentRule? = null
}


