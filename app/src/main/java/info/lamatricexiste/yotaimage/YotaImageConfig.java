package info.lamatricexiste.yotaimage;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class YotaImageConfig extends Activity {

    private Intent intent = null;
    private int frWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int bsWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private static String TAG = "YotaImageConfig";
    private static int RESULT_LOAD_IMAGE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.yota_image_config);
        Bundle extras = this.getIntent().getExtras();

        if (extras != null) {

            frWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            int ids[] = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (ids != null) {
                frWidgetId = ids[0]; // Widget that is on a front screen (into YotaHub)
                bsWidgetId = ids[1]; // Widget that is on a back screen
                Log.v(TAG, "frid=" + frWidgetId + ", bsid=" + bsWidgetId);
            }
        }

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
            InputStream is = null;
            try {
                is = getApplicationContext().getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap image = BitmapFactory.decodeStream(is);

            /*
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap image = BitmapFactory.decodeFile(picturePath);
            */

            ((ImageView) findViewById(R.id.image)).setImageBitmap(image);

            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, frWidgetId);
            setResult(RESULT_OK, intent);
            //finish();
        }

    }
}
