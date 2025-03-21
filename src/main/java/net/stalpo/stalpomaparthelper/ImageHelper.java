package net.stalpo.stalpomaparthelper;

import net.minecraft.client.MinecraftClient;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ImageHelper {
    public static int maxWrong = 3;

    public static ArrayList<ImageData> images = new ArrayList<>();

    public static boolean sameImage(File f1, File f2){
        return sameImage(returnPixelVal(f1), returnPixelVal(f2));
    }

    public static boolean sameImage(byte[] pixels1, byte[] pixels2){
        int wrong = 0;
        for(int i = 0; i < 16384; i++){
            if(pixels1[i] != pixels2[i] || pixels1[i + 1] != pixels2[i + 1] || pixels1[i + 2] != pixels2[i + 2] || pixels1[i + 3] != pixels2[i + 3]){
                wrong++;
                if(wrong > maxWrong){
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isDuplicate(File f){
        byte[] pixels1 = returnPixelVal(f);
        for(int m = 0; m < images.size(); m++){
            if(sameImage(pixels1, images.get(m).getPixels())){
                return true;
            }
        }
        return false;
    }

    private static byte[] returnPixelVal(File in) {

        BufferedImage img = null;
        File f = null;
        byte[] pixels = null;

        try {
            f = in;
            img = ImageIO.read(f);
            pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        } catch (IOException e) {
            System.out.println(e);
        }

        return pixels;

    }

    public static void initializeImages(){
        clearImages();
        File checkdir = new File(StalpoMapartHelper.modFolder, "maparts");
        for(File f : checkdir.listFiles()){
            images.add(new ImageData(f.getName(), returnPixelVal(f)));
        }
    }

    public static void addImage(File in){
        images.add(new ImageData(in.getName(), returnPixelVal(in)));
    }

    public static void clearImages(){
        images = new ArrayList<>();
    }
}

