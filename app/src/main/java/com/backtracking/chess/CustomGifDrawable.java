package com.backtracking.chess;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;

public class CustomGifDrawable extends ImageViewTarget<GifDrawable> {
    private final Handler handler = new Handler();
    private final Runnable checkIfAnimationDone = new Runnable() {
        @Override
        public void run() {
            GifDrawable gifDrawable = (GifDrawable) getView().getDrawable();
            if (gifDrawable != null) {
                if (gifDrawable.isRunning()) {
                    // Kiểm tra lại sau một khoảng thời gian ngắn
                    handler.postDelayed(this, 100);
                } else {
                    // GIF đã kết thúc, ẩn ImageView
                    getView().setVisibility(View.GONE);
                }
            }
        }
    };


    public CustomGifDrawable(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(GifDrawable resource) {
        // Bước 3: Thiết lập phát GIF một lần
        resource.setLoopCount(1);
        view.setImageDrawable(resource);
        resource.start();

        handler.post(checkIfAnimationDone);
    }
}
