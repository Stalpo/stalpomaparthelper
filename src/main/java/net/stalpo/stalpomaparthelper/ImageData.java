package net.stalpo.stalpomaparthelper;

public class ImageData {
    private String name;
    private byte[] pixels;

    ImageData(String n, byte[] ps){
        name = n;
        pixels = ps;
    }

    public String getName(){
        return name;
    }

    public byte[] getPixels(){
        return pixels;
    }

    public byte getPixel(int p){
        return pixels[p];
    }
}
