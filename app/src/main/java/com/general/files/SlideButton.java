package com.general.files;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.taxifgo.driver.R;
import com.utils.Logger;
import com.view.MTextView;

@SuppressLint("ClickableViewAccessibility")
public class SlideButton extends RelativeLayout {

    boolean isStarted = false, isRTL = false, isFirstTouchCompleted = false;
    private LottieAnimationView lottieAnim;
    private OnClickListener clickListener;
    private RelativeLayout slideLayout;
    public MTextView btnText;
    private int xDelta, yDelta;
    private CardView cardView;
    Context mContext;
    GeneralFunctions functions;
    View nonclickable;

    public SlideButton(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public SlideButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.slide_button_layout, this);
        functions = MyApp.getInstance().getGeneralFun(mContext);
        isRTL = functions.isRTLmode();
        slideLayout = findViewById(R.id.slideLayout);
        cardView = findViewById(R.id.cardView);
        lottieAnim = findViewById(R.id.lottieAnim);
        btnText = findViewById(R.id.btnText);
        nonclickable = findViewById(R.id.nonclickable);
        //   btnText.setOnTouchListener(null);
        // btnText.setOnClickListener(null);
        nonclickable.setOnTouchListener(null);
        nonclickable.setOnClickListener(null);

        slideLayout.setOnTouchListener(onTouchListener());
        TempAnim();
    }

    private View.OnTouchListener onTouchListener() {
        return (view, event) -> {
            btnText.setEnabled(false);
            btnText.setOnTouchListener(null);

            FrameLayout.LayoutParams pr = (FrameLayout.LayoutParams) view.getLayoutParams();
            //pr.setMargins(0, 0, 0, 0);
            pr.width = cardView.getMeasuredWidth();
            pr.height = cardView.getMeasuredHeight();
            view.setLayoutParams(pr);

            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    if (isRTL) {
                        xDelta = -x - lParams.leftMargin;
                        yDelta = -y - lParams.topMargin;
                    } else {
                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                    }
                    Logger.d("ACTION_DOWN", "" + x);
                    break;

                case MotionEvent.ACTION_UP:
                    if (!isFirstTouchCompleted) {
                        isFirstTouchCompleted = true;
                    }
                    if (isRTL) {
                        if (-x >= -cardView.getMeasuredWidth() / 4) {
                            final TranslateAnimation animation = new TranslateAnimation(0, -2000, 0, 0);
                            animation.setDuration(1000);
                            animation.setRepeatCount(0);
                            animation.setRepeatMode(0);
                            animation.setFillAfter(true);
                            view.startAnimation(animation);
                            view.setVisibility(View.GONE);
                            view.setEnabled(false);
                            view.setOnTouchListener(null);
                            isStarted = true;
                            lottieAnim.setVisibility(View.VISIBLE);
                            lottieAnim.playAnimation();
                            lottieAnim.setRepeatCount(0);
                            clickListener.onClick(true);
                        } else {
                            lottieAnim.setVisibility(View.GONE);
                            view.animate().translationXBy(-view.getX()).translationX(-x - xDelta).setDuration(700).start();
                            /*FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) slideLayout.getLayoutParams();
                            layoutParams.width = cardView.getMeasuredWidth();
                            layoutParams.height = cardView.getMeasuredHeight();
                            view.setLayoutParams(layoutParams);*/
                            clickListener.onClick(false);
                        }
                    } else {
                        if (x > cardView.getMeasuredWidth() / 1.4) {
                            final TranslateAnimation animation = new TranslateAnimation(0, view.getX() * 2, 0, 0);
                            animation.setDuration(1000);
                            animation.setRepeatCount(0);
                            animation.setRepeatMode(0);
                            animation.setFillAfter(true);
                            view.startAnimation(animation);
                            view.setVisibility(View.GONE);
                            view.setEnabled(false);
                            view.setOnTouchListener(null);
                            isStarted = true;
                            lottieAnim.setVisibility(View.VISIBLE);
                            lottieAnim.playAnimation();
                            lottieAnim.setRepeatCount(0);
                            clickListener.onClick(true);
                        } else {
                            lottieAnim.setVisibility(View.GONE);
                            view.animate().translationXBy(-view.getX()).setDuration(700).start();
                            /*FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                            params.width = cardView.getMeasuredWidth();
                            params.height = cardView.getMeasuredHeight();
                            view.setLayoutParams(params);*/
                            clickListener.onClick(false);
                        }
                    }
                case MotionEvent.ACTION_MOVE:
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                    if (isRTL) {
                        if (!isFirstTouchCompleted) {
                            params.rightMargin = -x - xDelta;
                        } else {
                            params.rightMargin = -x - xDelta;
                            params.leftMargin = -x - xDelta;
                        }
                    } else {
                        if (x > 100) {
                            params.leftMargin = x - xDelta;
                            Logger.d("MotionEvent_", "x: " + event.getX() + " | xDelta: " + x + "," + params.leftMargin);
                            //params.rightMargin =0;
                        }
                    }
                    view.setLayoutParams(params);
                    btnText.setGravity(Gravity.CENTER);
                    btnText.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                    if (isRTL) {
                        if (-x > -cardView.getMeasuredWidth() / 4) {
                            if (lottieAnim.getVisibility() != View.VISIBLE) {
                                lottieAnim.setVisibility(View.VISIBLE);
                                lottieAnim.playAnimation();
                                lottieAnim.setRepeatCount(0);
                            }
                        } else {
                            lottieAnim.setVisibility(View.GONE);
                        }
                    } else {
                        if (x > cardView.getMeasuredWidth() / 1.4) {
                            if (lottieAnim.getVisibility() != View.VISIBLE) {
                                lottieAnim.setVisibility(View.VISIBLE);
                                lottieAnim.playAnimation();
                                lottieAnim.setRepeatCount(0);
                            }
                        } else {
                            lottieAnim.setVisibility(View.GONE);
                        }
                    }
                    break;
            }
            cardView.invalidate();
            return true;
        };
    }

    private void TempAnim() {
        if (isStarted) {
            slideLayout.setVisibility(View.GONE);
            return;
        }
        new Handler().postDelayed(() -> {
            if (slideLayout.getVisibility() == View.VISIBLE) {
                Animation anim = AnimationUtils.loadAnimation(mContext, isRTL ? R.anim.slide_right_to_left : R.anim.slide_left_to_right);
                findViewById(R.id.frmView).startAnimation(anim);
                new Handler().postDelayed(this::TempAnim, 100);
            }
        }, 8000);
    }

    public interface OnClickListener {
        void onClick(boolean isCompleted);
    }

    public void onClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setButtonText(String str) {
        btnText.setText(str);
    }

    public void setBackgroundColor(int color) {
        cardView.setBackgroundColor(color);
        slideLayout.setBackgroundColor(color);
    }

    public void resetButtonView(String buttonText) {
        if (isRTL) {
            isFirstTouchCompleted = false;
            final TranslateAnimation an = new TranslateAnimation(-slideLayout.getX(), 0, 0, 0);
            an.setDuration(500);
            an.setRepeatCount(0);
            an.setRepeatMode(0);
            an.setFillAfter(true);
            slideLayout.startAnimation(an);
            setButtonText(buttonText);
            isStarted = false;
            lottieAnim.setVisibility(View.GONE);
            slideLayout.setVisibility(View.VISIBLE);
            slideLayout.setEnabled(true);
            slideLayout.setOnTouchListener(onTouchListener());
            slideLayout.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, -cardView.getX() - slideLayout.getX(), 0, 0.5f, 0, 0, 1, 1, 0, 0));
        } else {
            isFirstTouchCompleted = false;
            final TranslateAnimation an = new TranslateAnimation(slideLayout.getX(), 0, 0, 0);
            an.setDuration(500);
            an.setRepeatCount(0);
            an.setRepeatMode(0);
            an.setFillAfter(true);
            slideLayout.startAnimation(an);
            setButtonText(buttonText);
            isStarted = false;
            lottieAnim.setVisibility(View.GONE);
            slideLayout.setVisibility(View.VISIBLE);
            slideLayout.setEnabled(true);
            slideLayout.setOnTouchListener(onTouchListener());
            slideLayout.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, cardView.getX(), 0, 0.5f, 0, 0, 1, 1, 0, 0));
        }
    }
}
