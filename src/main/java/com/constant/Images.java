package com.constant;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Images {
    public static int basePixels = 50;
    public static BufferedImage backgroundImg;
    public static BufferedImage titleImg;
    public static BufferedImage[] images;

    static {
        try {
            backgroundImg = ImageIO.read(new File(SimplePath.IMG_PATH + "background.jpg"));
            titleImg = ImageIO.read(new File(SimplePath.IMG_PATH + "title.png"));
            images = new BufferedImage[]{
                    ImageIO.read(new File(SimplePath.IMG_PATH + "1.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "2.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "3.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "4.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "5.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "6.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "7.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "8.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "9.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "10.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "11.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "12.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "13.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "14.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "15.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "16.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "17.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "18.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "19.jpg")),
                    ImageIO.read(new File(SimplePath.IMG_PATH + "20.jpg")),
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
