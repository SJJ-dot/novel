package sjj.novel.view.reader.page;

import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

/**
 * Created by newbiechen on 17-7-1.
 */

public class TxtChapter {
    //保存10章的章节内容 这里的10个字符串会导致内存泄漏。但是我并不关心 总不可能有10M的
    private static String cacheBookId;
    private static LruCache<String, String> contents = new LruCache<>(50);

    public static void checkCache(String bookId) {
        if (TextUtils.isEmpty(cacheBookId)||!bookId.equals(cacheBookId)) {
            contents.evictAll();
        }
    }
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
        cacheBookId = bookId;
        if (!TextUtils.isEmpty(content))
            contents.put(link, content);
    }



    @NonNull
    @Override
    public String toString() {
        return "TxtChapter{" +
                "bookId='" + bookId + '\'' +
                ", link='" + link + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
