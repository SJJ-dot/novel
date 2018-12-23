package sjj.novel.view.reader.page;

/**
 * Created by newbiechen on 2018/2/5.
 * 作用：翻页动画的模式
 */

public enum PageMode {
    SIMULATION("仿真"),
    COVER("覆盖"),
    SLIDE("平移"),
    NONE("无"),
    SCROLL("滚动");

    public final String des;

    PageMode(String des) {
        this.des = des;
    }
}
