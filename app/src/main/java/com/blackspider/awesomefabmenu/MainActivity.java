package com.blackspider.awesomefabmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.ShapeBadgeItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.blackspider.util.helper.AnimatorUtils;
import com.blackspider.util.lib.archlayout.ArcLayout;
import com.blackspider.util.widget.ClipRevealFrame;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BottomNavigationBar.OnTabSelectedListener{

    BottomNavigationBar bottomNavigationBar;
    @Nullable
    TextBadgeItem numberBadgeItem;
    @Nullable
    ShapeBadgeItem shapeBadgeItem;
    int lastSelectedPosition = 0;

    Toast toast = null;
    ImageView fab;
    Button btnShoutout, btnAsk, btnPoll;
    View rootLayout;
    ClipRevealFrame menuLayout;
    ArcLayout arcLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        fab = findViewById(R.id.fab);
        btnShoutout = findViewById(R.id.btn_shoutout);
        btnAsk = findViewById(R.id.btn_ask);
        btnPoll = findViewById(R.id.btn_poll);
        menuLayout = findViewById(R.id.menu_layout);
        rootLayout = findViewById(R.id.root_layout);
        arcLayout = findViewById(R.id.arc_layout);

        btnShoutout.setOnClickListener(this);
        btnAsk.setOnClickListener(this);
        btnPoll.setOnClickListener(this);
        fab.setOnClickListener(this);
        bottomNavigationBar.setTabSelectedListener(this);

        refresh();

        loadGifIconWithInterval();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                onFabClick(v);
                return;

            case R.id.btn_shoutout:
                showToast("Shoutout");
                return;

            case R.id.btn_ask:
                showToast("Ask question");
                return;

            case R.id.btn_poll:
                showToast("Create poll");
                return;
        }
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        refresh();
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    private void showToast(String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();

    }

    private void onFabClick(View v) {
        int x = (v.getLeft() + v.getRight()) / 2;
        int y = (v.getTop() + v.getBottom()) / 2;
        float radiusOfFab = 1f * v.getWidth() / 2f;
        float radiusFromFabToRoot = (float) Math.hypot(
                Math.max(x, rootLayout.getWidth() - x),
                Math.max(y, rootLayout.getHeight() - y));

        if (v.isSelected()) {
            hideMenu(x, y, radiusFromFabToRoot, radiusOfFab);
        } else {
            showMenu(x, y, radiusOfFab, radiusFromFabToRoot);
        }

        v.setSelected(!v.isSelected());
    }

    private void showMenu(int cx, int cy, float startRadius, float endRadius) {
        menuLayout.setVisibility(View.VISIBLE);

        List<Animator> animList = new ArrayList<>();

        Animator revealAnim = createCircularReveal(menuLayout, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        //revealAnim.setDuration(400);
        animList.add(revealAnim);

        for (int i = 0, len = arcLayout.getChildCount(); i < len; i++) {
            animList.add(createShowItemAnimator(arcLayout.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(1000);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.playTogether(animList);
        animSet.start();
    }

    private void hideMenu(int cx, int cy, float startRadius, float endRadius) {

        List<Animator> animList = new ArrayList<>();

        for (int i = arcLayout.getChildCount() - 1; i >= 0; i--) {
            animList.add(createHideItemAnimator(arcLayout.getChildAt(i)));
        }

        Animator revealAnim = createCircularReveal(menuLayout, cx, cy, startRadius, endRadius);
        revealAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        //revealAnim.setDuration(400);
        revealAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                menuLayout.setVisibility(View.INVISIBLE);
            }
        });

        animList.add(revealAnim);

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(600);
        animSet.setInterpolator(new AnticipateInterpolator());
        animSet.playTogether(animList);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                menuLayout.setVisibility(View.INVISIBLE);
            }
        });
        animSet.start();
    }

    private Animator createShowItemAnimator(View item) {

        float dx = fab.getX() - item.getX();
        float dy = fab.getY() - item.getY();

        item.setRotation(0f);
        item.setTranslationX(dx);
        item.setTranslationY(dy);

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(0f, 360f),
                AnimatorUtils.translationX(dx, 0f),
                AnimatorUtils.translationY(dy, 0f)
        );

        return anim;
    }

    private Animator createHideItemAnimator(final View item) {
        float dx = fab.getX() - item.getX();
        float dy = fab.getY() - item.getY();

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(360f, 0f),
                AnimatorUtils.translationX(0f, dx),
                AnimatorUtils.translationY(0f, dy)
        );

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                item.setTranslationX(0f);
                item.setTranslationY(0f);
            }
        });

        return anim;
    }

    private Animator createCircularReveal(final ClipRevealFrame view, int x, int y, float startRadius,
                                          float endRadius) {
        final Animator reveal;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reveal = ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
        } else {
            view.setClipOutLines(true);
            view.setClipCenter(x, y);
            reveal = ObjectAnimator.ofFloat(view, "ClipRadius", startRadius, endRadius);
            reveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setClipOutLines(false);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return reveal;
    }

    private void refresh() {

        bottomNavigationBar.clearAll();

        numberBadgeItem = new TextBadgeItem()
                .setBorderWidth(4)
                .setBackgroundColorResource(R.color.blue)
                .setText("" + lastSelectedPosition)
                .setHideOnSelect(false);

        shapeBadgeItem = new ShapeBadgeItem()
                .setShape(ShapeBadgeItem.SHAPE_HEART)
                .setShapeColorResource(R.color.teal)
                .setGravity(Gravity.TOP | Gravity.END)
                .setHideOnSelect(false);

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);


        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_home2, "").setActiveColorResource(R.color.orange))
                .addItem(new BottomNavigationItem(R.drawable.ic_notification, "").setActiveColorResource(R.color.teal).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_circle, "").setActiveColorResource(R.color.invisible))
                .addItem(new BottomNavigationItem(R.drawable.ic_account, "").setActiveColorResource(R.color.blue).setBadgeItem(shapeBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_friends, "").setActiveColorResource(R.color.primary))
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
    }

    private void loadGifIconWithInterval(){
        new CountDownTimer(60000, 500) {
            int count = 0, animBreak = 5000;
            boolean animated = false;

            public void onTick(long millisUntilFinished) {
                count+=500;
                final RequestOptions requestOptions;
                if(count>animBreak && (count%animBreak==0)){
                    requestOptions = new RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE);
                    animated = true;
                    show(fab, requestOptions);
                }
                else {
                    requestOptions = new RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .dontAnimate();

                    if(animated){
                        animated = false;
                        show(fab, requestOptions);
                    }
                }
            }

            public void onFinish() {
                loadGifIconWithInterval();
            }

        }.start();
    }

    void show(final ImageView view, RequestOptions requestOptions){
        Glide.with(MainActivity.this)
                .load(R.drawable.ic_curios)
                .apply(requestOptions)
                .into(view);
    }
}
