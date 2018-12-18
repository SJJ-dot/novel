package sjj.novel.view.reader.bean;

import java.io.Serializable;

/**
 * Created by newbiechen on 17-5-10.
 * 书的章节链接(作为下载的进度数据)
 * 同时作为网络章节和本地章节 (没有找到更好分离两者的办法)
 */
public class BookChapterBean{
    public String id;

    public String url;

    public String title;

    public int index;

    public String content;

    //所属的书籍
    private String bookId;
}