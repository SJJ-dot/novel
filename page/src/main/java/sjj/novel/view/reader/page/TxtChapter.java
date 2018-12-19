package sjj.novel.view.reader.page;

import android.support.v4.util.LruCache;
import android.text.TextUtils;

/**
 * Created by newbiechen on 17-7-1.
 */

public class TxtChapter {
    //保存10章的章节内容
    private static LruCache<String, String> contents = new LruCache<>(10);
    //章节所属的小说(网络)
    public String bookId;
    //章节的链接(网络)
    public String link;

    //章节名(共用)
    public String title;

    /**
     * 章节内容
     */
    public String getContent() {
        return contents.get(link);
    }

    public void setContent(String content) {
        if (!TextUtils.isEmpty(content))
            contents.put(link, content);
    }

}
