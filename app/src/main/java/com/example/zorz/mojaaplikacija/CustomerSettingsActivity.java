package com.example.zorz.mojaaplikacija;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.provider.MediaStore.*;

public class CustomerSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField;

    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mProfileImageUrl;

    private ImageView mProfileImage;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1); //broj 1 je broj od bas ovog intenta, tj za biranje slike iz galerije
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveUserInformation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo() {
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) { //ako su unete informacije od strane korisnika
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("profileImageUrl") != null) {
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() throws IOException {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        mCustomerDatabase.updateChildren(userInfo);

        if(resultUri != null) { //ako je korisnik izabrao sliku
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            //pretvaramo Uri u sliku

            bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);


            ByteArrayOutputStream baos = new ByteArrayOutputStream(); //kompresujemo sliku
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data); //uploadujemo sliku u Storage

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mCustomerDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        }
        else {
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData(); //ovaj Uri ima tacnu lokaciju slike koju je korisnik izabrao
            resultUri = imageUri;
            mProfileImage .setImageURI(resultUri); //postavljamo imageView u sliku koju smo dobili kada je korisnik izabrao sliku iz galerije

        }
    }
}