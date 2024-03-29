package com.sba.sinhalaphotoeditor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.sba.sinhalaphotoeditor.sdk.Methods;
import com.sba.sinhalaphotoeditor.R;
import com.sba.sinhalaphotoeditor.singleton.ImageList;

import java.util.Locale;

import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_SINHALA;
import static com.sba.sinhalaphotoeditor.config.Constants.SHARED_PREF_NAME;


public class CreateABackgroundActivity extends AppCompatActivity {

    private ImageView userCreatedImage;
    private Button create;
    private EditText width,height;
    private static int color = Color.parseColor("#2dcb70");;
    private static int widthValue = 1000;
    private static int heightValue = 1000;
    private Bitmap createdBitmap = null;
    private Button useImage;
    private Boolean isImageCreating = false;
    private InterstitialAd mInterstitialAd;


    @Override
    public void onBackPressed()
    {
        if(mInterstitialAd.isLoaded())
        {
            mInterstitialAd.show();
        }
        else
        {
            super.onBackPressed();
            overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
        }

    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        SharedPreferences pref = newBase.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String localeString = pref.getString(LANGUAGE_KEY,LANGUAGE_SINHALA);
        Locale myLocale = new Locale(localeString);
        Locale.setDefault(myLocale);
        Configuration config = newBase.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(myLocale);
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                Context newContext = newBase.createConfigurationContext(config);
                super.attachBaseContext(newContext);
                return;
            }
        } else {
            config.locale = myLocale;
        }
        super.attachBaseContext(newBase);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_abackground);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3538783908730049/5147080745");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        create = (Button) findViewById(R.id.create);
        width = (EditText) findViewById(R.id.width);
        height = (EditText) findViewById(R.id.height);
        userCreatedImage = (ImageView) findViewById(R.id.userCreatedImage);
        ImageView pickColor = (ImageView) findViewById(R.id.pickColor);
        useImage = (Button) findViewById(R.id.useImage);



        useImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                useImage.setEnabled(false);
                if(createdBitmap != null)
                {
                    if(mInterstitialAd.isLoaded())
                    {
                        mInterstitialAd.show();
                        useImage.setEnabled(true);
                    }
                    else
                    {

                        ImageList.getInstance().clearImageList();
                        ImageList.getInstance().addBitmap(createdBitmap,false);


                        startActivity(new Intent(getApplicationContext(), EditorActivity.class));
                        finish();
                        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
                    }


                }
            }
        });

        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                chooseColor();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isImageCreating)
                {
                    return;
                }
                if(width.getText().toString().equals(""))
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.enter_valid_width));
                    return;
                }
                if(height.getText().toString().equals(""))
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.enter_valid_height));
                    return;
                }
                if(color == -99)
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.pick_color_text));
                    return;
                }
                if(Integer.parseInt(width.getText().toString()) > 1000)
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.width_exceed_text));
                    return;
                }
                if(Integer.parseInt(height.getText().toString()) > 1000)
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.height_exceed_text));
                    return;
                }
                if(Integer.parseInt(width.getText().toString()) < 400)
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.width_is_low_text));
                    return;
                }
                if(Integer.parseInt(height.getText().toString()) < 400)
                {
                    Methods.showCustomToast(CreateABackgroundActivity.this,getResources().getString(R.string.height_is_low_text2));
                    return;
                }


                String hex = "#" + Integer.toHexString(color);
                widthValue = Integer.parseInt(width.getText().toString());
                heightValue = Integer.parseInt(height.getText().toString());
                createdBitmap = createImage(widthValue,heightValue,hex);
                userCreatedImage.setImageBitmap(createdBitmap);


            }
        });


        width.setText(String.valueOf(widthValue));
        height.setText(String.valueOf(heightValue));
        final String hex = "#" + Integer.toHexString(color);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run()
            {
                createdBitmap = createImage(widthValue,heightValue,hex);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        userCreatedImage.setImageBitmap(createdBitmap);
                    }
                });

            }
        });


        changeTypeFace();


    }
    public  Bitmap createImage(int width, int height, String color)
    {
        isImageCreating = true;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(color));
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);

        isImageCreating = false;
        return bitmap;
    }
    public void chooseColor()
    {

        String hex = "#" + Integer.toHexString(color);


        ColorPickerDialogBuilder.with(CreateABackgroundActivity.this)
                .setTitle("Choose Color")
                .initialColor(Color.parseColor(hex))
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(20)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int i)
                    {
                        color = i;
                    }
                })
                .setPositiveButton("Pick", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] integers)
                    {
                        color = i;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                }).build().show();
    }
    private void changeTypeFace()
    {
        Typeface typeface = Methods.getDefaultTypeFace(getApplicationContext());
        create.setTypeface(typeface);
        useImage.setTypeface(typeface);
    }

}
