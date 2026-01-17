package com.example.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.HashMap;

import com.example.practicaltest02.general.Constants;
import com.example.practicaltest02.model.WeatherForecastInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // 1. Obtinem stream-urile de intrare/iesire pentru a vorbi cu clientul
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");

            // 2. Citim orasul si tipul informatiei cerute
            String city = bufferedReader.readLine();
            String informationType = bufferedReader.readLine();

            if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }

            // 3. Verificam daca avem datele in cache (HashMap-ul din ServerThread)
            HashMap<String, WeatherForecastInformation> data = serverThread.getAllData();
            WeatherForecastInformation weatherForecastInformation = null;

            if (data.containsKey(city)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(city);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");

                // 4. Daca nu, facem cerere HTTP catre OpenWeatherMap
                String httpClient = Constants.WEB_SERVICE_ADDRESS + "?q=" + city + "&appid=" + Constants.WEB_SERVICE_API_KEY + "&units=" + Constants.UNITS;

                URL url = new URL(httpClient);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String pageSourceCode = sb.toString();

                // 5. Parsam JSON-ul primit
                JSONObject content = new JSONObject(pageSourceCode);

                JSONArray weatherArray = content.getJSONArray("weather");
                JSONObject weather;
                String condition = "";
                if (weatherArray.length() > 0) {
                    weather = weatherArray.getJSONObject(0);
                    condition = weather.getString("main");
                }

                JSONObject main = content.getJSONObject("main");
                String temperature = main.getString("temp");
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                JSONObject wind = content.getJSONObject("wind");
                String windSpeed = wind.getString("speed");

                // Cream obiectul cu datele meteo
                weatherForecastInformation = new WeatherForecastInformation(
                        temperature, windSpeed, condition, pressure, humidity
                );

                // Il salvam in cache-ul serverului
                serverThread.setData(city, weatherForecastInformation);
            }

            if (weatherForecastInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }

            // 6. Trimitem raspunsul inapoi la client
            String result = null;
            switch (informationType) {
                case Constants.ALL:
                    result = weatherForecastInformation.toString();
                    break;
                case Constants.TEMPERATURE:
                    result = weatherForecastInformation.getTemperature();
                    break;
                case Constants.WIND_SPEED:
                    result = weatherForecastInformation.getWindSpeed();
                    break;
                case Constants.CONDITION:
                    result = weatherForecastInformation.getCondition();
                    break;
                case Constants.HUMIDITY:
                    result = weatherForecastInformation.getHumidity();
                    break;
                case Constants.PRESSURE:
                    result = weatherForecastInformation.getPressure();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / speed / condition / humidity / pressure)!";
            }
            printWriter.println(result);
            printWriter.flush();

            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        }
    }
}