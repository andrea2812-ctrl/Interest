package com.example.interest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class PostsAdapter extends BaseAdapter {

    private Context context;
    private List<Post> postList;

    // Costruttore dell'adapter
    public PostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @Override
    public int getCount() {
        return postList.size();  // Restituisce il numero di post
    }

    @Override
    public Object getItem(int position) {
        return postList.get(position);  // Restituisce il post alla posizione specificata
    }

    @Override
    public long getItemId(int position) {
        return position;  // Restituisce la posizione dell'elemento nella lista
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Crea una nuova view se non c'è una view riciclata
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_post, null);
        }

        // Ottieni l'oggetto Post alla posizione data
        Post post = postList.get(position);

        // Trova le viste (TextView e Button) nel layout
        TextView tvAccount = convertView.findViewById(R.id.tvAccount);
        TextView tvPostText = convertView.findViewById(R.id.tvPostText);
        Button btnLike = convertView.findViewById(R.id.btnLike);

        // Imposta i dati nelle viste
        tvAccount.setText(post.getUserEmail());
        tvPostText.setText(post.getText());

        // Esempio di gestione del click sul pulsante "Mi Piace"
        btnLike.setOnClickListener(v -> {
            // Logica per "Mi Piace" (per esempio, incrementare il numero di "Mi Piace" o cambiare il colore del pulsante)
        });

        return convertView;  // Restituisci la vista personalizzata per questo elemento
    }
}
