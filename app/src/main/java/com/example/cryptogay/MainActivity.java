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

            // Hide bottom navigation on detail screens, show on top-level tabs
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                boolean isVisible = com.example.cryptogay.util.NavigationHelper.isBottomNavVisible(destination.getId());
                binding.bottomNav.setVisibility(isVisible ? android.view.View.VISIBLE : android.view.View.GONE);
            });

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

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra("coin_id")) {
            String coinId = intent.getStringExtra("coin_id");
            if (coinId != null && !coinId.isEmpty()) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment != null) {
                    NavController navController = navHostFragment.getNavController();
                    Bundle args = new Bundle();
                    args.putString("coin_id", coinId);
                    navController.navigate(R.id.detailsFragment, args);
                }
            }
        }
    }
}