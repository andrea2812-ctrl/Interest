package com.example.interest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton, resendEmailButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        resendEmailButton = findViewById(R.id.resendEmailButton);

        // Login con verifica email
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Compila tutti i campi", Toast.LENGTH_SHORT).show();
            }
        });

        // Registrazione con invio email di verifica
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                registerUser(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Compila tutti i campi", Toast.LENGTH_SHORT).show();
            }
        });

        // Reinvio email di verifica
        resendEmailButton.setOnClickListener(v -> resendVerificationEmail());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Login riuscito!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Verifica la tua email prima di accedere!", Toast.LENGTH_LONG).show();
                            auth.signOut(); // Disconnette l'utente se l'email non è verificata
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Errore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                if (emailTask.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email di verifica inviata! Controlla la tua casella di posta.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Errore nell'invio della verifica!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            saveUserToDatabase(user);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Errore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        String userId = user.getUid();
        User newUser = new User(user.getEmail(), "Nome Utente", "Bio di default");

        databaseReference.child(userId).setValue(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Registrazione completata! Verifica la tua email prima di accedere.", Toast.LENGTH_LONG).show();
                auth.signOut(); // Disconnette l'utente fino alla verifica
            } else {
                Toast.makeText(LoginActivity.this, "Errore nel salvataggio dei dati", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email di verifica inviata di nuovo!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Errore nell'invio della verifica!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Accedi prima di richiedere una nuova email di verifica.", Toast.LENGTH_SHORT).show();
        }
    }
}
