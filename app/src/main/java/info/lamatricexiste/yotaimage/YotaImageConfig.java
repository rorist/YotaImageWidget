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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.graphics.Bitmap.CompressFormat;


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yotadevices.sdk.BackscreenLauncherConstants;
import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.BitmapUtils;
import com.yotadevices.sdk.utils.EinkUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.Math;

import com.android.camera.CropImageIntentBuilder;

public class YotaImageConfig extends Activity {

    private Intent intent = null;
    private int frWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int bsWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String TAG = "YotaImageConfig";
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_CROP_IMAGE = 2;
    protected static String PREF_IMAGE_PATH = "image_path";
    protected static String PREF_IMAGE_SIZEW = "image_sizew";
    protected static String PREF_IMAGE_SIZEH = "image_sizeh";
    protected static String PREF_IMAGE_UPDATE = "image_update";

    private SharedPreferences mPrefs;
    private String mPicturePath;
    private int mPictureW;
    private int mPictureH;
    private int mRatioX = 0;
    private int mRatioY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.yota_image_config);

        Context context = getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(YotaImageConfig.this);

        // Get widget IDs
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {

            frWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            int ids[] = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (ids != null) {
                frWidgetId = ids[0]; // Widget that is on a front screen (into YotaHub)
                bsWidgetId = ids[1]; // Widget that is on a back screen
                Log.d(TAG, "frid=" + frWidgetId + ", bsid=" + bsWidgetId);
            }
        }

        // Get widget size
        Bundle appWidgetOptions = AppWidgetManager.getInstance(context).getAppWidgetOptions(bsWidgetId);
        int size = appWidgetOptions.getInt(BackscreenLauncherConstants.OPTION_WIDGET_SIZE, -1);
        switch (size) {
            case BackscreenLauncherConstants.WIDGET_SIZE_SMALL:
                mPictureW = 476;
                mPictureH = 112;
                mRatioX = 17;
                mRatioY = 4;
                break;
            case BackscreenLauncherConstants.WIDGET_SIZE_MEDIUM:
                mPictureW = 476;
                mPictureH = 168;
                mRatioX = 17;
                mRatioY = 6;
                break;
            case BackscreenLauncherConstants.WIDGET_SIZE_LARGE:
                mPictureW = 448;
                mPictureH = 476;
                mRatioX = 16;
                mRatioY = 17;
                break;
            case BackscreenLauncherConstants.WIDGET_SIZE_EXTRA_LARGE:
                mPictureW = 540;
                mPictureH = 960;
                mRatioX = 9;
                mRatioY = 16;
                break;
            default:
                String errorStr = getString(R.string.error_widget_size);
                Log.e(TAG, errorStr);
                Toast.makeText(YotaImageConfig.this, errorStr, Toast.LENGTH_SHORT).show();
        }

        // Get default image
        ImageView imageView = (ImageView) findViewById(R.id.config_image);
        mPicturePath = mPrefs.getString(PREF_IMAGE_PATH + frWidgetId, null);
        if (mPicturePath != null) {
            Bitmap image = createBitmap(mPicturePath, mPictureW, mPictureH);
            imageView.setImageBitmap(image);
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
        }

        // Save button
        ((Button) findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save image path in preferences
                SharedPreferences.Editor edit = mPrefs.edit();

                edit.putString(PREF_IMAGE_PATH + frWidgetId, mPicturePath);
                edit.putInt(PREF_IMAGE_SIZEW + frWidgetId, mPictureW);
                edit.putInt(PREF_IMAGE_SIZEH + frWidgetId, mPictureH);
                edit.putBoolean(PREF_IMAGE_UPDATE + frWidgetId, true);

                edit.putString(PREF_IMAGE_PATH + bsWidgetId, mPicturePath);
                edit.putInt(PREF_IMAGE_SIZEW + bsWidgetId, mPictureW);
                edit.putInt(PREF_IMAGE_SIZEH + bsWidgetId, mPictureH);
                edit.putBoolean(PREF_IMAGE_UPDATE + bsWidgetId, true);

                edit.commit();

                // Leave
                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, frWidgetId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // Cancel button
        ((Button) findViewById(R.id.btn_cancel)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, frWidgetId);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        // Select image button
        ((Button) findViewById(R.id.btn_image)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        // Crop image button
        ((Button) findViewById(R.id.btn_crop)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPicturePath == null) {
                    Toast.makeText(YotaImageConfig.this, getString(R.string.error_image_crop), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    File croppedFile = File.createTempFile(getString(R.string.config_temp_file), null);
                    Uri croppedImage = Uri.fromFile(croppedFile);
                    CropImageIntentBuilder cropImage = new CropImageIntentBuilder(mRatioX, mRatioY, mPictureW, mPictureH, croppedImage);
                    cropImage.setDoFaceDetection(false);
                    cropImage.setSourceImage(Uri.fromFile(new File(mPicturePath)));
                    mPicturePath = croppedFile.getAbsolutePath();
                    Toast.makeText(YotaImageConfig.this, getString(R.string.config_crop_help), Toast.LENGTH_SHORT).show();
                    startActivityForResult(cropImage.getIntent(YotaImageConfig.this), RESULT_CROP_IMAGE);
                } catch (Exception e) {
                    Toast.makeText(YotaImageConfig.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            // Load image
            // Get image path and data
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor == null) {
                Toast.makeText(this, R.string.error_image_load, Toast.LENGTH_SHORT).show();
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mPicturePath = cursor.getString(columnIndex);
            cursor.close();

            // Show image
            Bitmap imageBitmap = createBitmap(mPicturePath, mPictureW, mPictureH);
            ImageView img = (ImageView) findViewById(R.id.config_image);
            img.setImageBitmap(imageBitmap);
            img.requestLayout();

        } else if (requestCode == RESULT_CROP_IMAGE && resultCode == RESULT_OK) {
            // Crop image
            // Show image
            Bitmap imageBitmap = createBitmap(mPicturePath, mPictureW, mPictureH);
            ImageView img = (ImageView) findViewById(R.id.config_image);
            img.setImageBitmap(imageBitmap);
            img.requestLayout();
        }

    }

    protected static Bitmap createBitmap(String path, int w, int h) {
        Bitmap imageBitmap = BitmapFactory.decodeFile(path);
        if (imageBitmap != null) {
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, w, h, false);
            imageBitmap = createBitmap(imageBitmap);
        }
        return imageBitmap;
    }

    protected static Bitmap createBitmap(Bitmap imageBitmap) {
        imageBitmap = BitmapUtils.prepareImageForBS(imageBitmap);
        imageBitmap = BitmapUtils.ditherBitmap(imageBitmap, Drawer.Dithering.DITHER_ATKINSON);
        return imageBitmap;
    }

}
