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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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

        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvUserEmail = convertView.findViewById(R.id.tvUserEmail);
        TextView tvPostText = convertView.findViewById(R.id.tvPostText);
        Button btnLike = convertView.findViewById(R.id.btnLike);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);
        TextView tvLikeCount = convertView.findViewById(R.id.tvLikeCount);
        ImageView imgProfile = convertView.findViewById(R.id.imgProfile);

        tvUsername.setText(post.getUserName() != null ? post.getUserName() : "Nome non disponibile");
        tvUserEmail.setText(post.getUserEmail() != null ? post.getUserEmail() : "Email non disponibile");
        tvPostText.setText(post.getText());
        loadImageFromBase64(post.getProfileImageBase64(), imgProfile);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.getUid().equals(post.getUserId())) {
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        DatabaseReference postRef = postsReference.child(post.getPostId());
        DatabaseReference likesRef = postRef.child("likes");

        postRef.child("likeCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int likeCount = dataSnapshot.getValue(Integer.class);
                    tvLikeCount.setText(String.valueOf(likeCount));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("PostsAdapter", "Errore nel recupero dei like: " + databaseError.getMessage());
            }
        });

        btnLike.setOnClickListener(v -> {
            if (currentUser != null) {
                String userId = currentUser.getUid();
                likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        postRef.child("likeCount").runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Integer currentLikes = mutableData.getValue(Integer.class);
                                if (currentLikes == null) {
                                    currentLikes = 0; // Inizializza a 0 se non esiste
                                }
                                if (dataSnapshot.exists()) {
                                    // Se l'utente ha già messo like, decrementa e rimuovi il like dall'albero likes
                                    mutableData.setValue(currentLikes - 1);
                                    likesRef.child(userId).removeValue();
                                } else {
                                    // Altrimenti incrementa e aggiungi il like
                                    mutableData.setValue(currentLikes + 1);
                                    likesRef.child(userId).setValue(true);
                                }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                if (databaseError != null) {
                                    Log.e("PostsAdapter", "Errore nell'aggiornamento dei like: " + databaseError.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PostsAdapter", "Errore durante il controllo dei like: " + databaseError.getMessage());
                    }
                });
            }
        });

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

    private void removePostById(String postId) {
        for (int i = 0; i < postList.size(); i++) {
            if (postList.get(i).getPostId().equals(postId)) {
                postList.remove(i);
                break;
            }
        }
    }

    private void loadImageFromBase64(String base64String, ImageView imageView) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                Log.e("PostsAdapter", "Errore nella decodifica dell'immagine: " + e.getMessage());
            }
        }
    }
}