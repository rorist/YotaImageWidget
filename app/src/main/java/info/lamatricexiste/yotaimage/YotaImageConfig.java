package info.lamatricexiste.yotaimage;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.BitmapUtils;
import com.yotadevices.sdk.utils.EinkUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

import android.net.Uri;

public class YotaImageConfig extends Activity {

    private Intent intent = null;
    private int frWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int bsWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String TAG = "YotaImageConfig";
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_CROP_IMAGE = 2;
    protected static String PREF_IMAGE_PATH = "image_path";

    private SharedPreferences mPrefs;
    private String mPicturePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.yota_image_config);

        ((RadioButton) findViewById(R.id.radio_small)).setChecked(true);

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

        // Get default image
        ImageView imageView = (ImageView) findViewById(R.id.config_image);
        mPicturePath = mPrefs.getString(PREF_IMAGE_PATH + frWidgetId, null);
        if (mPicturePath != null) {
            Bitmap image = createBitmap(mPicturePath);
            imageView.setImageBitmap(image);
        } else {
            imageView.setImageDrawable(getDrawable(R.drawable.placeholder));
        }

        // Save button
        ((Button) findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save image path in preferences
                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString(PREF_IMAGE_PATH + frWidgetId, mPicturePath);
                edit.putString(PREF_IMAGE_PATH + bsWidgetId, mPicturePath);
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

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

            // Crop image // FIXME doesnt work yet
            /*
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(selectedImage, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 128);
            cropIntent.putExtra("outputY", 128);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, RESULT_CROP_IMAGE);
            */

            // Save image in external storage
            // TODO

            // Show image
            Bitmap imageBitmap = createBitmap(mPicturePath);
            ((ImageView) findViewById(R.id.config_image)).setImageBitmap(imageBitmap);
        } else {
            if (data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = extras.getParcelable("data");
                ((ImageView) findViewById(R.id.config_image)).setImageBitmap(imageBitmap);
            }
        }

    }

    protected static Bitmap createBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap imageBitmap = BitmapFactory.decodeFile(path, options);
        imageBitmap = createBitmap(imageBitmap);
        return imageBitmap;
    }

    protected static Bitmap createBitmap(Bitmap imageBitmap) {
        imageBitmap = BitmapUtils.prepareImageForBS(imageBitmap);
        imageBitmap = BitmapUtils.ditherBitmap(imageBitmap, Drawer.Dithering.DITHER_ATKINSON);
        return imageBitmap;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_small:
                if (checked){
                    setImageSize(476, 112);
                }
                break;
            case R.id.radio_medium:
                if (checked){
                    setImageSize(476, 168);
                }
                break;
            case R.id.radio_large:
                if (checked){
                    setImageSize(448, 476);
                }
                break;
            case R.id.radio_extra_large:
                if (checked){
                    setImageSize(960, 540);
                }
                break;
        }
    }

    private void setImageSize(int w, int h) {
        ImageView img = (ImageView) findViewById(R.id.config_image);
        img.setAdjustViewBounds(true);
        img.setMaxWidth(w);
        img.setMaxHeight(h);
    }

}
