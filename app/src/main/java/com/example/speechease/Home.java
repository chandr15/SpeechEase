package com.example.speechease;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.speechease.Utils.Save;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.regex.Pattern;

public class Home extends AppCompatActivity implements View.OnClickListener {
   // Button btnLogin;
    Button btnSignIn;
    boolean session;
    Button go;
    private EditText emailId,password,number1,fname1;
    FirebaseAuth mFirebaseAuth;
   // ImageView chatbot;
    private CountryCodePicker ccp;
    private ProgressBar progressBars;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(getApplicationContext());
     //   btnLogin = findViewById(R.id.btn_signin);
        mFirebaseAuth = FirebaseAuth.getInstance();
        progressBars = findViewById(R.id.progressBar2);
        progressBars.setVisibility(View.GONE);

        emailId = findViewById(R.id.email);
        password = findViewById(R.id.password);
        fname1 = findViewById(R.id.fname);
        number1 = findViewById(R.id.cnumber);
        //chatbot=findViewById( R.id.chatbot );
        ccp=findViewById( R.id.ccp );
        ccp.setCountryForNameCode("CA");

        findViewById( R.id.go ).setOnClickListener( this );
SESSION();
        btnSignIn=findViewById(R.id.btn_signin);

        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==btnSignIn){
                    Intent intent = new Intent(getApplicationContext(), Home_Login.class);
                    startActivity(intent);
                    emailId.setText( "" );
                    password.setText( "" );
                    fname1.setText( "" );
                    number1.setText( "" );
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();
                }
            }
        } );

    }

    public void SESSION(){
        session= Boolean.valueOf( Save.read(getApplicationContext(),"session","false"));
        if(session){
            //here when user first or logout
            //In here,intent to signup for first reg

            Toast.makeText(this,"Already Logged In",Toast.LENGTH_LONG).show();
            Intent signup=new Intent(getApplicationContext(),Dashboard.class);
            startActivity(signup);

            finish();
        }
        else{
            //here when user logged in
            //value here is true
            //Toast.makeText(this,"ALREADY LOGGED IN",Toast.LENGTH_SHORT).show();


        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseApp.initializeApp(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.go:
                progressBars.setVisibility(View.VISIBLE);
                findViewById( R.id.go ).setVisibility( View.VISIBLE );
                final String phonenumber=ccp.getSelectedCountryCode();
                boolean valid = validateUser();


                if (valid) {
                    final String number=number1.getText().toString().trim();
                    DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("Users");
                    dbref.keepSynced(true);
                    dbref.orderByChild("contactn").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null){
                                progressBars.setVisibility(View.GONE);
                                findViewById(R.id.go).setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(),"User on this phone number already exists",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                final String email = emailId.getText().toString().trim();
                                final String pwd = password.getText().toString().trim();
                                final String fname = fname1.getText().toString().trim();

                                final String number = number1.getText().toString().trim();
                                progressBars.setVisibility(View.GONE);

                                Intent intent = new Intent( getApplicationContext(), Verification.class );
                                intent.putExtra( "name", fname );
                                intent.putExtra( "email", email );
                                intent.putExtra( "password", pwd );
                                intent.putExtra( "code", phonenumber );
                                intent.putExtra( "number", number );

                                startActivity( intent );

                                //  progressBar.setVisibility(View.GONE);
                                findViewById( R.id.go ).setVisibility( View.VISIBLE );
                                //Toast.makeText(RegAct.this,"NO user found",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else {
                    progressBars.setVisibility(View.GONE);

                }
                break;
        }


    }


    private boolean validateUser() {
        final String email=emailId.getText().toString().trim();
        final String pwd=password.getText().toString().trim();
        final String fname=fname1.getText().toString().trim();

        final String number=number1.getText().toString().trim();


        if(fname.isEmpty()){
            fname1.setError(getString(R.string.input_error_name));
            fname1.requestFocus();
            return false;
        }
        else if(email.isEmpty()){
            emailId.setError(getString(R.string.input_error_email));
            emailId.requestFocus();
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailId.setError(getString(R.string.input_error_email_invalid));
            emailId.requestFocus();
            return false;
        }
        else if(pwd.isEmpty()){
            Toast.makeText( getApplicationContext(),"Password Required",Toast.LENGTH_SHORT ).show();
            return false;
        }
        else if (pwd.length() < 6 ) {
            Toast.makeText( getApplicationContext(),"Password should be least 6 characters long",Toast.LENGTH_SHORT ).show();
            return false;
        }
        else if(!PASSWORD_PATTERN.matcher(pwd).matches()){
                Toast.makeText(getApplicationContext(),"Password does not match the given criteria",Toast.LENGTH_SHORT).show();
//            Toast.makeText( getApplicationContext(),"1 Digit? \n 1 LowerCase? \n 1 UpperCase? \n 1 Special Character? \n least 6 character?",Toast.LENGTH_SHORT ).show();
            return false;
        }



        else if(number.isEmpty()){
            number1.setError("Please Enter Your Number");
            number1.requestFocus();
            return false;
        }

        else if (number.length() != 10) {
            number1.setError(getString(R.string.input_error_phone_invalid));
            number1.requestFocus();
            return false;
        }


        else {
            return true;
        }
    }
}