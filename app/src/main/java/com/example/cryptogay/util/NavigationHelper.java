package com.example.cryptogay.util;

import com.example.cryptogay.R;

public final class NavigationHelper {

    private NavigationHelper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Determines whether the BottomNavigationView should be visible for a given destination ID.
     * DetailsFragment is a detail screen, so the bottom navigation bar is hidden.
     *
     * @param destinationId ID of the current destination in the navigation graph
     * @return true if bottom navigation should be visible, false otherwise
     */
    public static boolean isBottomNavVisible(int destinationId) {
        return destinationId != R.id.detailsFragment;
    }
}
