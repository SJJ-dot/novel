package sjj.novel.view.reader.record;

import android.content.Context;
import android.content.SharedPreferences;

import sjj.novel.view.reader.page.PageMode;
import sjj.novel.view.reader.page.PageStyle;
import sjj.novel.view.reader.utils.ScreenUtils;
import sjj.novel.view.reader.utils.SharedPreUtils;

/**
 * Created by newbiechen on 17-5-17.
 * 阅读器的配置管理
 */

public class ReadSettingManager {

    public static final String SHARED_READ_BG = "shared_read_bg_name";
    public static final String SHARED_READ_BRIGHTNESS = "shared_read_brightness";
    public static final String SHARED_READ_IS_BRIGHTNESS_AUTO = "shared_read_is_brightness_auto";
    public static final String SHARED_READ_TEXT_SIZE = "shared_read_text_size";
    public static final String SHARED_READ_IS_TEXT_DEFAULT = "shared_read_text_default";
    public static final String SHARED_READ_PAGE_MODE = "shared_read_mode";
    public static final String SHARED_READ_NIGHT_MODE = "shared_night_mode";
    public static final String SHARED_READ_VOLUME_TURN_PAGE = "shared_read_volume_turn_page";
    public static final String SHARED_READ_FULL_SCREEN = "shared_read_full_screen";
    public static final String SHARED_READ_CONVERT_TYPE = "shared_read_convert_type";

    private SharedPreferences sharedPreUtils;
    private ScreenUtils screenUtils;

    public ReadSettingManager(Context context) {
        sharedPreUtils = SharedPreUtils.getInstance(context);
        screenUtils = new ScreenUtils(context);
    }

    public void setPageStyle(PageStyle pageStyle) {
        sharedPreUtils.edit().putString(SHARED_READ_BG, pageStyle.name()).apply();
    }

    public void setBrightness(int progress) {
        sharedPreUtils.edit().putInt(SHARED_READ_BRIGHTNESS, progress).apply();
    }

    public void setAutoBrightness(boolean isAuto) {
        sharedPreUtils.edit().putBoolean(SHARED_READ_IS_BRIGHTNESS_AUTO, isAuto).apply();
    }

    public void setDefaultTextSize(boolean isDefault) {
        sharedPreUtils.edit().putBoolean(SHARED_READ_IS_TEXT_DEFAULT, isDefault).apply();
    }


    public void setPageMode(PageMode mode) {
        sharedPreUtils.edit().putInt(SHARED_READ_PAGE_MODE, mode.ordinal()).apply();
    }

    public void setNightMode(boolean isNight) {
        sharedPreUtils.edit().putBoolean(SHARED_READ_NIGHT_MODE, isNight).apply();
    }

    public int getBrightness() {
        return sharedPreUtils.getInt(SHARED_READ_BRIGHTNESS, 40);
    }

    public boolean isBrightnessAuto() {
        return sharedPreUtils.getBoolean(SHARED_READ_IS_BRIGHTNESS_AUTO, false);
    }


    public void setTextSize(int textSize) {
        sharedPreUtils.edit().putInt(SHARED_READ_TEXT_SIZE, textSize).apply();
    }

    public int getTextSize() {
        return sharedPreUtils.getInt(SHARED_READ_TEXT_SIZE, screenUtils.spToPx(28));
    }

    public boolean isDefaultTextSize() {
        return sharedPreUtils.getBoolean(SHARED_READ_IS_TEXT_DEFAULT, false);
    }

    public PageMode getPageMode() {
        int mode = sharedPreUtils.getInt(SHARED_READ_PAGE_MODE, PageMode.SIMULATION.ordinal());
        return PageMode.values()[mode];
    }

    public PageStyle getPageStyle() {
        String style = sharedPreUtils.getString(SHARED_READ_BG, PageStyle.BG_def.name());
        return PageStyle.valueOf(style);
    }

    public boolean isNightMode() {
        return sharedPreUtils.getBoolean(SHARED_READ_NIGHT_MODE, false);
    }

    public void setVolumeTurnPage(boolean isTurn) {
        sharedPreUtils.edit().putBoolean(SHARED_READ_VOLUME_TURN_PAGE, isTurn).apply();
    }

    public boolean isVolumeTurnPage() {
        return sharedPreUtils.getBoolean(SHARED_READ_VOLUME_TURN_PAGE, false);
    }

    public void setFullScreen(boolean isFullScreen) {
        sharedPreUtils.edit().putBoolean(SHARED_READ_FULL_SCREEN, isFullScreen).apply();
    }

    public boolean isFullScreen() {
        return sharedPreUtils.getBoolean(SHARED_READ_FULL_SCREEN, false);
    }

    public void setConvertType(int convertType) {
        sharedPreUtils.edit().putInt(SHARED_READ_CONVERT_TYPE, convertType).apply();
    }

    public int getConvertType() {
        return sharedPreUtils.getInt(SHARED_READ_CONVERT_TYPE, 0);
    }
}
