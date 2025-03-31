package com.example.interest;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PhotoFragment extends Fragment {

    private EditText editText;
    private Button postButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference usersReference;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        // Inizializza Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        usersReference = FirebaseDatabase.getInstance().getReference("users");

        // Collega gli elementi XML
        editText = view.findViewById(R.id.editText);
        postButton = view.findViewById(R.id.button);

        // Listener del bottone
        postButton.setOnClickListener(v -> publishPost());

        return view;
    }

    private void publishPost() {
        String postText = editText.getText().toString().trim();

        if (postText.isEmpty()) {
            Toast.makeText(getActivity(), "Inserisci un testo prima di pubblicare!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null) {
            String userId = user.getUid();
            String postId = databaseReference.push().getKey(); // Genera un ID univoco per il post
            String userEmail = user.getEmail();

            // Recupera lo username e l'immagine del profilo (base64) dell'utente
            getUserProfileData(userId, (userName, profileImageBase64) -> {
                // Crea la mappa con i dati del post
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("postId", postId);
                postMap.put("userId", userId);
                postMap.put("userEmail", userEmail);
                postMap.put("userName", userName); // Aggiungi lo username
                postMap.put("text", postText);
                postMap.put("timestamp", System.currentTimeMillis());

                // Aggiungi l'immagine in base64 se disponibile
                if (profileImageBase64 != null) {
                    postMap.put("profileImageBase64", profileImageBase64);
                }

                if (postId != null) {
                    databaseReference.child(postId).setValue(postMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Post pubblicato!", Toast.LENGTH_SHORT).show();
                            editText.setText(""); // Svuota il campo dopo la pubblicazione
                        } else {
                            Toast.makeText(getActivity(), "Errore nella pubblicazione!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    // Funzione per recuperare lo username e l'immagine del profilo dell'utente
    private void getUserProfileData(String userId, ProfileDataCallback callback) {
        usersReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String userName = task.getResult().child("userName").getValue(String.class);
                String profileImageBase64 = task.getResult().child("profileImageBase64").getValue(String.class);

                // Se lo username non è presente, usa una stringa di default
                if (userName == null || userName.isEmpty()) {
                    userName = "Utente";
                }

                callback.onCallback(userName, profileImageBase64);
            } else {
                callback.onCallback("Utente", null);
            }
        });
    }

    // Interfaccia di callback per passare i dati
    private interface ProfileDataCallback {
        void onCallback(String userName, String profileImageBase64);
    }
}
