package com.pixelforge;

import com.pixelforge.core.ProductImage;
import com.pixelforge.core.TreatmentType;
import com.pixelforge.factory.ClassicStyleFactory;
import com.pixelforge.factory.StyleCatalog;
import com.pixelforge.factory.StyleFactory;
import com.pixelforge.pipeline.DecoratorCatalog;
import com.pixelforge.pipeline.DecoratorPipelineBuilder;
import com.pixelforge.pipeline.PipelineBuilder;
import com.pixelforge.pipeline.PipelineDirector;
import com.pixelforge.pipeline.PipelineValidator;
import com.pixelforge.pipeline.TreatmentRequest;
import com.pixelforge.prototype.PipelinePreset;
import com.pixelforge.prototype.PresetRegistry;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PatternTests {

    private static int passed = 0;

    public static void main(String[] args) {
        copyIsIndependentFromThePrototype();
        registryAlwaysReturnsFreshCopies();
        storeNameReplacesOnlyTheWatermark();
        stylesProduceDifferentBorders();
        styleDefaultsAreUsedWhenParameterIsMissing();
        explicitParameterOverridesTheStyle();
        builderCreatesTheSamePipelineAsTheDirector();
        builderCanBeReused();
        System.out.println("\nAll " + passed + " pattern tests passed.");
    }

    static void copyIsIndependentFromThePrototype() {
        PipelinePreset original = new PresetRegistry().createCopy("MARKETPLACE");
        PipelinePreset copy = original.copy().withStoreName("Other Shop");
        check(!original.getTreatments().get(2).getParameter().equals("Other Shop"), "original must not change");
        check(copy.getTreatments().get(2).getParameter().equals("Other Shop"), "copy should have the new name");
        ok("Prototype: a copy can change without touching the original");
    }

    static void registryAlwaysReturnsFreshCopies() {
        PresetRegistry registry = new PresetRegistry();
        PipelinePreset first = registry.createCopy("SOCIAL_MEDIA").withStoreName("First");
        PipelinePreset second = registry.createCopy("SOCIAL_MEDIA");
        check(!second.getTreatments().get(3).getParameter().equals("First"), "registry prototype was modified");
        check(registry.createAllCopies().size() == 3, "there should be 3 presets");
        check(first != second, "each call should return a new object");
        ok("Prototype: the registry returns a new copy every time");
    }

    static void storeNameReplacesOnlyTheWatermark() {
        PipelinePreset preset = new PresetRegistry().createCopy("SOCIAL_MEDIA").withStoreName("Pixel Shop");
        for (TreatmentRequest request : preset.getTreatments()) {
            boolean isWatermark = request.getType() == TreatmentType.WATERMARK;
            check(isWatermark == "Pixel Shop".equals(request.getParameter()), "only the watermark should use the name");
        }
        ok("Prototype: the store name only changes the watermark");
    }

    static void stylesProduceDifferentBorders() {
        BufferedImage classic = styled("CLASSIC", "BORDER").process();
        BufferedImage vibrant = styled("VIBRANT", "BORDER").process();
        check(new Color(classic.getRGB(1, 1)).equals(Color.BLACK), "classic border should be black");
        check(new Color(vibrant.getRGB(1, 1)).equals(new Color(0xFF3B30)), "vibrant border should be red");
        ok("Abstract Factory: each style factory creates its own family of decorators");
    }

    static void styleDefaultsAreUsedWhenParameterIsMissing() {
        ProductImage image = styled("FRESH", "COLOR_FILTER");
        check(image.getDescription().contains("BRIGHTEN"), "fresh style should use BRIGHTEN");
        ok("Abstract Factory: missing parameter falls back to the style default");
    }

    static void explicitParameterOverridesTheStyle() {
        ProductImage image = styled("FRESH", "COLOR_FILTER:GRAYSCALE");
        check(image.getDescription().contains("GRAYSCALE"), "explicit filter should win over the style");
        ok("Abstract Factory: an explicit parameter overrides the style default");
    }

    static void builderCreatesTheSamePipelineAsTheDirector() {
        StyleFactory style = new ClassicStyleFactory();
        PipelineBuilder builder = new DecoratorPipelineBuilder(new DecoratorCatalog(), new PipelineValidator());
        ProductImage manual = builder.reset()
                .photo(samplePhoto())
                .style(style)
                .addTreatment(TreatmentRequest.parse("REMOVE_BACKGROUND"))
                .addTreatment(TreatmentRequest.parse("RESIZE"))
                .addTreatment(TreatmentRequest.parse("WATERMARK:Pixel Shop"))
                .addTreatment(TreatmentRequest.parse("COMPRESSION"))
                .build();
        ProductImage directed = new PipelineDirector(builder).constructMarketplacePhoto(samplePhoto(), style, "Pixel Shop");
        check(manual.getDescription().equals(directed.getDescription()), "both pipelines should match");
        check(manual.getCost() == directed.getCost(), "both pipelines should cost the same");
        ok("Builder: fluent calls and the director build the same pipeline");
    }

    static void builderCanBeReused() {
        PipelineBuilder builder = new DecoratorPipelineBuilder(new DecoratorCatalog(), new PipelineValidator());
        builder.reset().photo(samplePhoto()).addTreatment(TreatmentRequest.parse("RESIZE")).build();
        ProductImage second = builder.reset().photo(samplePhoto()).addTreatment(TreatmentRequest.parse("BORDER")).build();
        check(second.getLayers().size() == 2, "reset should clear the previous treatments");
        ok("Builder: reset clears the previous state");
    }

    private static ProductImage styled(String styleId, String treatment) {
        StyleFactory style = new StyleCatalog().find(styleId);
        PipelineBuilder builder = new DecoratorPipelineBuilder(new DecoratorCatalog(), new PipelineValidator());
        return builder.reset()
                .photo(samplePhoto())
                .style(style)
                .addTreatment(TreatmentRequest.parse(treatment))
                .build();
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
