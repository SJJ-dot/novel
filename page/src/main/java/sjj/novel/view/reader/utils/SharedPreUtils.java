package sjj.novel.view.reader.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreUtils {
    public static SharedPreferences getInstance(Context context) {
        return context.getSharedPreferences("sjj.novel.view.reader.record.ReadSettingManager", Context.MODE_PRIVATE);
    }
}
