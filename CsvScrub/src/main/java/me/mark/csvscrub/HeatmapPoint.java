package me.mark.csvscrub;

public class HeatmapPoint {

  private double latitude;
  private double longitude;
  private int heat;

  public HeatmapPoint(double latitude, double longitude, int heat) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.heat = heat;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public int getHeat() {
    return heat;
  }

  public void setHeat(int heat) {
    this.heat = heat;
  }
}
