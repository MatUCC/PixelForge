package com.pixelforge;

import com.pixelforge.core.BaseImage;
import com.pixelforge.core.LayerInfo;
import com.pixelforge.core.ProductImage;
import com.pixelforge.factory.StyleCatalog;
import com.pixelforge.factory.StyleFactory;
import com.pixelforge.pipeline.DecoratorCatalog;
import com.pixelforge.pipeline.DecoratorPipelineBuilder;
import com.pixelforge.pipeline.PipelineDirector;
import com.pixelforge.pipeline.PipelineValidator;
import com.pixelforge.prototype.PipelinePreset;
import com.pixelforge.prototype.PresetRegistry;
import com.pixelforge.treatments.BorderDecorator;
import com.pixelforge.treatments.ColorFilterDecorator;
import com.pixelforge.treatments.ColorFilterDecorator.FilterType;
import com.pixelforge.treatments.CompressionDecorator;
import com.pixelforge.treatments.RemoveBackgroundDecorator;
import com.pixelforge.treatments.ResizeDecorator;
import com.pixelforge.treatments.WatermarkDecorator;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedImage photo = createSamplePhoto();
        new File("output").mkdirs();
        ImageIO.write(photo, "png", new File("output/0-original.png"));

        ProductImage plain = new BaseImage(photo);
        printSummary("1) Base image only", plain);

        ProductImage forMarketplace =
                new CompressionDecorator(
                        new WatermarkDecorator(
                                new ResizeDecorator(
                                        new RemoveBackgroundDecorator(
                                                new BaseImage(photo))),
                                "MyStore"));
        printSummary("2) Marketplace-ready photo", forMarketplace);
        ImageIO.write(forMarketplace.process(), "jpg", new File("output/1-marketplace.jpg"));

        ProductImage grayThenBorder = new BorderDecorator(
                new ColorFilterDecorator(new BaseImage(photo), FilterType.GRAYSCALE), "FF0000");
        ProductImage borderThenGray = new ColorFilterDecorator(
                new BorderDecorator(new BaseImage(photo), "FF0000"), FilterType.GRAYSCALE);
        printSummary("3a) Grayscale, then red border", grayThenBorder);
        printSummary("3b) Red border, then grayscale", borderThenGray);
        ImageIO.write(grayThenBorder.process(), "png", new File("output/2-gray-then-border.png"));
        ImageIO.write(borderThenGray.process(), "png", new File("output/3-border-then-gray.png"));
        System.out.println("   -> In 3a the border stays red; in 3b the border also turns gray.");

        System.out.println("\nImages saved in the 'output' folder.");
    }

    private static void printSummary(String title, ProductImage image) {
        System.out.println("\n" + title);
        System.out.println("   Description: " + image.getDescription());
        for (LayerInfo layer : image.getLayers()) {
            System.out.printf("     - %-32s $%,6d   %d s%n", layer.getName(), layer.getCost(), layer.getSeconds());
        }
        System.out.printf("   TOTAL: $%,d COP, %d s%n", image.getCost(), image.getProcessingSeconds());
    }

    private static BufferedImage createSamplePhoto() {
        BufferedImage photo = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = photo.createGraphics();
        g.setColor(new Color(235, 235, 235));
        g.fillRect(0, 0, 800, 600);
        g.setColor(new Color(30, 90, 200));
        g.fillRoundRect(280, 160, 220, 280, 30, 30);
        g.fillOval(470, 230, 110, 120);
        g.setColor(new Color(235, 235, 235));
        g.fillOval(495, 255, 60, 70);
        g.dispose();
        return photo;
    }
}
