package sjj.novel.view.reader.bean;

import java.util.List;

/**
 * Created by newbiechen on 17-5-8.
 * 收藏的书籍
 */
public class BookBean {
    public String id;
    public String title;
    public String author;
    public String shortIntro;
    public String cover;
    public List<BookChapterBean> bookChapterList;

    public boolean isLocal() {
        return false;
    }
}