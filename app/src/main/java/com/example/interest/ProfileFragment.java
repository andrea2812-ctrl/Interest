package com.example.interest;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView emailTextView;
    private EditText nameEditText, bioEditText;
    private Button saveButton, logoutButton;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inizializzazione Firebase
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Collegamento alle View
        emailTextView = view.findViewById(R.id.emailTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        saveButton = view.findViewById(R.id.saveButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Carica i dati del profilo utente
        loadUserData();

        // Salva le modifiche
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });

        // Logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadUserData() {
        if (user != null) {
            String userId = user.getUid();
            emailTextView.setText(user.getEmail()); // Mostra l'email

            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String bio = snapshot.child("bio").getValue(String.class);

                        nameEditText.setText(name);
                        bioEditText.setText(bio);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Errore nel caricamento del profilo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserProfile() {
        String newName = nameEditText.getText().toString().trim();
        String newBio = bioEditText.getText().toString().trim();

        if (!newName.isEmpty() && !newBio.isEmpty()) {
            String userId = user.getUid();
            databaseReference.child(userId).child("name").setValue(newName);
            databaseReference.child(userId).child("bio").setValue(newBio);

            Toast.makeText(getActivity(), "Profilo aggiornato!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "I campi non possono essere vuoti!", Toast.LENGTH_SHORT).show();
        }
    }
}
