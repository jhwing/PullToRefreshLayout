package jhw.ptr;

import android.content.res.Resources;
import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jihongwen on 15/10/21.
 */
public class DisplayUtil {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * 生成view id
     *
     * @return
     */
    public static int generateViewId() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    /**
     * 根据手机密度从 dp 的单位 转成为 px(像素)
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(float dpValue) {
        return (int) (dpValue * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 根据手机密度从 px 的单位 转成为 dp
     *
     * @param pxValue
     * @return
     */
    public static int px2dip(float pxValue) {
        return (int) (pxValue / Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param fontScale（DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(float fontScale) {
        return (int) (fontScale * Resources.getSystem().getDisplayMetrics().scaledDensity + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param fontScale（DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(float pxValue) {
        return (int) (pxValue / Resources.getSystem().getDisplayMetrics().scaledDensity + 0.5f);
    }

    /**
     * 获取手机屏幕高度
     *
     * @return heightPixels 单位px
     */
    public static int getScreenHeightPixels() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取手机屏幕宽度
     *
     * @return widthPixels 单位px
     */
    public static int getScreenWidthPixels() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

}
