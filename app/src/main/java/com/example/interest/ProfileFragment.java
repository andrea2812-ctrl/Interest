package com.example.interest;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView emailTextView;
    private EditText nameEditText, bioEditText;
    private Button saveButton, logoutButton;
    private ListView postListView;

    private FirebaseAuth auth;
    private DatabaseReference usersReference, postsReference;
    private FirebaseUser user;
    private List<Post> postList;
    private PostsAdapter postAdapter;

    public ProfileFragment() {
        // Costruttore vuoto richiesto
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inizializzazione Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        usersReference = FirebaseDatabase.getInstance().getReference("users");
        postsReference = FirebaseDatabase.getInstance().getReference("posts");

        // Collegamento alle View
        emailTextView = view.findViewById(R.id.emailTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        saveButton = view.findViewById(R.id.saveButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        postListView = view.findViewById(R.id.postListView);

        // Inizializzazione della lista dei post e dell'adattatore
        postList = new ArrayList<>();
        postAdapter = new PostsAdapter(getActivity(), postList);
        postListView.setAdapter(postAdapter);

        // Carica i dati utente e i post
        if (user != null) {
            loadUserData();
            loadUserPosts();
        } else {
            Toast.makeText(getActivity(), "Utente non autenticato", Toast.LENGTH_SHORT).show();
        }

        // Pulsante Salva
        saveButton.setOnClickListener(v -> updateUserProfile());

        // Pulsante Logout
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        String userId = user.getUid();
        emailTextView.setText(user.getEmail());

        usersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);

                    if (name != null) nameEditText.setText(name);
                    if (bio != null) bioEditText.setText(bio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Errore nel caricamento del profilo", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Errore Firebase: " + error.getMessage());
            }
        });
    }

    private void loadUserPosts() {
        postsReference.orderByChild("userId").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Errore nel caricamento dei post", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Errore Firebase: " + error.getMessage());
            }
        });
    }

    private void updateUserProfile() {
        String newName = nameEditText.getText().toString().trim();
        String newBio = bioEditText.getText().toString().trim();

        if (!newName.isEmpty() && !newBio.isEmpty()) {
            String userId = user.getUid();
            usersReference.child(userId).child("name").setValue(newName);
            usersReference.child(userId).child("bio").setValue(newBio)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Profilo aggiornato!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Errore nell'aggiornamento del profilo", Toast.LENGTH_SHORT).show();
                        Log.e("ProfileFragment", "Errore Firebase: " + e.getMessage());
                    });
        } else {
            Toast.makeText(getActivity(), "I campi non possono essere vuoti!", Toast.LENGTH_SHORT).show();
        }
    }
}