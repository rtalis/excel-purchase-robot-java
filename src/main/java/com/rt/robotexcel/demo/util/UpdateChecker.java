package com.rt.robotexcel.demo.util;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.json.JSONObject;

public class UpdateChecker {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/rtalis/excel-purchase-robot-java/releases/latest";

    public static void checkForUpdates() {
        try {
            // 1. Load current version from properties
            Properties props = new Properties();
            try (InputStream is = UpdateChecker.class.getClassLoader().getResourceAsStream("application.properties")) {
                props.load(is);
            }
            String currentVersion = props.getProperty("app.version", "0.0.0").replace("v", "");

            // 2. Query GitHub API asynchronously
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_API_URL))
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(body -> {
                        JSONObject json = new JSONObject(body);
                        String latestVersion = json.getString("tag_name").replace("v", "");
                        String downloadUrl = json.getJSONArray("assets").getJSONObject(0)
                                .getString("browser_download_url");

                        // 3. Compare versions
                        if (isNewer(latestVersion, currentVersion)) {

                            SwingUtilities.invokeLater(() -> {
                                int choice = JOptionPane.showConfirmDialog(null,
                                        "Nova versão " + latestVersion + " disponível. Deseja atualizar agora?",
                                        "Atualização Disponível",
                                        JOptionPane.YES_NO_OPTION);

                                if (choice == JOptionPane.YES_OPTION) {
                                    Updater.downloadAndInstall(downloadUrl);
                                }
                            });
                        }
                    });

        } catch (Exception e) {
            System.err.println("Não foi possível verificar atualizações: " + e.getMessage());
        }
    }

    private static boolean isNewer(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");
        for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
            int l = Integer.parseInt(latestParts[i]);
            int c = Integer.parseInt(currentParts[i]);
            if (l > c)
                return true;
            if (l < c)
                return false;
        }
        return latestParts.length > currentParts.length;
    }
}
