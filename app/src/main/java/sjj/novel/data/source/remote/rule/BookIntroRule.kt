package sjj.novel.data.source.remote.rule

/**
 *书籍简介解析规则（css选择器）
 */
class BookIntroRule {
    /**
     * 简介内容位置。
     * 先尽量缩小范围以提高查询效率
     */
    var bookInfo = ""

    /**
     * 书籍简介的url 通常是当前的这个请求的url
     */
    var bookUrl = ""
    /**
     * 书籍名字
     */
    var bookName = ""
    /**
     * 提取名字时使用的正则表达式
     */
    var bookNameRegex = ""
    /**
     * 书籍作者
     */
    var bookAuthor = ""
    /**
     * 作者名字提取正则表达式
     */
    var bookAuthorRegex = ""

    /**
     * 书籍封面的url 应当指向<img /> 标签
     */
    var bookCoverImgUrl = ""

    /**
     * 书籍简介
     */
    var bookIntro = ""
    /**
     * 书籍简介内容提取的正则表达式 。 通常为空
     */
    var bookIntroRegex = ""

    /**
     * 章节列表的url 如果为空的话则为当前的网页的url
     */
    var bookChapterListUrl = ""
}