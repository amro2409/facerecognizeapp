package me.dev.is.mllibrary.core.widgets.expandlayout;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Created by SilenceDut on 16/8/21.
 */

class Utils {

    @Nullable
    static ExpandConfig.ScrolledParent getScrolledParent (@NotNull ViewGroup child) {

        ViewParent parent= child.getParent();
        int childBetweenParentCount =0;
        while (parent!=null){
            if((parent instanceof RecyclerView || parent instanceof AbsListView)) {
                ExpandConfig.ScrolledParent scrolledParent = new ExpandConfig.ScrolledParent();
                scrolledParent.scrolledView = (ViewGroup)parent;
                scrolledParent.childBetweenParentCount =childBetweenParentCount;
                return scrolledParent;
            }
            childBetweenParentCount++;
            parent = parent.getParent();
        }
        return null;
    }

    static ValueAnimator createParentAnimator(final View parent, int distance , long duration) {

        ValueAnimator parentAnimator = ValueAnimator.ofInt(0,distance);

        parentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int lastDy;
            int dy;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dy = (int)animation.getAnimatedValue()-lastDy;
                lastDy = (int)animation.getAnimatedValue();
                parent.scrollBy(0,dy);
            }
        });
        parentAnimator.setDuration(duration);

        return  parentAnimator;
    }




}
