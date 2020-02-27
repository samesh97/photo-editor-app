package com.sba.sinhalaphotoeditor.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.glidebitmappool.GlideBitmapPool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sba.sinhalaphotoeditor.BuildConfig;
import com.sba.sinhalaphotoeditor.MostUsedMethods.Methods;
import com.sba.sinhalaphotoeditor.R;
import com.sba.sinhalaphotoeditor.firebase.AppData;
import com.sba.sinhalaphotoeditor.firebase.SihalaUser;


import java.util.ArrayList;
import java.util.Locale;


import static com.sba.sinhalaphotoeditor.Config.Constants.ACTIVITY_EXTRA_KEY;
import static com.sba.sinhalaphotoeditor.Config.Constants.APP_MARKET_LINK;
import static com.sba.sinhalaphotoeditor.Config.Constants.FIREBASE_APP_INFO_REFERENCE;
import static com.sba.sinhalaphotoeditor.Config.Constants.FIREBASE_PAYLOAD_MESSAGE_TEXT;
import static com.sba.sinhalaphotoeditor.Config.Constants.FIREBASE_PAYLOAD_TITLE_TEXT;
import static com.sba.sinhalaphotoeditor.Config.Constants.IS_WALKTHROUGH_NEEDED_KEY;
import static com.sba.sinhalaphotoeditor.Config.Constants.LANGUAGE_ENGLISH;
import static com.sba.sinhalaphotoeditor.Config.Constants.LANGUAGE_KEY;
import static com.sba.sinhalaphotoeditor.Config.Constants.LANGUAGE_POSITION_KEY;
import static com.sba.sinhalaphotoeditor.Config.Constants.LANGUAGE_SINHALA;
import static com.sba.sinhalaphotoeditor.Config.Constants.LANGUAGE_TAMIL;
import static com.sba.sinhalaphotoeditor.Config.Constants.SHARED_PREF_NAME;
import static com.sba.sinhalaphotoeditor.activities.MyCustomGallery.IMAGE_PICK_RESULT_CODE;
import static com.sba.sinhalaphotoeditor.activities.MyCustomGallery.selectedBitmap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int PICK_IMAGE_REQUEST = 234;
    public static Bitmap bitmap;
    public static int imagePosition = 0;

    private boolean isExit = false;
    private static boolean isFirstTime = true;


    public static ArrayList<Bitmap> images = new ArrayList<>();
    private ProgressDialog dialog;
    private Spinner languagePicker;
    private Methods methods;


    private ImageView animation;
    private AnimatedVectorDrawable animations;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private ImageView createBitmap;
    private ConstraintLayout createImageFromLibrary;
    private ConstraintLayout pickImageFromDb;
    private ConstraintLayout pickImageFromGallery;
    private ImageView imageView;
    private ImageView roundImage;
    private TextView selectPictureText;
    private TextView verionName;
    private ImageView topGreenPannel;
    private TextView useOne;
    private TextView createOne;
    private TextView fromGalery;
    private ImageView usePreviousBitmap;







    @Override
    public void onBackPressed()
    {
        if(isExit)
        {
            isFirstTime = true;
            super.onBackPressed();
        }
        else
        {
            isUserWantToExitTheApp();
        }

    }

    @Override
    protected void onStart()
    {

        checkWalkThroughNeededOrNot();
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == IMAGE_PICK_RESULT_CODE && selectedBitmap != null)
        {
            new startActivity().execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initProfilePicture();
        methods = new Methods(getApplicationContext());

        setTextViewFontAndSize();
        setAppLanguage();


    }

    private void initProfilePicture()
    {
        CircleImageView profilePic = findViewById(R.id.profilePic);
        SihalaUser user = new SihalaUser();
        SihalaUser currentUser = user.getUser(MainActivity.this);

        if(currentUser != null)
        {
            if(currentUser.getUserProfilePic() != null)
            {
                Glide.with(MainActivity.this)
                        .load(currentUser.getUserProfilePic())
                        .error(R.drawable.sampleprofilepic)
                        .placeholder(R.drawable.sampleprofilepic)
                        .into(profilePic);
            }
        }
    }

    private void initViews()
    {
        createBitmap = (ImageView) findViewById(R.id.createBitmap);
        createImageFromLibrary = (ConstraintLayout) findViewById(R.id.createImageFromLibrary);
        pickImageFromDb = (ConstraintLayout) findViewById(R.id.pickImageFromDb);
        pickImageFromGallery = (ConstraintLayout) findViewById(R.id.pickImageFromGallery);
        imageView = (ImageView) findViewById(R.id.imageView);
        roundImage = (ImageView) findViewById(R.id.roundImage);
        selectPictureText = findViewById(R.id.selectPictureText);
        languagePicker = findViewById(R.id.languagePicker);
        verionName = findViewById(R.id.verionName);
        topGreenPannel = findViewById(R.id.topGreenPannel);
        createOne = (TextView) findViewById(R.id.createOne);
        useOne = (TextView) findViewById(R.id.useOne);
        fromGalery = (TextView) findViewById(R.id.fromGalery);
        usePreviousBitmap = (ImageView) findViewById(R.id.usePreviousBitmap);



        dialog = new ProgressDialog(MainActivity.this);


        Glide.with(getApplicationContext()).load(R.drawable.samplewalpaper).into(topGreenPannel);
        verionName.setText("Version " + BuildConfig.VERSION_NAME);
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#018577")));


        Runtime rt = Runtime.getRuntime();
        int maxMemory = (int)rt.freeMemory();
        GlideBitmapPool.initialize(maxMemory);
        GlideBitmapPool.clearMemory();


        //pickImageFromGallery.animate().alpha(0.0f).setDuration(0).translationY(2000);


        Animation top = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottomtotop);
        Animation bottom = AnimationUtils.loadAnimation(MainActivity.this, R.anim.toptobottom);
        pickImageFromGallery.startAnimation(top);
        createImageFromLibrary.startAnimation(top);
        pickImageFromDb.startAnimation(top);
        roundImage.startAnimation(bottom);
        //languagePicker.startAnimation(bottom);
        selectPictureText.startAnimation(bottom);








        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        imageView.startAnimation(pulse);


        useOne.setOnClickListener(this);
        pickImageFromDb.setOnClickListener(this);
        createImageFromLibrary.setOnClickListener(this);
        createOne.setOnClickListener(this);
        createBitmap.setOnClickListener(this);
        usePreviousBitmap.setOnClickListener(this);


        createBitmap.startAnimation(pulse);
        usePreviousBitmap.startAnimation(pulse);



        configLanguagePicker();






        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child(FIREBASE_APP_INFO_REFERENCE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                checkForTheCurrentVersion();
            }
        },1500);




        configPushNotification();




    }

    private void configPushNotification()
    {
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(FIREBASE_PAYLOAD_TITLE_TEXT) && intent.hasExtra(FIREBASE_PAYLOAD_MESSAGE_TEXT))
        {

            if(intent.getExtras() != null)
            {

                String message = intent.getStringExtra(FIREBASE_PAYLOAD_MESSAGE_TEXT);
                String title = intent.getStringExtra(FIREBASE_PAYLOAD_TITLE_TEXT);

                if(message != null && title != null && !message.equals("") && !title.equals("") && !message.trim().equals("") && !title.trim().equals(""))
                {

                    Intent intent1 = new Intent(MainActivity.this,NotificationView.class);
                    intent1.putExtra(FIREBASE_PAYLOAD_TITLE_TEXT,title);
                    intent1.putExtra(FIREBASE_PAYLOAD_MESSAGE_TEXT,message);
                    startActivity(intent1);

                }
            }

        }
    }

    private void configLanguagePicker()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        final int pos = pref.getInt(LANGUAGE_POSITION_KEY,0);
        languagePicker.setSelection(pos);




        languagePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(pos != languagePicker.getSelectedItemPosition())
                {
                    switch (position)
                    {
                        case 1 :
                            setLocale(LANGUAGE_SINHALA,position);
                            break;

                        case  2 :
                            setLocale(LANGUAGE_ENGLISH,position);
                            break;

                        case 3:
                            setLocale(LANGUAGE_TAMIL,position);
                            break;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        boolean isReadGranted = false;
        boolean isWriteGranted = false;
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    isReadGranted = true;
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                else
                {
                    isReadGranted = false;
                }
                break;

            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    isWriteGranted = true;

                }
                else
                {
                    isWriteGranted = false;
                }
                break;
        }
    }

    public void showFileChooser(View view)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

            if(!isPermissonGranted())
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            if(!isPermissonGranted2())
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            }
            if(isPermissonGranted2() && isPermissonGranted())
            {
                showFileChooser();
            }

        }
        else
        {
            showFileChooser();
        }


    }
    private void showFileChooser()
    {
        /*
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/

        Intent intent = new Intent(MainActivity.this, MyCustomGallery.class);
        intent.putExtra(ACTIVITY_EXTRA_KEY,"MainActivity");
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);


    }
    public boolean isPermissonGranted()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        return false;

    }
    public boolean isPermissonGranted2()
    {

        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        return false;

    }
    public void isUserWantToExitTheApp()
    {

        final Dialog dialog = new Dialog(this);
        if(dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }




        View view = getLayoutInflater().inflate(R.layout.exit_dialog_layout,null);

        dialog.setContentView(view);

        WindowManager wm = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

        TextView message = view.findViewById(R.id.textView8);

        message.setText(getResources().getText(R.string.exit_app_confirmation_text));

        TextView title = view.findViewById(R.id.textView7);


        title.setText(getResources().getText(R.string.app_exit_text));

        Button yes = view.findViewById(R.id.yesButton);
        Button no = view.findViewById(R.id.noButton);

        yes.setText(getResources().getText(R.string.exit_app_yes_text));
        no.setText(getResources().getText(R.string.exit_app_no_text));
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                isExit = true;
                onBackPressed();
            }
        });


        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                isExit = false;
            }
        });
        dialog.show();


    }
    public static void deleteUndoRedoImages()
    {


        if(images.size() > 6)
        {
            int size = images.size() - 6;
            MainActivity.imagePosition = MainActivity.imagePosition - size;
            for(int i = 1; i <= size; i++)
            {
                if(i == imagePosition)
                {
                    size++;
                }
                else
                {

                    images.remove(i);
                    GlideBitmapPool.clearMemory();
                }
            }


        }
    }
    public void setLocale(String lang,int position)
    {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);





        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(LANGUAGE_POSITION_KEY,position);
        editor.putString(LANGUAGE_KEY,lang);

        editor.apply();




        Intent refresh = new Intent(this, MainActivity.class);
        //refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(refresh);
        finish();
    }
    public void setLocaleOnStart(String lang)
    {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);


        isFirstTime = false;

        Intent refresh = new Intent(this, MainActivity.class);
        //refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(refresh);
        finish();

    }
    public void setTextViewFontAndSize()
    {

        TextView selectPictureText = findViewById(R.id.selectPictureText);
        TextView fromGalery = findViewById(R.id.fromGalery);
        TextView createOne = findViewById(R.id.createOne);
        TextView useOne = findViewById(R.id.useOne);

        Typeface typeface;

        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        int pos = pref.getInt(LANGUAGE_POSITION_KEY,-99);
        if(pos != 99)
        {
            switch (pos)
            {
                case 1 :


                    typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.gemunulibresemibold);
                    selectPictureText.setTypeface(typeface);
                    fromGalery.setTypeface(typeface);
                    createOne.setTypeface(typeface);
                    useOne.setTypeface(typeface);
                    break;
                case 2 :

                    typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.englishfont);
                    selectPictureText.setTypeface(typeface);


                    fromGalery.setTypeface(typeface);


                    createOne.setTypeface(typeface);




                    useOne.setTypeface(typeface);


                    break;
                case 3:


                    typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.tamilfont);


                    selectPictureText.setTypeface(typeface);
                    selectPictureText.setTextSize(20);

                    fromGalery.setTypeface(typeface);
                    fromGalery.setTextSize(14);

                    createOne.setTypeface(typeface);
                    createOne.setTextSize(14);

                    useOne.setTypeface(typeface);
                    useOne.setTextSize(14);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v == useOne || v == usePreviousBitmap)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {

                if(!isPermissonGranted())
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                if(!isPermissonGranted2())
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                if(isPermissonGranted() && isPermissonGranted2())
                {
                    startActivity(new Intent(MainActivity.this, UsePreviouslyEditedImageActivity.class));
                }
            }
            else
            {
                startActivity(new Intent(MainActivity.this,UsePreviouslyEditedImageActivity.class));
            }
        }
        else if(v == pickImageFromDb)
        {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {

                        if(!isPermissonGranted())
                        {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                        if(!isPermissonGranted2())
                        {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                        }
                        if(isPermissonGranted() && isPermissonGranted2())
                        {
                            startActivity(new Intent(MainActivity.this,UsePreviouslyEditedImageActivity.class));
                        }
                    }
                    else
                    {
                        startActivity(new Intent(MainActivity.this,UsePreviouslyEditedImageActivity.class));
                    }

        }
        else if(v == createImageFromLibrary || v == createOne || v == createBitmap)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {

                if(!isPermissonGranted())
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                if(!isPermissonGranted2())
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                if(isPermissonGranted() && isPermissonGranted2())
                {
                    startActivity(new Intent(getApplicationContext(), CreateABackgroundActivity.class));
                }
            }
            else
            {
                startActivity(new Intent(getApplicationContext(),CreateABackgroundActivity.class));
            }

        }

    }

    public class startActivity extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            configDialog();

        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            checkAnimationOverTime();

        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            images.clear();
            imagePosition = 0;

            bitmap = selectedBitmap.copy(selectedBitmap.getConfig(), true);
            selectedBitmap = null;

            bitmap = methods.getResizedBitmap(bitmap, 1500);
            return null;

        }


    }
    public void checkAnimationOverTime()
    {
        if(animations.isRunning())
        {
            animations.invalidateSelf();
            animations.stop();
        }

        Drawable drawable = getResources().getDrawable(R.drawable.finish_loading);
        animation.setImageDrawable(drawable);

        Drawable d = animation.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {

            animations = (AnimatedVectorDrawable) d;
            animations.start();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    images.add(0, bitmap);
                    startActivity(new Intent(getApplicationContext(), EditorActivity.class));
                    dialog.dismiss();
                }
            },2500);
        }


    }
    public void configDialog()
    {
        dialog.show();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.6f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
        dialog.getWindow().setAttributes(lp);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);
        dialog.setContentView(view);

        animation = view.findViewById(R.id.animation);

        Drawable d = animation.getDrawable();
        if (d instanceof AnimatedVectorDrawable) {

            animations = (AnimatedVectorDrawable) d;
            animations.start();
        }
    }
    public void checkForTheCurrentVersion()
    {
        final int currentVersion = BuildConfig.VERSION_CODE;
        final String currentVersionName = BuildConfig.VERSION_NAME;
        final boolean appType = BuildConfig.DEBUG;

        if (database == null)
        {
            database = FirebaseDatabase.getInstance();
        }
        if (reference == null)
        {
            reference = database.getReference().child(FIREBASE_APP_INFO_REFERENCE);
        }



        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    AppData data = dataSnapshot.getValue(AppData.class);
                    if (data != null) {
                        if (data.getVersionCode() > currentVersion) {
                            showUpgradeAppPopup(data);
                        } else if (data.getVersionCode() < currentVersion) {
                            if (appType) {
                                //debug app version
                                return;
                            }
                            AppData newData = new AppData();
                            newData.setVersionCode(currentVersion);
                            newData.setVersionName(currentVersionName);

                            uploadAppDetails(newData);

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showUpgradeAppPopup(AppData data)
    {
        if (data != null)
        {
            final Dialog popdialog = new Dialog(MainActivity.this);
            View view = getLayoutInflater().inflate(R.layout.update_app_dialog, null);
            popdialog.setContentView(view);
            if(popdialog.getWindow() != null)
            {
                popdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }


            popdialog.setCanceledOnTouchOutside(false);

            popdialog.getWindow().setGravity(Gravity.BOTTOM);

            WindowManager.LayoutParams lp = popdialog.getWindow().getAttributes();
            lp.dimAmount=0.8f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
            popdialog.getWindow().setAttributes(lp);

            popdialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, 500);

            TextView versionName = view.findViewById(R.id.versionName);
            Button updateAppButton = view.findViewById(R.id.updateAppButton);
            ImageView close = view.findViewById(R.id.close);

            if (data.getVersionName() != null) {
                versionName.setText("V" + data.getVersionName());
            }
            updateAppButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(APP_MARKET_LINK));
                    startActivity(intent);

                    popdialog.dismiss();
                }
            });

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    popdialog.dismiss();
                }
            });

            if (!MainActivity.this.isFinishing())
            {
                popdialog.show();
            }

        }


    }

    public void uploadAppDetails(final AppData data) {
        if (data == null) {
            return;
        }
        if (reference == null) {
            reference = database.getReference().child(FIREBASE_APP_INFO_REFERENCE);
        }
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                reference.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            uploadAppDetails(data);
                        }
                    }
                });
            }
        });

    }
    private void checkWalkThroughNeededOrNot()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        //SharedPreferences.Editor editor = pref.edit();
        boolean isWalkThroughNeeded = pref.getBoolean(IS_WALKTHROUGH_NEEDED_KEY, false);
        if (!isWalkThroughNeeded)
        {
            startActivity(new Intent(MainActivity.this, WelcomeScreen.class));
            finish();
        }
        else
        {
            SihalaUser user = new SihalaUser();
            SihalaUser cUser = user.getUser(MainActivity.this);
            if(cUser == null)
            {
                startActivity(new Intent(MainActivity.this, RegisterScreen.class));
                finish();
            }
        }
    }
    private void setAppLanguage()
    {
        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String code = pref2.getString(LANGUAGE_KEY,null);
        if(code != null)
        {
            if(isFirstTime)
            {
                setLocaleOnStart(code);
            }

        }
    }

}







