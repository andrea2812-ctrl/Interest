package com.example.interest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PostsAdapter extends BaseAdapter {

    private Context context;
    private List<Post> postList;
    private DatabaseReference postsReference;

    public PostsAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.postsReference = FirebaseDatabase.getInstance().getReference("posts");
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int position) {
        return postList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_post, null);
        }

        Post post = postList.get(position);

        TextView tvAccount = convertView.findViewById(R.id.tvAccount);
        TextView tvPostText = convertView.findViewById(R.id.tvPostText);
        Button btnLike = convertView.findViewById(R.id.btnLike);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        tvAccount.setText(post.getUserEmail());
        tvPostText.setText(post.getText());

        // Ottieni l'utente corrente
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Verifica se l'utente corrente è il creatore del post
        if (currentUser != null && currentUser.getUid().equals(post.getUserId())) {
            btnDelete.setVisibility(View.VISIBLE); // Mostra il pulsante di eliminazione
        } else {
            btnDelete.setVisibility(View.GONE); // Nascondi il pulsante di eliminazione
        }

        // Logica per il pulsante "Like"
        btnLike.setOnClickListener(v -> {
            // Implementa la logica per il "Mi piace" qui
            Toast.makeText(context, "Hai messo Mi piace al post", Toast.LENGTH_SHORT).show();
        });

        // Logica per il pulsante "Elimina"
        btnDelete.setOnClickListener(v -> {
            String postId = post.getPostId(); // Usa l'ID del post invece della posizione
            DatabaseReference postRef = postsReference.child(postId);

            postRef.removeValue().addOnSuccessListener(aVoid -> {
                // Rimuovi il post dalla lista usando l'ID per evitare errori di posizione
                removePostById(postId);
                notifyDataSetChanged();
                Toast.makeText(context, "Post eliminato", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Errore nell'eliminazione", Toast.LENGTH_SHORT).show();
                Log.e("PostsAdapter", "Errore durante l'eliminazione del post: " + e.getMessage());
            });
        });

        return convertView;
    }

    // Metodo per rimuovere un post tramite ID
    private void removePostById(String postId) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).getPostId().equals(postId)) {
                postList.remove(i);
                break; // Esci dal ciclo dopo la rimozione
            }
        }
    }
}