package com.jcolladosp.pruebastripe;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import java.util.List;


public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.MyViewHolder> {

    private List<Token> cardList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txToken;

        TextView txCVC;

        TextView txLast;

        TextView txDate;


        public MyViewHolder(View view) {
            super(view);
            txToken = (TextView) view.findViewById(R.id.txToken);
            txDate = (TextView) view.findViewById(R.id.txDate);
            txCVC = (TextView) view.findViewById(R.id.txCVC);
            txLast = (TextView) view.findViewById(R.id.txLast);
        }
    }


    public CardsAdapter(List<Token> cardList) {
        this.cardList = cardList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cell_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Token token = cardList.get(position);

        holder.txToken.setText("Token ID: "+token.getId());
        holder.txDate.setText("Card Expiration Date: "+token.getCard().getExpMonth() +"/" + token.getCard().getExpYear());
        holder.txCVC.setText("Card CVC: "+token.getCard().getNumber());
        holder.txLast.setText("Card last 4 digits: "+token.getCard().getLast4());
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }
}