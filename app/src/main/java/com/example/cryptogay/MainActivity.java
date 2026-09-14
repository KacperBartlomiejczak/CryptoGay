package com.example.cryptogay;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.cryptogay.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            // Handle navigation from price alert push notification
            if (getIntent() != null && getIntent().hasExtra("coin_id")) {
                String coinId = getIntent().getStringExtra("coin_id");
                if (coinId != null && !coinId.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putString("coin_id", coinId);
                    navController.navigate(R.id.detailsFragment, args);
                }
            }
        }

        // Initialize notification channel and schedule periodic price alert checks
        com.example.cryptogay.notification.AlertNotificationHelper.createNotificationChannel(this);
        com.example.cryptogay.worker.PriceAlertWorker.schedulePeriodicCheck(this);
    }
}