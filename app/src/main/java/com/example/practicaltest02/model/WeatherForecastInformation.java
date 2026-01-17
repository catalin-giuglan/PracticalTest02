package com.example.practicaltest02.model;

public class WeatherForecastInformation {
    private String temperature;
    private String windSpeed;
    private String condition;
    private String pressure;
    private String humidity;

    public WeatherForecastInformation(String temperature, String windSpeed, String condition, String pressure, String humidity) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.pressure = pressure;
        this.humidity = humidity;
    }

    public String getTemperature() { return temperature; }
    public String getWindSpeed() { return windSpeed; }
    public String getCondition() { return condition; }
    public String getPressure() { return pressure; }
    public String getHumidity() { return humidity; }

    @Override
    public String toString() {
        return "Temperature: " + temperature + "\n\n" +
                "Wind Speed: " + windSpeed + "\n\n" +
                "Condition: " + condition + "\n\n" +
                "Pressure: " + pressure + "\n\n" +
                "Humidity: " + humidity;
    }
}