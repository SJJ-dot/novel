package sjj.novel.view.reader.page;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import sjj.novel.view.page.R;

/**
 * Created by newbiechen on 2018/2/5.
 * 作用：页面的展示风格。
 */

public enum PageStyle {
    BG_def(R.color.chapter_text_color, R.color.chapter_background),
    BG_0(R.color.nb_read_font_1, R.drawable.theme_leather_bg,BackgroundType.drawable),
    BG_1(R.color.nb_read_font_2, R.color.nb_read_bg_2),
    BG_2(R.color.nb_read_font_3, R.color.nb_read_bg_3),
    BG_3(R.color.nb_read_font_4, R.color.nb_read_bg_4),
    BG_4(R.color.nb_read_font_5, R.color.nb_read_bg_5),
    NIGHT(R.color.nb_read_font_night, R.color.nb_read_bg_night);

    private int fontColor;
    private int bgResId;
    private BackgroundType type = BackgroundType.color;

    PageStyle(@ColorRes int fontColor, @ColorRes int bgResId) {
        this.fontColor = fontColor;
        this.bgResId = bgResId;
    }
    PageStyle(@ColorRes int fontColor, @DrawableRes int bgResId, BackgroundType type) {
        this.fontColor = fontColor;
        this.bgResId = bgResId;
        this.type = type;
    }

    public int getFontColor() {
        return fontColor;
    }

    public Drawable getBackgroundDrawable(Context context) {
        switch (type) {
            case color:
                return new BgDrawable(new ColorDrawable(ContextCompat.getColor(context, bgResId)));
            case drawable:
                return new BgDrawable(ContextCompat.getDrawable(context, bgResId));
        }
        return null;
    }

    public enum BackgroundType {
        color, drawable
    }

    class BgDrawable extends Drawable {
        private Drawable drawable;
        private Rect rect = new Rect();

        BgDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.getClipBounds(rect);
            drawable.setBounds(rect);
            drawable.draw(canvas);
        }

        @Override
        public void setAlpha(int alpha) {
            drawable.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            drawable.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return drawable.getOpacity();
        }
    }
}
