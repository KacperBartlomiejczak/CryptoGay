package com.example.cryptogay;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.cryptogay.util.NavigationHelper;

import org.junit.Test;

public class NavigationBehaviorTest {

    @Test
    public void isBottomNavVisible_whenDetailsFragment_returnsFalse() {
        boolean visible = NavigationHelper.isBottomNavVisible(R.id.detailsFragment);
        assertFalse("Bottom navigation should be hidden on DetailsFragment", visible);
    }

    @Test
    public void isBottomNavVisible_whenMarketFragment_returnsTrue() {
        boolean visible = NavigationHelper.isBottomNavVisible(R.id.marketFragment);
        assertTrue("Bottom navigation should be visible on MarketFragment", visible);
    }

    @Test
    public void isBottomNavVisible_whenFavoritesFragment_returnsTrue() {
        boolean visible = NavigationHelper.isBottomNavVisible(R.id.favoritesFragment);
        assertTrue("Bottom navigation should be visible on FavoritesFragment", visible);
    }

    @Test
    public void isBottomNavVisible_whenAlertsFragment_returnsTrue() {
        boolean visible = NavigationHelper.isBottomNavVisible(R.id.alertsFragment);
        assertTrue("Bottom navigation should be visible on AlertsFragment", visible);
    }
}
