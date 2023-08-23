package me.indian.allowlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AllowlistGenerator {

    private static final Gson gson = new Gson();
    private static final StringBuilder xuidResponse = new StringBuilder();

    public static void main(String[] args) throws IOException {
        List<PlayerData> playerDataList = loadPlayerDataFromFile("allowlist.json");


        List<PlayerData> newPlayers = new ArrayList<>();
        newPlayers.add(new PlayerData(true, "JndjanBartonka"));
        newPlayers.add(new PlayerData(false, "t9m3k"));
        newPlayers.add(new PlayerData(false, "KENTREXXX1099"));
        newPlayers.add(new PlayerData(false, "RoXuSTheWolf"));
        newPlayers.add(new PlayerData(false, "Komuniz hehe"));
        newPlayers.add(new PlayerData(false, "bqrtqk"));
        newPlayers.add(new PlayerData(false, "LadyZazuka"));
        newPlayers.add(new PlayerData(false, "Nieugietyxd"));
        newPlayers.add(new PlayerData(false, "Skwarka2"));
//        newPlayers.add(new PlayerData(false, "STRZELS0N"));
        newPlayers.add(new PlayerData(false, "PIOTErom"));
        newPlayers.add(new PlayerData(false, "Kasiexx69"));

        for (PlayerData newPlayer : newPlayers) {
            if (!isPlayerInList(playerDataList, newPlayer.getName())) {
                System.out.println("Dodano: " + newPlayer.getName());
                playerDataList.add(newPlayer);
            } else {
                System.out.println("Ominięto " + newPlayer.getName());
            }
        }


        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(newPlayers);

        savePlayerDataToFile(json, "allowlist.json");
    }

    private static boolean isPlayerInList(final List<PlayerData> playerDataList,final  String playerName) {
        for (PlayerData playerData : playerDataList) {
            if (playerData.getName().equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    private static List<PlayerData> loadPlayerDataFromFile(final String fileName) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            PlayerData[] playerDataArray = gson.fromJson(bufferedReader, PlayerData[].class);
            List<PlayerData> playerDataList = new ArrayList<>();
            if (playerDataArray != null) {
                for (PlayerData playerData : playerDataArray) {
                    playerDataList.add(playerData);
                }
            }
            return playerDataList;
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private static void savePlayerDataToFile(final String json,final  String fileName) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            bufferedWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getXuid(final String name) {
        try {
            xuidResponse.setLength(0);
            final URL url = new URL(("https://api.geysermc.org/v2/xbox/xuid/" + name).replaceAll(" " , "%20"));
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    xuidResponse.append(inputLine);
                }

                in.close();
                final JsonObject jsonObject = gson.fromJson(xuidResponse.toString(), JsonObject.class);

                if (jsonObject.has("xuid")) {
                    System.out.println("Znaleziono xuid gracza " + name);
                    System.out.println(url);
                    return jsonObject.get("xuid").getAsLong();
                } else {
                    System.out.println("Klucz 'xuid' nie istnieje w JSON-ie.");
                }
            } else {
                System.out.println("Błąd przy pobieraniu danych. Kod odpowiedzi: " + responseCode);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Nie odnaleziono nazwy");
    }


    static class PlayerData {
        private boolean ignoresPlayerLimit;
        private String name;
        private long xuid;

        public PlayerData(boolean ignoresPlayerLimit, String name) {
            this.ignoresPlayerLimit = ignoresPlayerLimit;
            this.name = name;
            this.xuid = AllowlistGenerator.getXuid(name);
        }

        public String getName() {
            return name;
        }

    }
}