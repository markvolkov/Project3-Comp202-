package me.mark.csvscrub;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Country {

  private String code;
  private String name;
  private BoundingBox boundingBox;
  private double longitude;
  private double latitude;
  private TreeMap<Date, Integer> dayToCases;

  public Country(String name) {
    this.name = name;
    this.dayToCases = new TreeMap<>();
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<Date, Integer> getDayToCases() {
    return dayToCases;
  }

  public void setDayToCases(TreeMap<Date, Integer> dayToCases) {
    this.dayToCases = dayToCases;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setBoundingBox(BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  @Override
  public String toString() {
    return this.code + " " + this.name + " \n" + this.boundingBox;
  }
}
