package com.sba.sinhalaphotoeditor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sba.sinhalaphotoeditor.sdk.Methods;
import com.sba.sinhalaphotoeditor.R;
import com.sba.sinhalaphotoeditor.model.SihalaUser;


import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_KEY;
import static com.sba.sinhalaphotoeditor.config.Constants.LANGUAGE_SINHALA;
import static com.sba.sinhalaphotoeditor.config.Constants.SHARED_PREF_NAME;

public class ConfirmPhoneNumber extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private String name;
    private String phone;
    private String uri;
    private Uri filePath;
    private StorageReference storageReference;
    private String code = "null";
    private ProgressDialog pd;
    private String CountryCode;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_start_animation__for_tools,R.anim.activity_exit_animation__for_tools);
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

        setContentView(R.layout.activity_confirm_phone_number);

        Methods.freeUpMemory();

        Pinview pin = (Pinview) findViewById(R.id.pinview);
        TextView textView = (TextView) findViewById(R.id.textView);
        Button varify = (Button) findViewById(R.id.varify);
        CircleImageView profilePicture = findViewById(R.id.profilePicture);


        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        uri = intent.getStringExtra("uri");
        CountryCode = intent.getStringExtra("CountryCode");
        mVerificationId = intent.getStringExtra("mVerificationId");
        code = intent.getStringExtra("code");

        if(code != null)
        {
            pin.setValue(code);
        }
        else
        {
            code = "null";
        }

        pd = new ProgressDialog(ConfirmPhoneNumber.this);
        pd.setCancelable(false);
        pd.setMessage("Verifying..");



        if(CountryCode != null && phone != null)
        {
            textView.setText("Enter the code we've sent \nto " + CountryCode + phone);
        }
        if(uri != null)
        {
            filePath = Uri.parse(uri);
            profilePicture.setImageURI(filePath);
            profilePicture.setBorderWidth(4);
            profilePicture.setBorderColor(Color.WHITE);

        }


        pin.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser)
            {
                if(fromUser)
                {
                    code = pinview.getValue();
                }

            }
        });


        varify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if (!code.equals("null") && code.length() == 6)
                {
                    pd.show();
                    verifyVerificationCode(code);
                }
                else
                {
                    Methods.showCustomToast(ConfirmPhoneNumber.this, getString(R.string.enter_valid_code_text));
                }


            }
        });




    }

    private void verifyVerificationCode(String code)
    {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(ConfirmPhoneNumber.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {

                            registerUser();
                        }
                        else
                        {

                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                Toast.makeText(ConfirmPhoneNumber.this, "Code is incorrect!", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ConfirmPhoneNumber.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();


                        }
                    }
                });
    }
    private void registerUser()
    {
        try
        {
            if (uri != null)
            {

                final StorageReference sRef = storageReference.child("ProfilePictures/" + CountryCode + phone + "." + getFileExtension(filePath));
                

                //adding the file to reference
                sRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                            {
//                                String profilePicId = taskSnapshot.getDownloadUrl().toString();

                                sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri)
                                    {
                                        String profilePicId =  uri.toString();


                                        SihalaUser user = new SihalaUser();
                                        user.setPhoneNumber(CountryCode + phone);
                                        user.setUserProfilePic(profilePicId);
                                        user.setUserName(name);
                                        user.setUserId(mAuth.getCurrentUser().getUid());

                                        user.saveUser(ConfirmPhoneNumber.this,user);





//                                UserDetails user = new UserDetails(mAuth.getCurrentUser().getUid(),name,phone,profilePicId,password);


                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(CountryCode + phone)
                                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {

                                                if(task.isSuccessful())
                                                {

                                                    if(FirebaseAuth.getInstance().getCurrentUser() != null)
                                                    {
                                                        FirebaseAuth.getInstance().getCurrentUser().delete();
                                                    }


                                                    Intent intent = new Intent(ConfirmPhoneNumber.this,MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);


                                                    Methods.showCustomToast(ConfirmPhoneNumber.this,getString(R.string.registration_success_text));
                                                }
                                                else
                                                {
                                                    Methods.showCustomToast(ConfirmPhoneNumber.this,"Failed to Register, Check Your Connection!");
                                                }
                                                pd.dismiss();
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {


                                    }
                                });





                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {

                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                            {

                            }
                        });
            }
            else
            {
                //display an error if no file is selected
            }


            //Toast.makeText(RegisterScreen.this,"Successfully Registered!",Toast.LENGTH_SHORT).show();

        }
        catch (Exception e)
        {
            Toast.makeText(ConfirmPhoneNumber.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    public String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String re =  mime.getExtensionFromMimeType(cR.getType(uri));
        if(re == null)
        {
            return "PNG";
        }
        return re;
    }
}
