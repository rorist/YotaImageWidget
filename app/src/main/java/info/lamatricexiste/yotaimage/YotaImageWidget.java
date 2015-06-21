package info.lamatricexiste.yotaimage;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.sdk.BackscreenLauncherConstants;
import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.BitmapUtils;
import com.yotadevices.sdk.utils.EinkUtils;

public class YotaImageWidget extends AppWidgetProvider {

    private static String TAG = "YotaImageWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.v(TAG, "onUpdate");

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (BackscreenLauncherConstants.ACTION_APPWIDGET_VISIBILITY_CHANGED.equals(intent.getAction())) {
            Log.v(TAG, "onReceive visibility changed");
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(BackscreenLauncherConstants.ACTION_APPWIDGET_EXTRA_VISIBLE)) {

                final int[] visibleWidgetIds = extras.getIntArray(BackscreenLauncherConstants.ACTION_APPWIDGET_EXTRA_VISIBLE);
                if (visibleWidgetIds == null || visibleWidgetIds.length <= 0) return;

                // Array of all BSTimeWidget widget IDs
                int[] thisWidgetsIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, YotaImageWidget.class));
                if (thisWidgetsIds == null) {
                    return;
                }

                // Find all visible BSTimeWidget widgets
                int visibleCount = 0;
                for (int i = 0; i < thisWidgetsIds.length; i++)
                    for (int j = 0; j < visibleWidgetIds.length; j++)
                        if (thisWidgetsIds[i] == visibleWidgetIds[j]) {
                            updateAppWidget(context, AppWidgetManager.getInstance(context), thisWidgetsIds[i]);
                            break;
                        }
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.v(TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.v(TAG, "onDisabled");
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.v(TAG, "onUpdateAppWidget=" + appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.yota_image);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String imagePath = prefs.getString(YotaImageConfig.PREF_IMAGE_PATH + appWidgetId, null);
        if (imagePath != null) {
            Bitmap imageBitmap = YotaImageConfig.createBitmap(imagePath);
            views.setImageViewBitmap(R.id.widget_image, imageBitmap);
        } else {
            views.setImageViewResource(R.id.widget_image, R.drawable.placeholder);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}

