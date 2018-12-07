package sjj.fiction.data.source.remote.rule

/**
 * 小说网站解析
 */
class BookParseRule {
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
     * 书籍简介与章节列表
     */
    var bookIntroRule:BookIntroRule?=null
    /**
     * 章节内容解析
     */
    var chapterContentRule:ChapterContentRule?=null
}


