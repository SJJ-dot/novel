package sjj.fiction.data.source.remote.rule

/**
 * Html 元素规则
 * 读取内容
 */
class ElementRule {
    /**
     *e.g getElementByClass
     */
    var type: String = "class"

    /**
     * 例如 index 1 2 3 4 从列表取第0个返回
     *
     */
    var inside: String? = null

}