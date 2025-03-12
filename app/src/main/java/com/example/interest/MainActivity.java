package com.example.interest;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interest.databinding.ActivityMainBinding;
import com.example.interest.R;
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ✅ CORRETTO: Inizializza il binding correttamente
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Usa il layout da ViewBinding

        // ✅ CORRETTO: Imposta il listener per il BottomNavigationView
        binding.BottomMenu.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home :
                    Toast.makeText(this, "Home selezionato", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.photo:
                    Toast.makeText(this, "Photo selezionato", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.profile:
                    Toast.makeText(this, "Profile selezionato", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });

        // ✅ CORRETTO: Applica le insets alla tua `TitleTextView`
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.TitleTextView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
