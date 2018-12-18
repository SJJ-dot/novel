package sjj.novel.view.reader.page;

import android.text.TextUtils;

/**
 * Created by newbiechen on 17-7-1.
 */

public class TxtChapter {

    //章节所属的小说(网络)
    public String bookId;
    //章节的链接(网络)
    public  String link;

    //章节名(共用)
    public String title;

    /**
     * 章节内容
     */
    public String content;

    public boolean hasData = false;

    public void clean() {
        content = null;
        hasData = false;
    }

}
