package com.zhongzewei.widget;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Customized numeric soft keyboard. EditText registered with this NumberKeyboard can get input from the soft keyboard,
 * which will replace system default one. And the system default soft keyboard will not appear when touch EditText.
 *
 * Created by Zhong.Zewei on 14/11/16.
 */
public class NumberKeyboard {

    public static final int CODE_DELETE = -5;
    public static final int CODE_CLEAR = 5506;
    public static final int CODE_00 = 4848;

    /** A link to the KeyboardView that is used to render this custom keyboard. */
    private KeyboardView mKeyboardView;
    /** A link to the activity that hosts the {@link #mKeyboardView}. */
    private Activity mHostActivity;
    /** A link to the view that has current focus. */
    private static View mFocusCurrent;

    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            // Get the EditText and its Editable
            // When the NumberKeyboard is not hold by Activity, we cannot get focus. Then we use the mFocusCurrent object.
            View focusCurrent = mHostActivity.getCurrentFocus();
            if (focusCurrent==null || focusCurrent.getClass()!=EditText.class){
                focusCurrent = mFocusCurrent;
            }
            if( focusCurrent==null || focusCurrent.getClass()!=EditText.class )
                return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();

            // Handle key
            if( primaryCode== CODE_DELETE) {
                if( editable!=null && start>0 ) {
                    editable.delete(start - 1, start);
                }
            } else if( primaryCode== CODE_CLEAR) {
                if( editable!=null ) {
                    editable.clear();
                }
            } else if (primaryCode == CODE_00){
                editable.insert(start, "00");
            } else {
                // Insert character
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override
        public void onPress(int arg0) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }
    };

    public NumberKeyboard(Activity hostActivity){
        mHostActivity = hostActivity;
    }

    public NumberKeyboard(Activity hostActivity, int keyboardViewId, int keyboardLayoutId) {
        mHostActivity = hostActivity;
        setKeyboardView( null, keyboardViewId, keyboardLayoutId);
    }

    public void setKeyboardView(KeyboardView keyboardView, int keyboardViewId, int keyboardLayoutId){
        mKeyboardView = (KeyboardView)mHostActivity.findViewById(keyboardViewId);
        if ( mKeyboardView==null ){
            mKeyboardView = keyboardView;
        }
        if ( mKeyboardView==null){
            return;
        }
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, keyboardLayoutId));
        mKeyboardView.setPreviewEnabled(false);//不显示keyPreviewLayout
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        // Hide the standard keyboard initially
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Register <var>EditText<var> with resource id <var>resIßd</var> (on the hosting activity) for using this number keyboard.
     *
     * @param resId The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resId) {
        EditText editText = (EditText)mHostActivity.findViewById(resId);
        registerEditText(editText);
    }

    /**
     * Register EditText to NumberKeyboard, then the EditText will not call default soft keyboard. Will
     * get input content from the NumberKeyboard.
     *
     * @param editText the EditText that NumberKeyboard will listen.
     */
    public void registerEditText(EditText editText){

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        editText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                int touchPosition = edittext.getOffsetForPosition(event.getX(), event.getY());
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                if (touchPosition>0){
                    edittext.setSelection(touchPosition);   // Set touch position
                }
                setCurrentFocusEditText(v);                 // Set current EditText
                return true;                                // Consume touch event
            }
        });
        // Disable spell check
        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    private synchronized void setCurrentFocusEditText(View editText) {
        if (mFocusCurrent != null && mFocusCurrent != editText) {
            mFocusCurrent.clearFocus();
        }
        mFocusCurrent = editText;
    }
}
