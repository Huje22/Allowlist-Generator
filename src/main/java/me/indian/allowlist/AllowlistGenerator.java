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
import java.util.Arrays;
import java.util.List;

public class AllowlistGenerator {

    private static final Gson gson = new Gson();
    private static final StringBuilder xuidResponse = new StringBuilder();
    private static final  String name = "allowlist.json";

    public static void main(String[] args) throws IOException {
        final List<PlayerData> playerDataList = loadPlayerDataFromFile();
        final List<PlayerData> newPlayers = new ArrayList<>();


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

        for (final PlayerData newPlayer : newPlayers) {
            if (!isPlayerInList(playerDataList, newPlayer.getName())) {
                System.out.println("Dodano: " + newPlayer.getName());
                playerDataList.add(newPlayer);
            } else {
                System.out.println("Ominięto " + newPlayer.getName());
            }
        }


        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        final String json = gson.toJson(newPlayers);

        System.out.println(json);

        savePlayerDataToFile(json);
    }

    private static boolean isPlayerInList(final List<PlayerData> playerDataList, final String playerName) {
        for (final PlayerData playerData : playerDataList) {
            if (playerData.getName().equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    private static List<PlayerData> loadPlayerDataFromFile() throws IOException {
        //Nie znam sie na bibliotece gson , chat gtp mi pomagal z nią :D
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(name))) {
            final PlayerData[] playerDataArray = gson.fromJson(bufferedReader, PlayerData[].class);
            final List<PlayerData> playerDataList = new ArrayList<>();
            if (playerDataArray != null) {
                playerDataList.addAll(Arrays.asList(playerDataArray));
            }
            return playerDataList;
        } catch (final FileNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private static void savePlayerDataToFile(final String json) {
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(name))) {
            bufferedWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getXuid(final String name) {
        try {
            xuidResponse.setLength(0);
            final URL url = new URL(("https://api.geysermc.org/v2/xbox/xuid/" + name).replaceAll(" ", "%20"));
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
        private final boolean ignoresPlayerLimit;
        private final String name;
        private final long xuid;

        public PlayerData(boolean ignoresPlayerLimit, String name) {
            this.ignoresPlayerLimit = ignoresPlayerLimit;
            this.name = name;
            this.xuid = AllowlistGenerator.getXuid(name);
        }

        public boolean isIgnoresPlayerLimit() {
            return this.ignoresPlayerLimit;
        }

        public String getName() {
            return this.name;
        }

        public long getXuid() {
            return this.xuid;
        }
    }
}