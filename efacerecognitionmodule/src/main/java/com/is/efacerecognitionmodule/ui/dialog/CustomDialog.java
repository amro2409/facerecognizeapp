package com.is.efacerecognitionmodule.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import androidx.annotation.LayoutRes;

import com.is.efacerecognitionmodule.R;

public class CustomDialog extends Dialog {
    private final ViewGroup mContainerView;
    private boolean mIsCancelOnKey;

    private OnClickOkBtnListener mOkBtnListener;

    public CustomDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayout());

        // Initialize views
        Button okButton = findViewById(R.id.btn_ok);
        Button cancelButton = findViewById(R.id.btn_cancel);
        mContainerView=findViewById(R.id.container);

        // config dialog sho from bottom
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //getWindow().setGravity(Gravity.BOTTOM);

        // hid background
        getWindow().setWindowAnimations(android.R.style.Animation_Translucent);
        getWindow().setDimAmount(0.1f);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        setOnKeyListener((dialog, keyCode, event) -> {
            if (mIsCancelOnKey) dismiss();
            return false;
        });
        //listen for user real click on btn
        okButton.setOnClickListener(v -> {
            if (mOkBtnListener !=null){
                mOkBtnListener.onClickOk( this::dismiss);
            }
        });
        cancelButton.setOnClickListener(v -> dismiss());

    }

    public CustomDialog setCancelable1(boolean flag) {
              setCancelable(flag);
        return this;
    }

    public CustomDialog setIsCancelOnKey(boolean mIsCancelOnKey) {
        this.mIsCancelOnKey = mIsCancelOnKey;
        return this;
    }

    public CustomDialog setCustomView( @LayoutRes int lytRes){
        getLayoutInflater().inflate(lytRes,mContainerView);
        return this;
    }
    public CustomDialog setOnClickOkBtnListener(OnClickOkBtnListener okBtnListener){
        this.mOkBtnListener=okBtnListener;
        okBtnListener.initViews(mContainerView);
        return this;
    }

    interface OnClickOkBtnListener{
        void initViews(View parentView);
        void  onClickOk(OnCompletedOkBtnListener listener);
    }
    interface OnCompletedOkBtnListener{
        void  onCompletedOk();
    }

    @Override
    public void show() {
        super.show();
        animateDialogAppearance();
    }

    private void animateDialogAppearance() {
        Window window = getWindow();
        if (window == null) return;

        View dialogView = window.getDecorView();
        dialogView.setTranslationY(window.getDecorView().getHeight());
        dialogView.setScaleX(0);
        dialogView.setScaleY(0);

        int dialogHeight = dialogView.getMeasuredHeight();
        int screenHeight = getWindow().getDecorView().getHeight();
        float translationY = (screenHeight >> 1) - (dialogHeight >> 1);

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(dialogView, "translationY", translationY);
        translationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        translationAnimator.setDuration(300);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dialogView, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dialogView, "scaleY", 0f, 1f);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.setDuration(600);
        scaleY.setDuration(600);

        translationAnimator.start();
        scaleX.start();
        scaleY.start();
    }


    @Override
    public void dismiss() {
        animateDialogDismissal();
    }

    private void animateDialogDismissal() {
        Window window = getWindow();
        if (window == null) return;

        View dialogView = window.getDecorView();
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(dialogView, "alpha", 1f, 0f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(dialogView, "scaleX", 1f, 0f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(dialogView, "scaleY", 1f, 0f);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(dialogView, "rotation", 0f, 360f); // تأثير الدوران

        fadeOutAnimator.setDuration(300);
        scaleXAnimator.setDuration(300);
        scaleYAnimator.setDuration(300);
        rotationAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeOutAnimator, scaleXAnimator, scaleYAnimator, rotationAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dialogView.setVisibility(View.GONE);
                CustomDialog.super.dismiss(); // إغلاق الحوار بعد الانتهاء من التأثيرات
            }
        });
        animatorSet.start();
    }

    @LayoutRes
    int getLayout() {
        return R.layout.custom_dialog_with_btns;
    }



}
