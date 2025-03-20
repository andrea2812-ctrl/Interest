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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
        TextView tvLikeCount = convertView.findViewById(R.id.tvLikeCount); // Contatore dei like

        tvAccount.setText(post.getUserEmail());
        tvPostText.setText(post.getText());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Verifica se l'utente corrente è il creatore del post
        if (currentUser != null && currentUser.getUid().equals(post.getUserId())) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Definisci postRef una sola volta
        DatabaseReference postRef = postsReference.child(post.getPostId());

        // Recupera il numero di like da Firebase
        postRef.child("likeCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int likeCount = dataSnapshot.getValue(Integer.class);
                    tvLikeCount.setText(String.valueOf(likeCount));
                    post.setLikeCount(likeCount); // Aggiorna il post locale
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("PostsAdapter", "Errore nel recupero dei like: " + databaseError.getMessage());
            }
        });

        // Recupera la lista degli utenti che hanno messo "Mi Piace" su questo post
        DatabaseReference likesRef = postRef.child("likes");

        btnLike.setOnClickListener(v -> {
            // Controlla se l'utente ha già messo "Mi Piace" al post
            if (currentUser != null) {
                String userId = currentUser.getUid();
                likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Se l'utente ha già messo "Mi Piace", rimuovilo
                            likesRef.child(userId).removeValue();
                            int newLikeCount = post.getLikeCount() - 1;
                            post.setLikeCount(newLikeCount);
                            tvLikeCount.setText(String.valueOf(newLikeCount));
                            postRef.child("likeCount").setValue(newLikeCount); // Aggiorna Firebase
                        } else {
                            // Se l'utente non ha messo "Mi Piace", aggiungilo
                            likesRef.child(userId).setValue(true);
                            int newLikeCount = post.getLikeCount() + 1;
                            post.setLikeCount(newLikeCount);
                            tvLikeCount.setText(String.valueOf(newLikeCount));
                            postRef.child("likeCount").setValue(newLikeCount); // Aggiorna Firebase
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PostsAdapter", "Errore durante il controllo dei like: " + databaseError.getMessage());
                    }
                });
            }
        });

        // Logica per il pulsante "Elimina"
        btnDelete.setOnClickListener(v -> {
            postRef.removeValue().addOnSuccessListener(aVoid -> {
                removePostById(post.getPostId());
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
