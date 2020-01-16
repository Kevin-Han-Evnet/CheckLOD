package lib.netmania.dcamera.view;

/**
 * Created by hansangcheol on 16. 9. 21..
 */
/** Custom your own ViewPager to extends support ViewPager. java source: */
/** Created by azi on 2013-6-21.  */

        import android.content.Context;
        import android.util.AttributeSet;
        import android.view.MotionEvent;

public class ViePagerFixed extends android.support.v4.view.ViewPager {

    public ViePagerFixed(Context context) {
        super(context);
    }

    public ViePagerFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}