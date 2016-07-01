package com.jcolladosp.pruebastripe;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.stripe.android.model.Token;

import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends AppCompatActivity {
    private List<Token> tokenList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CardsAdapter mAdapter;
    public SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new CardsAdapter(tokenList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        DBCards Dbcards = new DBCards(this, "DBCards", null, 1);
        db = Dbcards.getWritableDatabase();
        addCards();
    }
private void addCards(){
    Cursor c = db.rawQuery("SELECT tokens FROM Cards", null);
    Gson gson = new Gson();

    if (c.moveToLast()) {

        do {
            String xd = c.getString(0);
            Token token = gson.fromJson(xd, Token.class);
            tokenList.add(token);

        } while (c.moveToPrevious());
    }
    c.close();
    db.close();

}

}
