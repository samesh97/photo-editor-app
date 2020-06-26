package com.sba.sinhalaphotoeditor.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;



import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.sba.sinhalaphotoeditor.sdk.Methods;
import com.sba.sinhalaphotoeditor.R;
import com.sba.sinhalaphotoeditor.singleton.ImageList;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import render.animations.Render;
import render.animations.*;

import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_SINHALA;
import static com.sba.sinhalaphotoeditor.config.Constants.NUMBER_OF_IMAGES_WAS_ADDED;
import static com.sba.sinhalaphotoeditor.activities.MyCustomGallery.IMAGE_PICK_RESULT_CODE;
import static com.sba.sinhalaphotoeditor.activities.MyCustomGallery.selectedBitmap;
import static com.sba.sinhalaphotoeditor.config.Constants.SHARED_PREF_NAME;

public class EditorActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int DRAW_ON_BITMAP_REQUEST_CODE = 500;
    public static final int NORMAL_CROP_IMAGE_REQUEST_CODE = 700;
    public static final int PICKED_IMAGE_CROP_IMAGE_REQUEST_CODE = 701;
    private ImageView userSelectedImage;
    private ImageView addText,addImage,addSticker,addCrop,addBlur;
    private static final int PICK_IMAGE_REQUEST = 234;

    private ImageView addEffect;


    private Dialog dia;

    private String CropType = null;


    public static Bitmap selectedSticker = null;

    private ArrayList<Bitmap> stickerList1 = new ArrayList<>();
    private ArrayList<Bitmap> stickerList2 = new ArrayList<>();
    private ArrayList<Bitmap> stickerList3 = new ArrayList<>();
    private ArrayList<Bitmap> stickerList4 = new ArrayList<>();
    private ArrayList<Bitmap> stickerList5 = new ArrayList<>();

    private Render render;
    private Methods methods;
    private InterstitialAd mInterstitialAd;



    @Override
    public void onBackPressed()
    {
        if(ImageList.getInstance().getImageListSize() > 1)
        {
            showConfirmationDialogToExit();
        }
        else
        {
            super.onBackPressed();
            overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_activity_menu, menu);
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.undoImage)
        {
            if(ImageList.getInstance().getCurrentPosition() >= 1)
            {
                try
                {

                    ImageList.getInstance().substractImagePosition(1);
                    methods.setImageViewScaleType(userSelectedImage);
                    Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);

                }
                catch (Exception e)
                {

                }
            }
            else
            {

                render.setAnimation(Attention.Shake(userSelectedImage));
                render.start();


                Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.you_reach_end_text));
            }
        }
        if(item.getItemId() == R.id.saveImage)
        {
            if(mInterstitialAd.isLoaded())
            {
                mInterstitialAd.show();
            }
            else
            {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

                if(ImageList.getInstance().getImageListSize() > 0)
                {

                    startActivity(new Intent(getApplicationContext(), ImageSavingActivity.class));
                    overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);

                }
                else
                {
                    Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.something_went_wrong_text));
                }
            }

        }
        if(item.getItemId() == R.id.redoImage)
        {
            if(ImageList.getInstance().getCurrentPosition() < (ImageList.getInstance().getImageListSize() - 1))
            {
                try
                {

                    ImageList.getInstance().increaseImagePosition(1);
                    methods.setImageViewScaleType(userSelectedImage);
                    Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);

                }
                catch (Exception e)
                {

                }



            }
            else
            {
                render.setAnimation(Attention.Shake(userSelectedImage));
                render.start();


                Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.you_reach_end_text_2));
            }
        }

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == TextOnImageActivity.TEXT_ON_IMAGE_REQUEST_CODE)
        {
            if(resultCode == TextOnImageActivity.TEXT_ON_IMAGE_RESULT_OK_CODE)
            {
                methods.setImageViewScaleType(userSelectedImage);
                Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);
            }

        }
        if(requestCode == PhotoOnPhotoActivity.IMAGE_ON_IMAGE_REQUEST_CODE)
        {
            if(resultCode == PhotoOnPhotoActivity.IMAGE_ON_IMAGE_RESULT_OK_CODE)
            {
                methods.setImageViewScaleType(userSelectedImage);
                Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);
            }
            if(requestCode == PhotoOnPhotoActivity.IMAGE_ON_IMAGE_RESULT_FAILED_CODE)
            {

            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == IMAGE_PICK_RESULT_CODE && selectedBitmap != null)
        {
                Intent intent = new Intent(getApplicationContext(),CropActivity.class);
                startActivityForResult(intent,PICKED_IMAGE_CROP_IMAGE_REQUEST_CODE);
        }
        if(requestCode == AddStickerOnImage.STICKER_ON_IMAGE_REQUEST_CODE)
        {
            if(resultCode == AddStickerOnImage.STICKER_ON_IMAGE_RESULT_OK_CODE)
            {
                methods.setImageViewScaleType(userSelectedImage);
                Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);

            }
        }
        if(requestCode == NORMAL_CROP_IMAGE_REQUEST_CODE && resultCode == RESULT_OK)
        {
            ImageList.getInstance().addBitmap(CropActivity.croppedBitmap,true);
            methods.setImageViewScaleType(userSelectedImage);
            Glide.with(getApplicationContext()).load(CropActivity.croppedBitmap).into(userSelectedImage);
            ImageList.getInstance().deleteUndoRedoImages();

        }
        if(requestCode == PICKED_IMAGE_CROP_IMAGE_REQUEST_CODE && resultCode == RESULT_OK)
        {
            selectedBitmap = CropActivity.croppedBitmap;
            addImageOnImage();
        }
        if(requestCode == 10  && resultCode == 11)
        {
            methods.setImageViewScaleType(userSelectedImage);
            Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);
        }
        if(requestCode == 20  && resultCode == 21)
        {
            methods.setImageViewScaleType(userSelectedImage);
            Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);
        }
        if(requestCode == DRAW_ON_BITMAP_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            methods.setImageViewScaleType(userSelectedImage);
            Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);




        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3538783908730049/5147080745");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());




        methods = new Methods(getApplicationContext());


       MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        render = new Render(EditorActivity.this);



        dia = new Dialog(this, R.style.DialogSlideAnim);

        userSelectedImage = (ImageView) findViewById(R.id.userSelectedImage);
        addText = (ImageView) findViewById(R.id.addText);
        addImage = (ImageView) findViewById(R.id.addImage);
        addSticker =(ImageView) findViewById(R.id.addSticker);
        addCrop = (ImageView) findViewById(R.id.addCrop);
        addEffect = (ImageView) findViewById(R.id.addEffect);
        addBlur = (ImageView) findViewById(R.id.addBlur);
       // stickerLayout = findViewById(R.id.stickerLayout);
        ImageView drawImage = (ImageView) findViewById(R.id.drawImage);


        addBlur.setOnClickListener(this);
        addEffect.setOnClickListener(this);



        try
        {
            methods.setImageViewScaleType(userSelectedImage);
            Glide.with(getApplicationContext()).load(ImageList.getInstance().getCurrentBitmap()).into(userSelectedImage);

        }
        catch (Exception e)
        {

        }



        drawImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), DrawOnBitmapActivity.class);
                startActivityForResult(intent,DRAW_ON_BITMAP_REQUEST_CODE);
                overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
            }
        });



        addText.setOnClickListener(this);
        addImage.setOnClickListener(this);
        addSticker.setOnClickListener(this);
        addCrop.setOnClickListener(this);

    }

    private void setStickers(ListViewAdapter adapter)
    {

        removeStickers();
        Bitmap b;
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji1);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji2);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji3);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji4);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji5);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji6);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji7);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji8);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji9);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji10);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();

        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji11);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji12);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji13);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji14);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji15);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();

        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji16);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji17);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji18);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji19);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji20);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();

        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji21);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji22);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji23);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji24);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji25);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();

        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji26);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji27);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji28);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji29);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji30);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();

        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji31);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji32);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji33);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji34);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji35);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();

        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji36);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji37);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji38);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji39);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji40);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();


        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji41);
        b = methods.getResizedBitmap(b,80);
        stickerList1.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji42);
        b = methods.getResizedBitmap(b,80);
        stickerList2.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji43);
        b = methods.getResizedBitmap(b,80);
        stickerList3.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji44);
        b = methods.getResizedBitmap(b,80);
        stickerList4.add(b);
        b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji45);
        b = methods.getResizedBitmap(b,80);
        stickerList5.add(b);

        adapter.notifyDataSetChanged();
        b = null;

    }
    public void removeStickers()
    {
        stickerList1.clear();
        stickerList2.clear();
        stickerList3.clear();
        stickerList4.clear();
        stickerList5.clear();
    }

    private void addTextOnImage(String text)
    {
        Intent intent = new Intent(EditorActivity.this, TextOnImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(TextOnImageActivity.TEXT_TO_WRITE,text);
        intent.putExtras(bundle);
        startActivityForResult(intent, TextOnImageActivity.TEXT_ON_IMAGE_REQUEST_CODE);
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);

    }
    private void addImageOnImage()
    {
        Intent intent = new Intent(EditorActivity.this,PhotoOnPhotoActivity.class);
        startActivityForResult(intent, PhotoOnPhotoActivity.IMAGE_ON_IMAGE_REQUEST_CODE);
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
    }
    public void showPopup()
    {
        final Dialog dialog = new Dialog(this);
        if(dialog.getWindow() != null)
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = getLayoutInflater().inflate(R.layout.activity_popup_screen,null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        final EditText editText = (EditText) view.findViewById(R.id.updateText);
        Button button1 = (Button) view.findViewById(R.id.update);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(editText.getText().toString().equals("") || editText.getText().toString().trim().equals(""))
                {
                    Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.enter_text));
                    return;
                }
                dialog.dismiss();
                addTextOnImage(editText.getText().toString());
            }
        });

        dialog.show();


    }
    private void showFileChooser()
    {

        Intent intent = new Intent(EditorActivity.this, MyCustomGallery.class);
        intent.putExtra("Activity","EditorActivity");
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
    }
    private void addStickerOnImage()
    {
        Intent intent = new Intent(EditorActivity.this,AddStickerOnImage.class);
        startActivityForResult(intent, AddStickerOnImage.STICKER_ON_IMAGE_REQUEST_CODE);
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
    }
    private void showStickerPopup()
    {
        dia.setContentView(R.layout.activity_sticker_popup_screen);
        dia.setCancelable(true);
        if(dia.getWindow() != null)
        {
            dia.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dia.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (Methods.getDeviceHeightInPX(getApplicationContext()) / 2));
            Window window = dia.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            window.setAttributes(wlp);
        }


        ListView list_alert = (ListView) dia.findViewById(R.id.stickeList);
        ListViewAdapter adapter = new ListViewAdapter(stickerList1,stickerList2,stickerList3,stickerList4,stickerList5);
        list_alert.setAdapter(adapter);
        setStickers(adapter);
        dia.show();

    }

    @Override
    public void onClick(View v)
    {

        if(v == addBlur)
        {

                    if(ImageList.getInstance().getCurrentBitmap() != null && ImageList.getInstance().getImageListSize() > 0)
                    {
                        render.setAnimation(Attention.Bounce(addBlur));
                        render.start();
                        startActivityForResult(new Intent(getApplicationContext(), AdjustImage.class),10);
                        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
                    }
                    else
                    {
                        Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.no_image_selected_text));

                    }

        }
        else if(v == addEffect)
        {


                    if(ImageList.getInstance().getCurrentBitmap() != null && ImageList.getInstance().getImageListSize() > 0)
                    {
                        render.setAnimation(Attention.Bounce(addEffect));
                        render.start();
                        startActivityForResult(new Intent(getApplicationContext(), AddEffects.class),20);
                        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
                    }
                    else
                    {
                        Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.no_image_selected_text));
                    }



        }
        else if(v == addText)
        {

                    if(ImageList.getInstance().getCurrentBitmap() != null && ImageList.getInstance().getImageListSize() > 0)
                    {
                        render.setAnimation(Attention.Bounce(addText));
                        render.start();
                        showPopup();
                    }
                    else
                    {
                        Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.no_image_selected_text));
                    }

        }
        else if(v == addImage)
        {

                    if(ImageList.getInstance().getCurrentBitmap() != null && ImageList.getInstance().getImageListSize() > 0)
                    {
                        CropType = "PhotoOnPhotoCrop";
                        render.setAnimation(Attention.Bounce(addImage));
                        render.start();
                        showFileChooser();
                    }
                    else
                    {
                        Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.no_image_selected_text));
                    }

        }
        else if(v == addSticker)
        {

            render.setAnimation(Attention.Bounce(addSticker));
            render.start();
            showStickerPopup();


        }
        else if(v == addCrop)
        {

            if(ImageList.getInstance().getCurrentBitmap() != null && ImageList.getInstance().getImageListSize() > 0)
            {
                selectedBitmap = ImageList.getInstance().getCurrentBitmap();
                Intent intent = new Intent(getApplicationContext(),CropActivity.class);
                startActivityForResult(intent,NORMAL_CROP_IMAGE_REQUEST_CODE);
            }
            else
            {
                    Methods.showCustomToast(EditorActivity.this,getResources().getString(R.string.no_image_selected_text));
            }
        }
    }

    public class ListViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
    {
        ArrayList<Bitmap> stic1;
        ArrayList<Bitmap> stic2;
        ArrayList<Bitmap> stic3;
        ArrayList<Bitmap> stic4;
        ArrayList<Bitmap> stic5;

        ListViewAdapter(ArrayList<Bitmap> stic1,ArrayList<Bitmap> stic2,ArrayList<Bitmap> stic3,ArrayList<Bitmap> stic4,ArrayList<Bitmap> stic5)
        {
            this.stic1 = stic1;
            this.stic2 = stic2;
            this.stic3 = stic3;
            this.stic4 = stic4;
            this.stic5 = stic5;
        }
        @Override
        public int getCount()
        {
            return stic5.size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View view = getLayoutInflater().inflate(R.layout.stickerrow,null);
            ImageView OneSticker = (ImageView) view.findViewById(R.id.OneSticker);
            ImageView TwoSticker = (ImageView) view.findViewById(R.id.TwoSticker);
            ImageView ThreeSticker = (ImageView) view.findViewById(R.id.ThreeSticker);
            ImageView FourSticker = (ImageView) view.findViewById(R.id.FourSticker);
            ImageView FiveSticker = (ImageView) view.findViewById(R.id.FiveSticker);


            OneSticker.setImageBitmap(stic1.get(position));
            TwoSticker.setImageBitmap(stic2.get(position));
            ThreeSticker.setImageBitmap(stic3.get(position));
            FourSticker.setImageBitmap(stic4.get(position));
            FiveSticker.setImageBitmap(stic5.get(position));


            Animation pulse = AnimationUtils.loadAnimation(EditorActivity.this, R.anim.pulse2);
            OneSticker.startAnimation(pulse);
            TwoSticker.startAnimation(pulse);
            ThreeSticker.startAnimation(pulse);
            FourSticker.startAnimation(pulse);
            FiveSticker.startAnimation(pulse);


            OneSticker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Bitmap x = getRealSticker((position * 5) + 1);
                    selectedSticker = x;
                    addStickerOnImage();
                    dia.dismiss();
                }
            });
            TwoSticker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Bitmap x = getRealSticker((position * 5) + 2);
                    selectedSticker = x;
                    addStickerOnImage();
                    dia.dismiss();
                }
            });
            ThreeSticker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Bitmap x = getRealSticker((position * 5) + 3);
                    selectedSticker = x;
                    addStickerOnImage();
                    dia.dismiss();
                }
            });
            FourSticker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Bitmap x = getRealSticker((position * 5) + 4);
                    selectedSticker = x;
                    addStickerOnImage();
                    dia.dismiss();
                }
            });
            FiveSticker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Bitmap x = getRealSticker((position * 5) + 5);
                    selectedSticker = x;
                    addStickerOnImage();
                    dia.dismiss();
                }
            });

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

        }
    }
    public Bitmap getRealSticker(int position)
    {
        Bitmap b;

        switch (position)
        {
            case 2 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji2);
                break;
            case 3 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji3);
                break;
            case 4 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji4);
                break;
            case 5 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji5);
                break;
            case 6 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji6);
                break;
            case 7 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji7);
                break;
            case 8 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji8);
                break;
            case 9 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji9);
                break;
            case 10 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji10);
                break;
            case 11 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji11);
                break;
            case 12 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji12);
                break;
            case 13 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji13);
                break;
            case 14 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji14);
                break;
            case 15 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji15);
                break;
            case 16 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji16);
                break;
            case 17 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji17);
                break;
            case 18 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji18);
                break;
            case 19 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji19);
                break;
            case 20 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji20);
                break;
            case 21 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji21);
                break;
            case 22 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji22);
                break;
            case 23 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji23);
                break;
            case 24 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji24);
                break;
            case 25 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji25);
                break;
            case 26 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji26);
                break;
            case 27 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji27);
                break;
            case 28 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji28);
                break;
            case 29 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji29);
                break;
            case 30 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji30);
                break;
            case 31 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji31);
                break;
            case 32 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji32);
                break;
            case 33 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji33);
                break;
            case 34 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji34);
                break;
            case 35 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji35);
                break;
            case 36 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji36);
                break;
            case 37 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji37);
                break;
            case 38 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji38);
                break;
            case 39 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji39);
                break;
            case 40 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji40);
                break;
            case 41 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji41);
                break;
            case 42 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji42);
                break;
            case 43 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji43);
                break;
            case 44 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji44);
                break;
            case 45 : b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji45);
                break;
            default:b = BitmapFactory.decodeResource(getResources(),R.drawable.emoji1);
                break;

        }

        return b;


    }
    private void showConfirmationDialogToExit()
    {
        final Dialog dialog = new Dialog(this);
        if(dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            float deviceWidth = Methods.getDeviceWidthInPX(getApplicationContext());
            int finalWidth = (int) (deviceWidth - (deviceWidth / 8));
            dialog.getWindow().setLayout(finalWidth,RelativeLayout.LayoutParams.WRAP_CONTENT);
        }


        View view = getLayoutInflater().inflate(R.layout.exit_dialog_layout,null);
        dialog.setContentView(view);


        ImageView imageView6 = view.findViewById(R.id.imageView6);
        imageView6.setImageDrawable(getDrawable(R.drawable.exit_editing_cartoon));
        imageView6.setScaleType(ImageView.ScaleType.CENTER);

        Typeface typeface = Methods.getDefaultTypeFace(getApplicationContext());


        TextView message = view.findViewById(R.id.textView8);
        message.setText(getResources().getString(R.string.are_you_sure_want_to_exit_text));
        message.setTypeface(typeface);

        TextView title = view.findViewById(R.id.textView7);
        title.setText(getResources().getString(R.string.warning_text));
        title.setTypeface(typeface);

        Button yes = view.findViewById(R.id.yesButton);
        Button no = view.findViewById(R.id.noButton);

        yes.setText(getResources().getString(R.string.yes_text));
        yes.setTypeface(typeface);

        no.setText(getResources().getString(R.string.no_text));
        no.setTypeface(typeface);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra(NUMBER_OF_IMAGES_WAS_ADDED,(ImageList.getInstance().getImageListSize() - 1));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}