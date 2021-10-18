package com.topzi.chat.View;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class PlaceHolderEditText extends AppCompatEditText {
    // Content in the last input box
    private String lastString;
    // Position of cursor
    private int selectPosition;
    // Input Box Content Change Monitor
    private TextChangeListener listener;

    // Additional Characters
    private String item = "   ";

    public PlaceHolderEditText(Context context) {
        super(context);
        initView();
    }

    public PlaceHolderEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public PlaceHolderEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            /**
             * Callback when input box content changes
             *@ Params changed string
             *@ Cursor Subscription after Param Start Change
             *@ How many characters have been deleted by param before
             *@ How many characters have been added to param count
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                // Because setText exists after reordering
                // This will cause the input box to enter from 0, which is to avoid a series of problems.
                if (start == 0 && count > 1 && getSelectionStart() == 0) {
                    return;
                }

                String textTrim = getText().toString().trim();
                if (TextUtils.isEmpty(textTrim)) {
                    return;
                }

                // If before > 0 & & count = 0, this operation is a deletion operation.
                if (before > 0 && count == 0) {
                    selectPosition = start;
                    if (TextUtils.isEmpty(lastString)) {
                        return;
                    }
                    // Compare the last string to the changed string
                    // If the same, the blank space is deleted on behalf of this operation.
                    if (textTrim.equals(lastString.replaceAll(item, ""))) {
                        // Help the user delete the deleted character instead of the space
                        StringBuilder stringBuilder = new StringBuilder(lastString);
                        stringBuilder.deleteCharAt(start - 1);
                        selectPosition = start - 1;
                        setText(stringBuilder.toString());
                    }
                } else {
                    // This represents the addition operation.
                    // When the cursor is in front of the space, when adding characters, you need to let the cursor skip the space, and then calculate the cursor position according to the previous logic.
                    if ((start + count) % 3 == 0) {
                        selectPosition = start + count + 1;
                    } else {
                        selectPosition = start + count;
                    }
                }
            }


            @Override
            public void afterTextChanged(Editable s) {
                // Get the contents of the input box, not the blanks
                String etContent = getText().toString();
                if (TextUtils.isEmpty(etContent)) {
                    if (listener != null) {
                        listener.textChange("");
                    }
                    return;
                }
                // Restitching strings
                String newContent = addSpaceByCredit(etContent);
                // Save this string data
                lastString = newContent;

                // If there is a change, refill
                // Prevent EditText Infinite setText () from creating a dead loop
                if (!newContent.equals(etContent)) {
                    setText(newContent);
                    try {
                        // Guarantee the position of cursor
                        setSelection(selectPosition > newContent.length() ? newContent.length() : selectPosition);
                    } catch (Exception e) {
                        // When space is added just to limit the integer multiple of a character, the crossing will occur.
                        // AppLogUtil. e ("Exceeding Limited Characters");
                    }

                }
                // Triggering callback content
                if (listener != null) {
                    listener.textChange(newContent);
                }

            }
        });
    }


    /**
     * Input box content callback, triggered when input box content changes
     */
    public interface TextChangeListener {
        void textChange(String text);
    }

    public void setTextChangeListener(TextChangeListener listener) {
        this.listener = listener;

    }

    /**
     * Add a space every 4 bits
     *
     * @param content
     * @return
     */
    public String addSpaceByCredit(String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        content = content.replaceAll(item, "");
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        StringBuilder newString = new StringBuilder();
        for (int i = 1; i <= content.length(); i++) {
            if (i % 4 == 0 && i != content.length()) {
                newString.append(content.charAt(i - 1) + item);
            } else {
                newString.append(content.charAt(i - 1));
            }
        }
        return newString.toString();
    }

    /**
     * Get the input before the additional character
     * @return
     */
    public String getInputText() {
        return getText().toString().replaceAll(item, "");
    }
}
