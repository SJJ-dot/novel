package sjj.novel.data.source.remote.rule

/**
 * 搜索结果即系规则
 */
class SearchResultRule {

    /**
     * 数据信息列表 选取规则
     */
    var bookInfos = ""

    /**
     * 搜索 读取书籍名字
     */
    var name = ""
    /**
     * 提取文本的正则表达式
     */
    var nameRegex = ""
    /**
     * 读取书籍作者
     */
    var author = ""
    /**
     *提取文本的正则表达式
     */
    var authorRegex = "*"

    var bookUrl = ""

    /**
     * 书籍封面的url 应当指向<img /> 标签
     */
    var bookCoverImgUrl = ""

    /**
     * 最新章章节
     */
    var lastChapterUrl = ""
    var lastChapterName = ""
    var lastChapterNameRegex = ""
}