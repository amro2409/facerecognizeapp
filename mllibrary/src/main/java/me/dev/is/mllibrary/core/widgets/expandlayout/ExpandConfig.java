package me.dev.is.mllibrary.core.widgets.expandlayout;

import android.view.ViewGroup;

public class ExpandConfig {

    static interface ExpandState {
        static final int PRE_INIT = -1;
        static final int CLOSED = 0;
        static final int EXPANDED = 1;
        static final int EXPANDING = 2;
        static final int CLOSING = 3;
    }

    public static class ScrolledParent {
        ViewGroup scrolledView;
        int childBetweenParentCount;
    }

    public static class Settings {
        static final int EXPAND_DURATION = 300;
        int expandDuration = EXPAND_DURATION;
        boolean expandWithParentScroll;
        boolean expandScrollTogether;
    }
}
