package me.mark.csvscrub;

public class BoundingBox {

  public static class GLCoord {
    private String type;
    private double longitude;
    private double latitude;

    public GLCoord(String type, double latitude, double longitude) {
      this.type = type;
      this.longitude = longitude;
      this.latitude = latitude;
    }

    @Override
    public String toString() {
      return type + " " + latitude + " " + longitude;
    }
  }

  private String iso;
  private GLCoord sw;
  private GLCoord ne;

  public BoundingBox(String iso, GLCoord sw, GLCoord ne) {
    this.iso = iso;
    this.sw = sw;
    this.ne = ne;
  }

  public String getIso() {
    return iso;
  }

  public GLCoord getSw() {
    return sw;
  }

  public void setSw(GLCoord sw) {
    this.sw = sw;
  }

  public GLCoord getNe() {
    return ne;
  }

  public void setNe(GLCoord ne) {
    this.ne = ne;
  }

  @Override
  public String toString() {
    return iso + " " + sw + " " + ne;
  }

}
