package com.example.zorz.mojaaplikacija;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PaymentDetails extends AppCompatActivity {

    TextView txtId, txtAmount, txtStatus, txtDetails, ratingDisplayTextView;
    RatingBar ratingBar;
    Button btnSubmit;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("ratings").child("rating");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);
        txtId = (TextView) findViewById(R.id.txtId);
        txtAmount = (TextView) findViewById(R.id.txtAmount);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtDetails = (TextView) findViewById(R.id.details);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnSubmit = (Button) findViewById(R.id.submit);
        ratingDisplayTextView = (TextView) findViewById(R.id.txtRating);

        Intent intent = getIntent();

        try{
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("PaymentDetails"));
            showDetails(jsonObject.getJSONObject("response"), intent.getStringExtra("PaymentAmount"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingDisplayTextView.setVisibility(View.VISIBLE);
                ratingDisplayTextView.setText("Your rating is: " +ratingBar.getRating());
                float rating = ratingBar.getRating();
                HashMap map = new HashMap();
                map.put("rating", rating);
                ref.updateChildren(map);
                //ref.child("rating").setValue(rating);
            }
        });
    }

    private void showDetails(JSONObject response, String paymentAmount) {
        try{
            txtId.setText("Payment ID: " + response.getString("id")); //"'id"
            txtStatus.setText("Payment status: " +response.getString("state")); //"state"
            txtAmount.setText("Amount: "+paymentAmount + "€" );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
