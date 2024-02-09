package net.stalpo.stalpomaparthelper;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class ImageHelper {
    public static boolean sameImage(File f1, File f2){
        return Arrays.equals(returnPixelVal(f1), returnPixelVal(f2));
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