package sjj.novel.view.reader.bean;

import androidx.annotation.NonNull;

/**
 * Created by newbiechen on 17-5-20.
 */
public class BookRecordBean{
    //所属的书的id
    public String bookId;
    //阅读到了第几章
    public int chapter;
    //当前的页码
    public int pagePos;

    public boolean isThrough = false;

    @NonNull
    @Override
    public String toString() {
        return "BookRecordBean{" +
                "bookId='" + bookId + '\'' +
                ", chapter=" + chapter +
                ", pagePos=" + pagePos +
                ", isThrough=" + isThrough +
                '}';
    }
}
