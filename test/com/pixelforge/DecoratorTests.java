package com.pixelforge;

import com.pixelforge.core.BaseImage;
import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentDecorator;
import com.pixelforge.pipeline.DecoratorCatalog;
import com.pixelforge.pipeline.DecoratorPipelineBuilder;
import com.pixelforge.pipeline.PipelineDirector;
import com.pixelforge.pipeline.PipelineValidator;
import com.pixelforge.factory.ClassicStyleFactory;
import com.pixelforge.pipeline.TreatmentRequest;
import com.pixelforge.treatments.BorderDecorator;
import com.pixelforge.treatments.ColorFilterDecorator;
import com.pixelforge.treatments.ColorFilterDecorator.FilterType;
import com.pixelforge.treatments.RemoveBackgroundDecorator;
import com.pixelforge.treatments.ResizeDecorator;
import com.pixelforge.core.TreatmentType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class DecoratorTests {
    private static int passed = 0;

    public static void main(String[] args) {
        costsAddUpThroughTheLayers();
        orderOfDecoratorsChangesTheResult();
        sameDecoratorCanBeStackedTwice();
        compressionMustBeLast();
        discountIsAddedWithFourOrMoreTreatments();
        newDecoratorWorksWithoutChangingExistingClasses();
        System.out.println("\nAll " + passed + " tests passed.");
    }

    static void costsAddUpThroughTheLayers() {
        ProductImage image = new ResizeDecorator(new RemoveBackgroundDecorator(new BaseImage(samplePhoto())));
        check(image.getCost() == 500 + 1500 + 300, "cost should be 2300 but was " + image.getCost());
        check(image.getProcessingSeconds() == 1 + 4 + 1, "time should be 6 s");
        check(image.getLayers().size() == 3, "there should be 3 layers");
        ok("Costs and times add up through the layers");
    }

    static void orderOfDecoratorsChangesTheResult() {
        BufferedImage grayThenBorder = new BorderDecorator(
                new ColorFilterDecorator(new BaseImage(samplePhoto()), FilterType.GRAYSCALE), "FF0000").process();
        BufferedImage borderThenGray = new ColorFilterDecorator(
                new BorderDecorator(new BaseImage(samplePhoto()), "FF0000"), FilterType.GRAYSCALE).process();

        Color cornerA = new Color(grayThenBorder.getRGB(1, 1));
        Color cornerB = new Color(borderThenGray.getRGB(1, 1));
        check(cornerA.getRed() == 255 && cornerA.getGreen() == 0, "border should stay red when applied last");
        check(cornerB.getRed() == cornerB.getGreen(), "border should be gray when grayscale is applied last");
        ok("The order of decorators changes the result");
    }

    static void sameDecoratorCanBeStackedTwice() {
        ProductImage image = new ColorFilterDecorator(
                new ColorFilterDecorator(new BaseImage(samplePhoto()), FilterType.BRIGHTEN), FilterType.GRAYSCALE);
        check(image.getCost() == 500 + 400 + 400, "two filters should be charged twice");
        image.process();
        ok("The same decorator can be stacked twice");
    }

    static void compressionMustBeLast() {
        List<TreatmentRequest> requests = Arrays.asList(
                TreatmentRequest.parse("COMPRESSION"), TreatmentRequest.parse("RESIZE"));
        try {
            build(requests);
            throw new AssertionError("COMPRESSION in the middle should be rejected");
        } catch (IllegalArgumentException expected) {
            ok("Business rule: COMPRESSION must be last");
        }
    }

    static void discountIsAddedWithFourOrMoreTreatments() {
        List<TreatmentRequest> requests = Arrays.asList(
                TreatmentRequest.parse("REMOVE_BACKGROUND"),
                TreatmentRequest.parse("RESIZE"),
                TreatmentRequest.parse("WATERMARK:MyStore"),
                TreatmentRequest.parse("COMPRESSION"));
        ProductImage image = build(requests);

        int fullPrice = 500 + 1500 + 300 + 600 + 250;
        check(image.getCost() == fullPrice - fullPrice / 10, "expected 10% off, got " + image.getCost());
        ok("Business rule: 10% discount with 4+ treatments");
    }

    static void newDecoratorWorksWithoutChangingExistingClasses() {
        class InvertColorsDecorator extends TreatmentDecorator {
            InvertColorsDecorator(ProductImage wrapped) {
                super(wrapped, TreatmentType.COLOR_FILTER);
            }

            @Override
            protected BufferedImage applyTo(BufferedImage image) {
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        image.setRGB(x, y, image.getRGB(x, y) ^ 0x00FFFFFF);
                    }
                }
                return image;
            }
        }

        BufferedImage result = new InvertColorsDecorator(new BaseImage(samplePhoto())).process();
        check(new Color(result.getRGB(0, 0)).equals(Color.BLACK), "white background should become black");
        ok("A new decorator works without modifying existing classes (Open/Closed)");
    }

    private static ProductImage build(List<TreatmentRequest> requests) {
        PipelineDirector director = new PipelineDirector(
                new DecoratorPipelineBuilder(new DecoratorCatalog(), new PipelineValidator()));
        return director.construct(samplePhoto(), new ClassicStyleFactory(), requests);
    }

    private static BufferedImage samplePhoto() {
        BufferedImage photo = new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 80; y++) {
            for (int x = 0; x < 100; x++) {
                photo.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        return photo;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void ok(String testName) {
        passed++;
        System.out.println("[OK] " + testName);
    }
}
