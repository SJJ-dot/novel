package sjj.novel.view.reader.utils;

import android.content.SharedPreferences;

import com.tencent.mmkv.MMKV;

public class SharedPreUtils {
    public static SharedPreferences getInstance() {
        return MMKV.mmkvWithID("sjj.novel.view.reader.record.ReadSettingManager");
    }
}
