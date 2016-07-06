package com.jcolladosp.pruebastripe;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.cooltechworks.creditcarddesign.CardEditActivity;
import com.cooltechworks.creditcarddesign.CreditCardUtils;
import com.google.gson.Gson;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class MainActivity extends AppCompatActivity {



    @Bind(R.id.b_pay)
    Button b_pay;
    @Bind(R.id.checkTV)
    TextView check;
    @Bind(R.id.bFill)
    Button bFill;
    @Bind(R.id.edPrice)
    EditText edPrice;

    public SQLiteDatabase db;
    public Stripe stripe;

    public String cardHolderName = "";
    public String creditCardNumber = "";
    public String expiryDate = "";
    public String cvv = "";
    public int expmonth = 0;
    public int expyear = 0;
    public int price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        b_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int GET_NEW_CARD = 2;
                Intent intent = new Intent(MainActivity.this, CardEditActivity.class);

                startActivityForResult(intent, GET_NEW_CARD);
            }
        });
        bFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fillText();
            }
        });


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

        switch (item.getItemId()) {
            case R.id.menu_collection:
                Intent a = new Intent(this, CollectionActivity.class);
                startActivity(a);
                break;
            case R.id.menu_camera:
                onScanPress(findViewById(android.R.id.content));
                break;
            case R.id.menu_stripe:
                Intent e = new Intent(this, StripeConnectActivity.class);
                startActivity(e);
        }
        return true;

    }


    public void createCreditCard() {

        Card card = new Card(creditCardNumber, expmonth, expyear, cvv);
        if (card.validateCard() && card.validateCVC() && card.validateExpiryDate()) {
            check.setText(R.string.valid_card);
            card.setName(cardHolderName);
            createToken(card);
        } else check.setText(R.string.invalid_card);


    }

    public void createToken(Card card) {
        try {

            stripe = new Stripe(keys.TEST_PUBLISHABLE_KEY);

            stripe.createToken(card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            getPrice();
                            storeCard(token);

                        }

                        public void onError(Exception e) {
                            Log.e(getString(R.string.log_stripe), getString(R.string.token_error), e);
                        }


                    }
            );
        } catch (AuthenticationException eAu) {
            Log.e(getString(R.string.log_stripe), getString(R.string.error_auth), eAu);
        }
    }


    private void storeCard(Token token) {

        JSONObject body = new JSONObject();
        try {
            body.put("token", token.getId());
            body.put("price", price);
            body.put("seller", "acct_18TpmlFe4c8fg7qS");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = Utils.makeStringRequest(Request.Method.POST, keys.SERVER, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("PAGOS", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("PAGOS", error.toString());
            }
        }, this);
        Volley.newRequestQueue(this).add(request);

        db = null;
        try {
            DBCards cardsDB = new DBCards(this, "DBCards", null, 1);
            db = cardsDB.getWritableDatabase();
            synchronized (db) {
                ContentValues cv = new ContentValues();
                cv.put(getString(R.string.tokens), tokenToString(token));
                db.insert(getString(R.string.cards), null, cv);
            }
        } catch (Exception e) {
            Log.e(getString(R.string.log_stripe), getString(R.string.database_error));

        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void fillText() {
        edPrice.setText(R.string.price_example);

    }

    public void getPrice() {
        price = Integer.parseInt(edPrice.getText().toString()) * 100;
    }

    public String tokenToString(Token token) {
        Gson gson = new Gson();
        return gson.toJson(token);

    }

    public void onScanPress(View v) {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, true); // default: false

        int MY_SCAN_REQUEST_CODE = 100;
        startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 2) {

            cardHolderName = data.getStringExtra(CreditCardUtils.EXTRA_CARD_HOLDER_NAME);
            creditCardNumber = data.getStringExtra(CreditCardUtils.EXTRA_CARD_NUMBER);
            expiryDate = data.getStringExtra(CreditCardUtils.EXTRA_CARD_EXPIRY);
            cvv = data.getStringExtra(CreditCardUtils.EXTRA_CARD_CVV);

            expmonth = Integer.parseInt(expiryDate.substring(0, 2));
            expyear = Integer.parseInt(expiryDate.substring(3));
        }
        if (requestCode == 100) {

            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                creditCardNumber = scanResult.cardNumber;

                if (scanResult.isExpiryValid()) {
                    expmonth = scanResult.expiryMonth;
                    expyear = scanResult.expiryYear;

                }

                if (scanResult.cvv != null) {
                    cvv = scanResult.cvv;
                }


            } else {
                check.setText(R.string.scan_canceled);
            }
        }

        createCreditCard();

    }

}