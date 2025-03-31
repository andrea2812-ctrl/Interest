package com.example.interest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

        // Usa gli ID corretti definiti nel layout
        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvUserEmail = convertView.findViewById(R.id.tvUserEmail);
        TextView tvPostText = convertView.findViewById(R.id.tvPostText);
        Button btnLike = convertView.findViewById(R.id.btnLike);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);
        TextView tvLikeCount = convertView.findViewById(R.id.tvLikeCount);
        ImageView imgProfile = convertView.findViewById(R.id.imgProfile);

        // Log per il debug dei dati
        Log.d("PostsAdapter", "Nome utente: " + post.getUserName());
        Log.d("PostsAdapter", "Email: " + post.getUserEmail());
        Log.d("PostsAdapter", "ProfileImage (Base64): " + post.getProfileImageBase64());

        // Imposta il nome utente e l'email (verificando che non siano null)
        tvUsername.setText(post.getUserName() != null ? post.getUserName() : "Nome non disponibile");
        if (tvUserEmail != null) {
            tvUserEmail.setText(post.getUserEmail() != null ? post.getUserEmail() : "Email non disponibile");
        }

        // Carica l'immagine del profilo da Base64
        loadImageFromBase64(post.getProfileImageBase64(), imgProfile);

        // Imposta il testo del post
        tvPostText.setText(post.getText());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Mostra o nascondi il pulsante "Elimina" in base all'utente corrente
        if (currentUser != null && currentUser.getUid().equals(post.getUserId())) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Definisci postRef una sola volta per il post corrente
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

        // Gestione del pulsante "Mi Piace"
        DatabaseReference likesRef = postRef.child("likes");
        btnLike.setOnClickListener(v -> {
            if (currentUser != null) {
                String userId = currentUser.getUid();
                likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Rimuovi il like se l'utente ha già messo "Mi Piace"
                            likesRef.child(userId).removeValue();
                            int newLikeCount = post.getLikeCount() - 1;
                            post.setLikeCount(newLikeCount);
                            tvLikeCount.setText(String.valueOf(newLikeCount));
                            postRef.child("likeCount").setValue(newLikeCount);
                        } else {
                            // Aggiungi il like
                            likesRef.child(userId).setValue(true);
                            int newLikeCount = post.getLikeCount() + 1;
                            post.setLikeCount(newLikeCount);
                            tvLikeCount.setText(String.valueOf(newLikeCount));
                            postRef.child("likeCount").setValue(newLikeCount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PostsAdapter", "Errore durante il controllo dei like: " + databaseError.getMessage());
                    }
                });
            }
        });

        // Gestione del pulsante "Elimina"
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

    // Metodo per rimuovere un post dalla lista tramite l'ID
    private void removePostById(String postId) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).getPostId().equals(postId)) {
                postList.remove(i);
                break;
            }
        }
    }

    // Metodo per decodificare la stringa Base64 e caricare l'immagine nel ImageView
    private void loadImageFromBase64(String base64String, ImageView imageView) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                Log.e("PostsAdapter", "Errore nella decodifica dell'immagine: " + e.getMessage());
            }
        } else {
            Log.d("PostsAdapter", "La stringa Base64 è vuota o nulla");
        }
    }
}
