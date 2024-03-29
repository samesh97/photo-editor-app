package com.sba.sinhalaphotoeditor.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sba.sinhalaphotoeditor.BuildConfig;
import com.sba.sinhalaphotoeditor.callbacks.OnAsyncTaskState;
import com.sba.sinhalaphotoeditor.sdk.Methods;
import com.sba.sinhalaphotoeditor.R;
import com.sba.sinhalaphotoeditor.database.DatabaseHelper;
import com.sba.sinhalaphotoeditor.adapters.RecentImageAdapter;
import com.sba.sinhalaphotoeditor.aynctask.AddImageToArrayListAsyncTask;
import com.sba.sinhalaphotoeditor.model.AppData;
import com.sba.sinhalaphotoeditor.model.SihalaUser;
import com.sba.sinhalaphotoeditor.singleton.ImageList;


import java.util.ArrayList;
import java.util.Locale;


import static com.sba.sinhalaphotoeditor.activities.MyGallery.selectedBitmap;
import static com.sba.sinhalaphotoeditor.config.Constants.ACTIVITY_EXTRA_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.APP_MARKET_LINK;
import static com.sba.sinhalaphotoeditor.config.Constants.FIREBASE_APP_INFO_REFERENCE;
import static com.sba.sinhalaphotoeditor.config.Constants.FIREBASE_PAYLOAD_MESSAGE_TEXT;
import static com.sba.sinhalaphotoeditor.config.Constants.FIREBASE_PAYLOAD_TITLE_TEXT;
import static com.sba.sinhalaphotoeditor.config.Constants.IS_WALKTHROUGH_NEEDED_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_POSITION_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_SINHALA;
import static com.sba.sinhalaphotoeditor.config.Constants.SHARED_PREF_NAME;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnAsyncTaskState {


    private static final int PICK_IMAGE_REQUEST = 234;
    public static Bitmap bitmap;
    private boolean isExit = false;
    private Dialog dialog;
    private Methods methods;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ConstraintLayout createImageFromLibrary;
    private ImageList.ImageListModel imageList = null;

    //previously edited images
    RecentImageAdapter adapter;
    RecyclerView recentImageRecyclerview;
    ArrayList<Bitmap> images = new ArrayList<>();
    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> dates = new ArrayList<>();
    DatabaseHelper helper = new DatabaseHelper(MainActivity.this);


    @Override
    public void onBackPressed()
    {
        if(isExit)
        {
            super.onBackPressed();
        }
        else
        {
            isUserWantToExitTheApp();
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
    protected void onStart()
    {
        isNeedToOpenAnotherActivity();
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && selectedBitmap != null)
        {
            showProgressDialog();

            AsyncTask.execute(new Runnable() {
                @Override
                public void run()
                {
                    imageList.clearImageList();
                    bitmap = selectedBitmap.copy(selectedBitmap.getConfig(), true);
                    selectedBitmap = null;
                    bitmap = methods.getResizedBitmap(bitmap, 1500);
                    AddImageToArrayListAsyncTask task = new AddImageToArrayListAsyncTask(bitmap,MainActivity.this);
                    task.execute();
                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //delete unnecessary recent images from database
        helper.deleteUnnessaryImages();

        imageList = ImageList.getInstance();
        initProfilePicture();
        methods = new Methods(getApplicationContext());


        recentImageRecyclerview = findViewById(R.id.ImageList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        recentImageRecyclerview.setLayoutManager(manager);
        adapter = new RecentImageAdapter(this,images,ids,dates);
        recentImageRecyclerview.setAdapter(adapter);


        createImageFromLibrary =  findViewById(R.id.createImageFromLibrary);
        createImageFromLibrary.setOnClickListener(this);
        ConstraintLayout pickImageFromGallery = findViewById(R.id.pickImageFromGallery);
        TextView versionName = findViewById(R.id.versionName);

        String version = BuildConfig.VERSION_NAME;
        versionName.setText("Version " + version);



        if(BuildConfig.DEBUG)
        {
            Methods.showCustomToast(getApplicationContext(),"This is not a released version");
        }


        TranslateAnimation animation2 = new TranslateAnimation(-1000,0,0, 0);
        animation2.setDuration(500);
        animation2.setFillAfter(true);
        pickImageFromGallery.startAnimation(animation2);
        createImageFromLibrary.setVisibility(View.GONE);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animat)
            {

                createImageFromLibrary.setVisibility(View.VISIBLE);
                TranslateAnimation animation = new TranslateAnimation(1000,0,0, 0);
                animation.setDuration(300);
                animation.setFillAfter(true);
                createImageFromLibrary.startAnimation(animation);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        getRecentImages();
        configPushNotification();
        checkForTheCurrentVersion();
        //configLanguagePicker();

        changeTypeFace();



    }
    public void getRecentImages()
    {

        ImageView empty_list_image = findViewById(R.id.empty_list_image);
        TextView recentTextView = findViewById(R.id.recent_tv);

        try
        {
            Cursor cursor = helper.GetAllImages();
            while(cursor.moveToNext())
            {
                ids.add(cursor.getInt(0));


                images.add(Methods.getImageFromInternalStorage(getApplicationContext(),cursor.getString(2)));
                dates.add(cursor.getString(2));

                adapter.notifyDataSetChanged();

                //free up memory
                Methods.freeUpMemory();

                empty_list_image.setVisibility(View.GONE);
                recentTextView.setVisibility(View.VISIBLE);


            }
            if(cursor.getCount() == 0)
            {
                //if there is no recent images available
                empty_list_image.setVisibility(View.VISIBLE);
                recentTextView.setVisibility(View.GONE);
            }


            cursor.close();
        }
        catch (Exception e)
        {
            Methods.showCustomToast(MainActivity.this,getResources().getString(R.string.we_will_fix_it_soon_text));
        }
    }
    private void initProfilePicture()
    {
        CircleImageView profilePic = findViewById(R.id.profilePic);
        TextView user_name = findViewById(R.id.user_name);
        Button login_button = findViewById(R.id.login_button);
        SihalaUser currentUser = SihalaUser.getUser(MainActivity.this);

        if(currentUser != null)
        {
            if(currentUser.getUserProfilePic() != null && currentUser.getLocalImagePath() != null)
            {
                Bitmap bitmap = Methods.getImageFromInternalStorage(getApplicationContext(),currentUser.getLocalImagePath());

                Glide.with(MainActivity.this)
                        .load(bitmap)
                        .error(R.drawable.sampleprofilepic)
                        .placeholder(R.drawable.sampleprofilepic)
                        .into(profilePic);

                profilePic.setBorderColor(Color.WHITE);
                profilePic.setBorderWidth(4);

                if(currentUser.getUserName() != null && !currentUser.getUserName().trim().equals(""))
                {
                    user_name.setText("Hello " + currentUser.getUserName());
                    login_button.setText("Log out");
                }
                else
                {
                    user_name.setText("Hello user");
                    login_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            startActivity(new Intent(MainActivity.this, RegisterScreen.class));
                            overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
                        }
                    });
                }

            }
        }
        else
        {
            user_name.setText("Hello user");
            login_button.setText("Login");
            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    startActivity(new Intent(MainActivity.this, RegisterScreen.class));
                    overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
                }
            });
        }

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
                    overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);

                }
            }

        }
        else
        {
            Log.d("intentResult","MainActivityNUll");
        }
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

        Intent intent = new Intent(MainActivity.this, MyGallery.class);
        intent.putExtra(ACTIVITY_EXTRA_KEY,"MainActivity");
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);

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

        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }
    public void isUserWantToExitTheApp()
    {

        final Dialog dialog = new Dialog(this);
        if(dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // dialog.getWindow().setDimAmount(0.4f);
            float deviceWidth = Methods.getDeviceWidthInPX(getApplicationContext());
            int finalWidth = (int) (deviceWidth - (deviceWidth / 8));
            dialog.getWindow().setLayout(finalWidth,RelativeLayout.LayoutParams.WRAP_CONTENT);
        }

        View view = getLayoutInflater().inflate(R.layout.exit_dialog_layout,null);
        dialog.setContentView(view);


        ImageView imageView6 = view.findViewById(R.id.imageView6);
        imageView6.setImageDrawable(getDrawable(R.drawable.exit_editing_cartoon));
        imageView6.setScaleType(ImageView.ScaleType.CENTER);



        TextView message = view.findViewById(R.id.textView8);

        message.setText(getResources().getText(R.string.exit_app_confirmation_text));

        TextView title = view.findViewById(R.id.textView7);


        title.setText(getResources().getText(R.string.app_exit_text));

        Button yes = view.findViewById(R.id.yesButton);
        Button no = view.findViewById(R.id.noButton);

        yes.setText(getResources().getText(R.string.exit_app_yes_text));
        no.setText(getResources().getText(R.string.exit_app_no_text));


        Typeface typeface = Methods.getDefaultTypeFace(getApplicationContext());

        message.setTypeface(typeface);
        title.setTypeface(typeface);
        yes.setTypeface(typeface);
        no.setTypeface(typeface);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
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
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
    }

    @Override
    public void onClick(View v)
    {
        if(v == createImageFromLibrary)
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
                    overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
                }
            }
            else
            {
                startActivity(new Intent(getApplicationContext(),CreateABackgroundActivity.class));
                overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
            }

        }

    }

    @Override
    public void startActivityForResult()
    {
        hideProgressDialog();
        startActivity(new Intent(getApplicationContext(), EditorActivity.class));
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
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
    private void isNeedToOpenAnotherActivity()
    {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        boolean isWalkThroughNeeded = pref.getBoolean(IS_WALKTHROUGH_NEEDED_KEY, false);
        if (!isWalkThroughNeeded)
        {
            startActivity(new Intent(MainActivity.this, WelcomeScreen.class));
            finish();
            overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
        }
    }
    public void startActivityForRecentEdits()
    {
        startActivity(new Intent(getApplicationContext(),EditorActivity.class));
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
    }
    public void showProgressDialog()
    {
        if(dialog == null)
        {
            dialog = new Dialog(MainActivity.this,R.style.CustomBottomSheetDialogTheme);
        }
        View view = getLayoutInflater().inflate(R.layout.progress_dialog,null);
        ProgressBar bar = view.findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);

//        Glide.with(getApplicationContext()).asGif().load(R.drawable.loading_gif).into(bar);

        dialog.setContentView(view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.show();
    }
    public void hideProgressDialog()
    {
        if(dialog != null)
        {
            View view = getLayoutInflater().inflate(R.layout.progress_dialog,null);
            ProgressBar bar = view.findViewById(R.id.progressBar);
            bar.setVisibility(View.GONE);
            dialog.setContentView(view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dialog.hide();
        }
    }
    private void changeTypeFace()
    {
       Typeface typeface = Methods.getDefaultTypeFace(getApplicationContext());

        TextView textView13 = findViewById(R.id.recent_tv);
        TextView fromGalery = findViewById(R.id.fromGalery);
        TextView createOne = findViewById(R.id.createOne);
        textView13.setTypeface(typeface);
        fromGalery.setTypeface(typeface);
        createOne.setTypeface(typeface);

    }

}







