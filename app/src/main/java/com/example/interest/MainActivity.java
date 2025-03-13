package com.example.interest;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.interest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inizializza il binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Imposta il listener per il BottomNavigationView usando if-else invece di switch
        binding.BottomMenu.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Usa un if-else per selezionare il Fragment giusto
            if (item.getItemId() == R.id.home) {
                // Qui puoi impostare il Fragment per la Home
                selectedFragment = new HomeFragment(); // Assumendo che tu abbia creato HomeFragment
            } else if (item.getItemId() == R.id.photo) {
                // Qui puoi impostare il Fragment per la Photo
                selectedFragment = new PhotoFragment(); // Assumendo che tu abbia creato PhotoFragment
            } else if (item.getItemId() == R.id.profile) {
                // Qui puoi impostare il Fragment per il profilo
                selectedFragment = new ProfileFragment(); // Assumendo che tu abbia creato ProfileFragment
            }

            // Se un Fragment è stato selezionato, sostituirlo
            if (selectedFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, selectedFragment); // R.id.fragment_container è il container dove vuoi aggiungere il Fragment
                transaction.addToBackStack(null); // Aggiungi la transazione alla back stack per permettere di tornare indietro
                transaction.commit();
            }

            return true;
        });

        // Applica gli insetti alla vista principale
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
}