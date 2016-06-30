package com.jcolladosp.pruebastripe;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.gson.Gson;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {



    @Bind(R.id.ed1)
    EditText ed1;
    @Bind(R.id.ed2)
    EditText ed2;
    @Bind(R.id.ed3)
    EditText ed3;
    @Bind(R.id.ed4)
    EditText ed4;
    @Bind(R.id.b_pay)
    Button b_pay;
    @Bind(R.id.checkTV)
    TextView check;

    public SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ed1.addTextChangedListener(new FourDigitCardFormatWatcher());
        b_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCreditCard();
            }
        });
        DBCards cardsDB = new DBCards(this, "DBCards", null, 1);
        db = cardsDB.getWritableDatabase();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.menu_collection:
                Intent a = new Intent(this, CollectionActivity.class);
                startActivity(a);
                break;
        }
        return true;

    }

    public String getStringCreditCard() {
        String creditCard = ed1.getText().toString();
        creditCard = creditCard.replaceAll("-", "");
        return creditCard;

    }
    public void saveCreditCard() {
        String cardnumber = getStringCreditCard();
        Integer exp_month = Integer.parseInt(ed2.getText().toString());
        Integer exp_year = Integer.parseInt(ed3.getText().toString());
        String cvc = ed4.getText().toString();

        Card card = new Card(cardnumber,exp_month,exp_year,cvc);
       if(card.validateCard() && card.validateCVC() && card.validateExpiryDate()){
           check.setText("Valid Card");
           createToken(card);}

       else check.setText("Invalid Card");


    }
    public void createToken(Card card) {
        try {

            Stripe stripe = new Stripe(keys.TEST_KEY);

            stripe.createToken(card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            storeCard(token);

                        }

                        public void onError(Exception e) {
                            Log.e("StripePrueba", "Error in the token", e);
                        }


                    }
            );
        }catch(AuthenticationException eAu){Log.e("StripePrueba", "Error in the token", eAu);}
    }

    private void storeCard(Token token) {
        if (db != null) {

            ContentValues cv=new ContentValues();
            cv.put("tokens", tokenToString(token));

            db.insert("Cards", null, cv);
            db.close();
        }
    }
    public String tokenToString(Token token){
        Gson gson = new Gson();
        return gson.toJson(token);

    }

    public Token stringToToken(String strin){
        Gson gson = new Gson();
        Token token = gson.fromJson(strin, Token.class);
        return token;
    }
}