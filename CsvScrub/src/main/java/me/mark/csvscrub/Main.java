package me.mark.csvscrub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import me.mark.csvscrub.BoundingBox.GLCoord;

public class Main {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static Map<String, Country> countryMap = new HashMap<>();
  private static Map<String, String> isoToName = new HashMap<>();
  private static Map<String, BoundingBox> isoToBox = new HashMap<>();
  private static Set<String> validIso = new HashSet<>();

  public static void main(String[] args) throws IOException {
    //Convert txt to valid json
    FileReader boundingBoxReader = new FileReader("bounding_boxes.txt");
    BufferedReader boundingBoxBufferedReader = new BufferedReader(boundingBoxReader);
    Stream<String> dataStream = boundingBoxBufferedReader.lines();
    List<String> scrubbed = new ArrayList<>();
    char quoteChar = Character.valueOf('"');
    dataStream.forEach(data -> {
      scrubbed.add(data.replace('\'', quoteChar));
    });

    //Write valid json to json file
    File boundingBox = new File("bounding_boxes.json");
    if(!boundingBox.exists()) {
      boundingBox.createNewFile();
      FileWriter writer = new FileWriter(boundingBox);
      writer.write(scrubbed.get(0));
      writer.flush();
      writer.close();
    }

    //Read the country data into a mapping
    File fullData = new File("full_data.csv");
    if (!fullData.exists()) {
      System.exit(1);
    }
    FileReader reader = new FileReader(fullData);
    BufferedReader bufferedReader = new BufferedReader(reader);
    dataStream = bufferedReader.lines().skip(1);
    dataStream.forEach(data -> {
      String[] split = data.split(",");
      String key = split[1];
      countryMap.putIfAbsent(key, new Country(key));
      try {
          //total cases at index 4
          Date date = DATE_FORMAT.parse(split[0]);
          Integer cases = Integer.parseInt(split[4]);
          countryMap.get(key).getDayToCases().put(date, cases);
        } catch (NumberFormatException | ParseException e) {
      }
    });

    File isoFile = new File("iso_codes.csv");
    if (!isoFile.exists())
      System.exit(1);
    FileReader isoFileReader = new FileReader(isoFile);
    BufferedReader isoReader = new BufferedReader(isoFileReader);
    Stream<String> isoDataStream = isoReader.lines().skip(1);
    isoDataStream.forEach(data -> {
      String[] split = data.split(",");
      String countryName = split[0];
      if (!countryMap.containsKey(countryName))
        return;
      String iso2 = split[1].split("/")[1].trim();
      validIso.add(iso2);
      isoToName.put(countryName, iso2);
    });

    //Read written json and parse it for bounding boxes of countries
    JsonReader jsonReader = new JsonReader(new BufferedReader(new FileReader(boundingBox)));
    jsonReader.beginObject();
    while(jsonReader.hasNext()) {
      String name = jsonReader.nextName();
      jsonReader.beginObject();
      BoundingBox box = readGLCoord(name, jsonReader);
      if (validIso.contains(box.getIso()))
        isoToBox.put(box.getIso().trim(), box);
      jsonReader.endObject();
    }
    jsonReader.endObject();

    System.out.println(isoToName.size() + " " + isoToBox.size());

    for (Map.Entry<String, Country> entry : countryMap.entrySet()) {
      String iso = isoToName.get(entry.getKey());
      if (isoToBox.containsKey(iso)) {
        BoundingBox box = isoToBox.get(iso);
        entry.getValue().setCode(box.getIso());
        entry.getValue().setBoundingBox(box);
      }
    }

    File countryData = new File("country_data.csv");
    FileReader countryDataReader = new FileReader(countryData);
    BufferedReader countryDataBuffer = new BufferedReader(countryDataReader);
    Stream<String> countryDataStream = countryDataBuffer.lines().skip(1);

    countryDataStream.forEach(data ->{
      String[] split = data.split(",");
      String key = split[3];
      if (countryMap.containsKey(key)) {
        double latitude = Double.parseDouble(split[1]);
        double longitude = Double.parseDouble(split[2]);
        countryMap.get(key).setLatitude(latitude);
        countryMap.get(key).setLongitude(longitude);
      }
    });

    //Iterator over values for testing to make sure scrubbing was correct
    List<Country> validCounties = new ArrayList<>();
    for (Country country : countryMap.values()) {
      System.out.println(country.getName());
      for (Map.Entry<Date, Integer> entry : country.getDayToCases().entrySet()) {
        System.out.println(entry.getKey().toString() + " " + entry.getValue());
      }
      System.out.println(country.getBoundingBox());
      if (country.getBoundingBox() != null) {
        validCounties.add(country);
      }
    }

    //Write scrubbed data to a consolidated json file (heatmap.json)
    File jsonOutput = new File("heatmap.json");
    if (jsonOutput.exists())
      jsonOutput.delete();
    jsonOutput.createNewFile();
    FileWriter writer = new FileWriter(jsonOutput);
    GSON.toJson(validCounties, writer);
    writer.flush();
    writer.close();

    File confirmed = new File("confirmed.csv");
    FileReader confirmedReader = new FileReader(confirmed);
    BufferedReader bConfirmedReader = new BufferedReader(confirmedReader);
    Stream<String> confirmedStream = bConfirmedReader.lines().skip(1);
    List<HeatmapPoint> points = new ArrayList<>();
    confirmedStream.forEach(data -> {
      String[] split = data.split(",");
      int latitudeIndex = 2;
      search: while(true){
        try {
          Double.parseDouble(split[latitudeIndex]);
          break search;
        } catch (NumberFormatException e) {
          latitudeIndex++;
        }
      }
      double latitude = Double.parseDouble(split[latitudeIndex]);
      double longitude = Double.parseDouble(split[latitudeIndex + 1]);
      int heat = Integer.parseInt(split[split.length - 1]);
      System.out.println(latitude + " " + longitude + " " + heat);
      points.add(new HeatmapPoint(latitude, longitude, heat));
    });
    System.out.println(points.size());
    FileWriter heatWriter = new FileWriter(new File("heatpoints.json"));
    GSON.toJson(points, heatWriter);
    heatWriter.flush();
    heatWriter.close();
  }

  private static BoundingBox readGLCoord(String type, JsonReader jsonReader) throws IOException {
    String sw = jsonReader.nextName();
    jsonReader.beginObject();
    jsonReader.nextName();
    double la1 = jsonReader.nextDouble();
    jsonReader.nextName();
    double ln1 = jsonReader.nextDouble();
    jsonReader.endObject();
    String ne = jsonReader.nextName();
    jsonReader.beginObject();
    jsonReader.nextName();
    double la2 = jsonReader.nextDouble();
    jsonReader.nextName();
    double ln2 = jsonReader.nextDouble();
    jsonReader.endObject();
    return new BoundingBox(type, new GLCoord(sw, la1, ln1), new GLCoord(ne, la2, ln2));
  }


}
