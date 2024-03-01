package net.stalpo.stalpomaparthelper;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageHelper {
    public static int maxWrong = 3;

    public static boolean sameImage(File f1, File f2){
        byte[] pixels1 = returnPixelVal(f1), pixels2 = returnPixelVal(f2);
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

    public static byte[] returnPixelVal(File in) {

        BufferedImage img = null;
        File f = null;
        byte[] pixels = null;
        // read image
        try {
            f = in;
            img = ImageIO.read(f);
            pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        } catch (IOException e) {
            System.out.println(e);
        }

        return pixels;

    }

}