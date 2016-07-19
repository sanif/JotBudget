package com.bestapps.jotbudget.db;

import android.text.TextUtils;
import java.util.Collections;

class DbUtil {
    private DbUtil() {

    }
    static String makePlaceholders(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count <= 0");
        }
        return TextUtils.join(",", Collections.nCopies(count, "?"));
    }
}
