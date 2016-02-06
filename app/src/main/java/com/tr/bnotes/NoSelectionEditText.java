package com.tr.bnotes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * {@link EditText} with disabled opportunity to change the cursor position.
 * Courtesy: http://stackoverflow.com/a/12982251
 */
public class NoSelectionEditText extends EditText {

    public NoSelectionEditText(Context context) {
        super(context);
    }

    public NoSelectionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoSelectionEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onSelectionChanged(int start, int end) {
        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }
        super.onSelectionChanged(start, end);
    }

}