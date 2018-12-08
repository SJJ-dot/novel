package sjj.novel.data.source.remote.rule

/**
 * 搜索规则
 */
class SearchRule {
    /**
     * 搜索内容需要转换的字符编码格式 默认为 utf - 8
     */
    var charset = Charset.UTF8

    /**
     * 搜索默认使用get 亲求
     */
    var method = Method.GET

    /**
     * 搜索的 url
     */
    var serverUrl: String = ""

    /**
     * 搜索的 key
     */
    var searchKey: String = "searchKey"

    /**
     * 获取搜索结果的规则
     * 可能存在多种情况。一组规则没有获得结果 则使用后一组规则获取
     */
    var resultRules: List<SearchResultRule>? = null

}