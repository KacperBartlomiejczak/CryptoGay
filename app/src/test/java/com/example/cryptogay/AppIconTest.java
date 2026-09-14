package com.example.cryptogay;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppIconTest {

    private static final String RES_DIR = "src/main/res";
    private static final String MANIFEST_PATH = "src/main/AndroidManifest.xml";

    @Test
    public void testManifestDeclaresLauncherIcons() throws Exception {
        File manifestFile = new File(MANIFEST_PATH);
        assertTrue("AndroidManifest.xml should exist", manifestFile.exists());

        String manifestContent = new String(Files.readAllBytes(Paths.get(MANIFEST_PATH)));
        assertTrue("Manifest should declare android:icon", manifestContent.contains("android:icon=\"@mipmap/ic_launcher\""));
        assertTrue("Manifest should declare android:roundIcon", manifestContent.contains("android:roundIcon=\"@mipmap/ic_launcher_round\""));
    }

    @Test
    public void testLauncherIconsExistInAllMipmapDensities() {
        String[] densities = {"mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};

        for (String density : densities) {
            File dir = new File(RES_DIR, "mipmap-" + density);
            assertTrue("Directory mipmap-" + density + " should exist", dir.exists() && dir.isDirectory());

            File icon = new File(dir, "ic_launcher.webp");
            if (!icon.exists()) {
                icon = new File(dir, "ic_launcher.png");
            }
            assertTrue("ic_launcher should exist in mipmap-" + density, icon.exists());

            File roundIcon = new File(dir, "ic_launcher_round.webp");
            if (!roundIcon.exists()) {
                roundIcon = new File(dir, "ic_launcher_round.png");
            }
            assertTrue("ic_launcher_round should exist in mipmap-" + density, roundIcon.exists());

            File foregroundIcon = new File(dir, "ic_launcher_foreground.png");
            File foregroundXml = new File(RES_DIR, "drawable/ic_launcher_foreground.xml");
            assertTrue("ic_launcher_foreground should exist in mipmap or drawable", foregroundIcon.exists() || foregroundXml.exists());
        }
    }

    @Test
    public void testAdaptiveIconsConfiguredInAnyDpiV26() {
        File anyDpiDir = new File(RES_DIR, "mipmap-anydpi-v26");
        assertTrue("mipmap-anydpi-v26 should exist", anyDpiDir.exists() && anyDpiDir.isDirectory());

        File launcherXml = new File(anyDpiDir, "ic_launcher.xml");
        assertTrue("ic_launcher.xml should exist in mipmap-anydpi-v26", launcherXml.exists());

        File roundLauncherXml = new File(anyDpiDir, "ic_launcher_round.xml");
        assertTrue("ic_launcher_round.xml should exist in mipmap-anydpi-v26", roundLauncherXml.exists());
    }

    @org.junit.Ignore("Czeka na realizacje zadania AppIcon")
    @Test
    public void testRootIconRelocated() {
        File rootIcon = new File("../../icon.png"); // or relative to project root
        File rootIconDirect = new File("icon.png");
        assertFalse("icon.png should be relocated from project root",
                rootIcon.exists() || (rootIconDirect.exists() && rootIconDirect.getAbsolutePath().endsWith("/Cryptogay/icon.png")));
    }
}
