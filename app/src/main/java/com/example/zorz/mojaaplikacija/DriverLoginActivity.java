package com.example.zorz.mojaaplikacija;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {
    private EditText mEmail, mPassword, mPasswordConfirm;
    private Button mLogin, mRegistration, mBack;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null) {
                    Intent intent = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };


        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mPasswordConfirm = (EditText) findViewById(R.id.passconfirm);

        mLogin = (Button) findViewById(R.id.login);
        mRegistration = (Button) findViewById(R.id.registration);
        mBack = (Button) findViewById(R.id.back);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String passconfirm = mPasswordConfirm.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(DriverLoginActivity.this, "You must enter your email address.", Toast.LENGTH_SHORT).show();
                    mEmail.requestFocus();
                } else if (password.isEmpty()) {
                    Toast.makeText(DriverLoginActivity.this, "You must enter your password.", Toast.LENGTH_SHORT).show();
                    mPassword.requestFocus();
                }
                else if (passconfirm.isEmpty()) {
                    Toast.makeText(DriverLoginActivity.this, "You need to confirm your password.", Toast.LENGTH_SHORT).show();

                }
                else if (!password.equals(passconfirm)) {
                    Toast.makeText(DriverLoginActivity.this, "Your passwords don't match.", Toast.LENGTH_SHORT).show();
                }else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {


                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                            } else {
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("email");
                                current_user_db.setValue(email);
                            }
                        }

                    });
                }
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(DriverLoginActivity.this, "You must enter your email address.", Toast.LENGTH_SHORT).show();
                    mEmail.requestFocus();
                } else if (password.isEmpty()) {
                    Toast.makeText(DriverLoginActivity.this, "You must enter your password.", Toast.LENGTH_SHORT).show();
                    mPassword.requestFocus();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


                mBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(DriverLoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                });
        }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
    super.onStop();
    mAuth.removeAuthStateListener(firebaseAuthListener);

    }
}

