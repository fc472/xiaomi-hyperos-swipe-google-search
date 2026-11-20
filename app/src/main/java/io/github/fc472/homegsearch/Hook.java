package io.github.fc472.homegsearch;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Home Swipe-Up -> Google Search
 *
 * Scope: com.miui.home (HyperOS 3 system launcher, phone + pad).
 * Swiping up on the home screen sends android.search.action.GLOBAL_SEARCH
 * to the Google app, which opens its search box with the keyboard shown,
 * like the swipe-up search on Global ROM launchers.
 *
 * Hook targets (class / method names) were located with reference to
 * HyperCeiler's ScreenSwipe rule (https://github.com/ReChronoRain/HyperCeiler).
 */
public class Hook implements IXposedHookLoadPackage {
    private static final String TAG = "[HomeGSearch] ";
    private static final String HOME = "com.miui.home";
    private static final String GOOGLE = "com.google.android.googlequicksearchbox";
    private static final String ACTION = "android.search.action.GLOBAL_SEARCH";
    private static final int GESTURE_UP = 10;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) {
        if (!HOME.equals(lp.packageName)) return;
        final ClassLoader cl = lp.classLoader;
        XposedBridge.log(TAG + "loaded in " + lp.processName);

        // 1) Swipe up on workspace -> Google search
        hook(cl, "com.miui.home.launcher.Workspace", "onVerticalGesture",
                new Object[]{int.class, MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                try {
                    int type = (Integer) p.args[0];
                    if (type != GESTURE_UP) return;
                    MotionEvent ev = (MotionEvent) p.args[1];
                    if (ev != null && ev.getPointerCount() != 1) return;
                    try {
                        Object editing = XposedHelpers.callMethod(p.thisObject, "isInNormalEditingMode");
                        if (editing instanceof Boolean && (Boolean) editing) return;
                    } catch (Throwable ignored) { }
                    Context ctx = ((View) p.thisObject).getContext();
                    Intent i = new Intent(ACTION);
                    i.setPackage(GOOGLE);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(i);
                    p.setResult(true);
                } catch (Throwable t) {
                    XposedBridge.log(TAG + "launch failed, fallback to stock: " + t);
                }
            }
        }});

        // 2) Disable Xiaomi's own swipe-up global search so it does not compete
        XC_MethodHook returnFalse = new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) { p.setResult(false); }
        };
        boolean ok = hook(cl, "com.miui.home.launcher.DeviceConfig", "isGlobalSearchEnable",
                new Object[]{Context.class, returnFalse});
        if (ok) {
            hook(cl, "com.miui.home.launcher.search.SearchEdgeLayout", "isBottomGlobalSearchEnable",
                    new Object[]{returnFalse});
            hook(cl, "com.miui.home.launcher.DeviceConfig", "isGlobalSearchBottomEffectEnable",
                    new Object[]{Context.class, returnFalse});
        } else {
            hook(cl, "com.miui.home.launcher.DeviceConfig", "allowedSlidingUpToStartGolbalSearch",
                    new Object[]{Context.class, returnFalse});
        }

        // 3) Stock slide-up gesture -> no_action
        hook(cl, "com.miui.home.launcher.allapps.LauncherMode", "getSlideUpGesture",
                new Object[]{Context.class, new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) { p.setResult("no_action"); }
        }});
    }

    private static boolean hook(ClassLoader cl, String cls, String method, Object[] typesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(cls, cl, method, typesAndCallback);
            XposedBridge.log(TAG + "hooked " + cls + "#" + method);
            return true;
        } catch (Throwable t) {
            XposedBridge.log(TAG + "skip " + cls + "#" + method + " : " + t);
            return false;
        }
    }
}
