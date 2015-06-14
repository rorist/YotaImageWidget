package info.lamatricexiste.yotaimage;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.yotadevices.sdk.BackscreenLauncherConstants;
import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.BitmapUtils;

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

        Log.v(TAG, "onUpdateAppWidget");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.yota_image);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String imagePath = prefs.getString(YotaImageConfig.PREF_IMAGE_PATH, null);
        if (imagePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 32;
            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, options);
            BitmapUtils.ditherBitmap(imageBitmap, Drawer.Dithering.DITHER_ATKINSON);
            views.setImageViewBitmap(R.id.widget_image, imageBitmap);
        } else {
            views.setImageViewResource(R.id.widget_image, R.drawable.placeholder);
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

}

