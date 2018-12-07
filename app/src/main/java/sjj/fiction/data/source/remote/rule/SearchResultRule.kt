package sjj.fiction.data.source.remote.rule

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
}