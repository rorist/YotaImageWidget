/*

  Copyright (C) 2015 Aubort Jean-Baptiste (Rorist)

  This file is part of YotaImageWidget.

  YotaImageWidget is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  YotaImageWidget is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with YotaImageWidget.  If not, see <http://www.gnu.org/licenses/>.
*/
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

import java.util.Arrays;

import com.yotadevices.sdk.BackscreenLauncherConstants;
import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.BitmapUtils;
import com.yotadevices.sdk.utils.EinkUtils;

public class YotaImageWidget extends AppWidgetProvider {

    private static String TAG = "YotaImageWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Update all widgets
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Log.d(TAG, "onUpdate="+appWidgetIds[i]);
            updateAppWidget(context, appWidgetManager, appWidgetIds[i], true);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (BackscreenLauncherConstants.ACTION_APPWIDGET_VISIBILITY_CHANGED.equals(intent.getAction())) {
            Log.d(TAG, "onReceive visibility changed");
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(BackscreenLauncherConstants.ACTION_APPWIDGET_EXTRA_VISIBLE)) {

                // Get all widgets
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisWidget = new ComponentName(context, YotaImageWidget.class);
                int[] thisClassWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                boolean isWidgetVisible = false;

                // Find all visible widgets
                final int[] visibleAppWidgetIds = extras.getIntArray(BackscreenLauncherConstants.ACTION_APPWIDGET_EXTRA_VISIBLE);
                for (int i = 0; i < visibleAppWidgetIds.length; i++) {
                    isWidgetVisible = Arrays.binarySearch(thisClassWidgetIds, visibleAppWidgetIds[i]) >= 0;
                    if (isWidgetVisible) {
                        updateAppWidget(context, appWidgetManager, visibleAppWidgetIds[i], false);
                    }
                }
            }
        }

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // Remove preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            edit.remove(YotaImageConfig.PREF_IMAGE_PATH + appWidgetIds[i]);
            edit.remove(YotaImageConfig.PREF_IMAGE_SIZEW + appWidgetIds[i]);
            edit.remove(YotaImageConfig.PREF_IMAGE_SIZEH + appWidgetIds[i]);
            Log.d(TAG, "onDeleted="+appWidgetIds[i]);
        }
        edit.commit();
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, boolean forceUpdate) {

        // Get prefs and check for update
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean needsUpdate = prefs.getBoolean(YotaImageConfig.PREF_IMAGE_UPDATE + appWidgetId, true);
        if (!needsUpdate && !forceUpdate) {
            Log.d(TAG, "Doesnt need update("+needsUpdate+","+forceUpdate+")="+appWidgetId);
            return;
        }
        Log.d(TAG, "onUpdateAppWidget("+needsUpdate+","+forceUpdate+")=" + appWidgetId);

        // Get image
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.yota_image);
        String imagePath = prefs.getString(YotaImageConfig.PREF_IMAGE_PATH + appWidgetId, null);
        int imageW = prefs.getInt(YotaImageConfig.PREF_IMAGE_SIZEW + appWidgetId, 476);
        int imageH = prefs.getInt(YotaImageConfig.PREF_IMAGE_SIZEH + appWidgetId, 112);

        // Create image and display it
        if (imagePath != null) {
            Bitmap imageBitmap = YotaImageConfig.createBitmap(imagePath, imageW, imageH);
            views.setImageViewBitmap(R.id.widget_image, imageBitmap);
        } else {
            views.setImageViewResource(R.id.widget_image, R.drawable.placeholder);
        }
        // Save state
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(YotaImageConfig.PREF_IMAGE_UPDATE + appWidgetId, false);
        edit.commit();

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}

