package sjj.novel.view.reader.bean;

import android.support.annotation.Nullable;

import java.util.List;

import sjj.novel.view.reader.page.TxtChapter;

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
    @Nullable
    public List<TxtChapter> bookChapterList;

    public boolean isLocal() {
        return false;
    }
}