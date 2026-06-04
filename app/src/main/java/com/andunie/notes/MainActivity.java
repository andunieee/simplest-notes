package com.andunie.notes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowInsets;
import android.widget.EditText;

/**
 * The whole app: one text box, saved whenever the activity leaves the foreground. Built in code with
 * framework widgets only (no layout inflation, no support libraries) to keep cold start as fast as possible.
 */
public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float dp = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * dp);
        prefs = getSharedPreferences("notes", MODE_PRIVATE);

        input = new EditText(this);
        input.setHint(R.string.hint);
        input.setTextSize(18);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setBackground(null);
        input.setPadding(pad, pad, pad, pad);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(prefs.getString("text", ""));
        input.setSelection(input.length());

        // Edge-to-edge is enforced on targetSdk 35+: keep content clear of system bars and keyboard.
        input.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets in = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                v.setPadding(in.left + pad, in.top + pad, in.right + pad, in.bottom + pad);
                return WindowInsets.CONSUMED;
            }
            // API 26-29: WindowInsets.Type doesn't exist; system window insets include the IME.
            v.setPadding(insets.getSystemWindowInsetLeft() + pad, insets.getSystemWindowInsetTop() + pad,
                    insets.getSystemWindowInsetRight() + pad, insets.getSystemWindowInsetBottom() + pad);
            return insets.consumeSystemWindowInsets();
        });
        setContentView(input);

        input.requestFocus();
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().getInsetsController().show(WindowInsets.Type.ime());
        }
    }

    /** {@code commit()} so the text is on disk before the process can be torn down. */
    @Override
    protected void onPause() {
        super.onPause();
        String text = input.getText().toString();
        if (!text.equals(prefs.getString("text", ""))) {
            prefs.edit().putString("text", text).commit();
        }
    }
}
