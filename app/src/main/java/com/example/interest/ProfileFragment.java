package com.example.interest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.net.Uri;
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
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView emailTextView;
    private EditText nameEditText, bioEditText;
    private Button saveButton, logoutButton, changeProfileImageButton;
    private ImageView profileImageView;
    private ListView postListView;

    private FirebaseAuth auth;
    private DatabaseReference usersReference, postsReference;
    private FirebaseUser user;
    private List<Post> postList;
    private PostsAdapter postAdapter;
    private Uri imageUri;

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
        profileImageView = view.findViewById(R.id.profileImageView);
        changeProfileImageButton = view.findViewById(R.id.changeProfileImageButton);
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

        // Pulsante per cambiare immagine profilo
        changeProfileImageButton.setOnClickListener(v -> openFileChooser());

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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            encodeImageAndSave();
        }
    }

    private void encodeImageAndSave() {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            usersReference.child(user.getUid()).child("profileImageBase64").setValue(encodedImage)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Immagine aggiornata!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Errore nel salvataggio", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUserData() {
        String userId = user.getUid();
        String email = user.getEmail();

        if (email != null) {
            emailTextView.setText(email);
        }

        usersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String profileImageBase64 = snapshot.child("profileImageBase64").getValue(String.class);

                    if (name == null || name.trim().isEmpty()) {
                        name = email != null ? email.substring(0, email.indexOf("@")) : "NomeUtente";
                        usersReference.child(userId).child("name").setValue(name);
                    }

                    nameEditText.setText(name);
                    if (bio != null) bioEditText.setText(bio);
                    if (profileImageBase64 != null) {
                        byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        profileImageView.setImageBitmap(decodedBitmap);
                    }
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
        // Carica i post dell'utente
        postsReference.orderByChild("userId").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear(); // Puliamo la lista dei post prima di aggiungere i nuovi
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post); // Aggiungi i post alla lista
                    }
                }
                postAdapter.notifyDataSetChanged(); // Aggiorna l'adattatore per visualizzare i post
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Errore nel caricamento dei post", Toast.LENGTH_SHORT).show();
                Log.e("ProfileFragment", "Errore nel caricamento dei post: " + error.getMessage());
            }
        });
    }

    private void updateUserProfile() {
        String newName = nameEditText.getText().toString().trim();
        String newBio = bioEditText.getText().toString().trim();

        if (!newName.isEmpty() && !newBio.isEmpty()) {
            String userId = user.getUid();

            // Crea un oggetto User con i nuovi dati
            User updatedUser = new User(user.getEmail(), newBio,newName );

            // Aggiorna i dati nel database
            usersReference.child(userId).setValue(updatedUser)
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
