package com.example.uplift;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText mName,mEmailId,mPassword,mPhoneNumber;
    Button mRegisterBtn;
    TextView mLoginBtn;
    ProgressBar progressBar;

    FirebaseFirestore fstore;
    String userID;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mName = findViewById(R.id.name);
        mEmailId = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhoneNumber = findViewById(R.id.phoneno);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.createtext);
        progressBar = findViewById(R.id.progressbar);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final  String email = mEmailId.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String name = mName.getText().toString();
                final String phoneno = mPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(name)){
                    mName.setError("Name Required!");
                    return;
                }
                if (phoneno.length() !=10)
                {
                    mPhoneNumber.setError("Phone Number must be of 10 digit");
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    mEmailId.setError("Email Required!");
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    mPassword.setError("Password Required!");
                    return;
                }

                if (password.length() < 6){
                    mPassword.setError("Password must contain 6 characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(),"Registeration Successful", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"Onfailure: Email Not Sent" + e.getMessage());
                                }
                            });

                            Toast.makeText(getApplicationContext(),"User Created" , Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fstore.collection("user").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName", name);
                            user.put("email",email);
                            user.put("phone",phoneno);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG,"onsuccess: User Profile Created For"+ userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"onfailure: "+ e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(Register.this, "error"+ task.getException().getMessage(),Toast.LENGTH_SHORT);
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });
            }
        });
    }
}