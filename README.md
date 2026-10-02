# PixelForge

PixelForge edits product photos for online stores. The customer uploads a photo, chooses treatments (remove background, resize, color filter, watermark, border, compression) in the order they want, and receives the final image with its price and processing time.

Live deployment: https://pixelforge-jd63.onrender.com

## New feature: ready-made packages and visual styles

The frontend now has two new controls in step 2:

- **Visual style** (Classic, Vibrant, Fresh): changes the default filter, the default border color and the watermark color.
- **Ready-made packages** (Marketplace ready, Social media post, Clean catalog): one click fills the pipeline with a group of treatments. The **Store name** field is written into the watermark of the package.

The customer can still edit, reorder or remove any treatment after choosing a package.

New endpoints used by the frontend:

| Endpoint | Purpose |
|---|---|
| `GET /api/styles` | List of visual styles |
| `GET /api/presets` | List of ready-made packages |
| `GET /api/presets/apply?id=...&store=...` | A copy of a package with the store name applied |
| `POST /api/process?treatments=...&style=...` | Now also receives the chosen style |

## How the patterns were applied

### Prototype

- `Prototype<T>` (`prototype/Prototype.java`) is the interface with one method, `copy()`.
- `PipelinePreset` implements `Prototype<PipelinePreset>`. It holds an id, a name, a description and a list of `TreatmentRequest`. Its `copy()` creates a new preset and copies every `TreatmentRequest` with `TreatmentRequest.copy()`, so the copy shares no mutable state with the original.
- `PresetRegistry` stores one prototype per package (`MARKETPLACE`, `SOCIAL_MEDIA`, `CLEAN_CATALOG`). It never gives the stored object to anyone: `createCopy(id)` and `createAllCopies()` always return copies.
- `ImageProcessingService.createPreset(...)` takes a copy and calls `withStoreName(...)` on it. Only the copy changes, so the next customer still gets the original package with the default store name.

### Abstract Factory

- `StyleFactory` (`factory/StyleFactory.java`) is the abstract factory. It declares `createFilter`, `createBorder` and `createWatermark`, which return `TreatmentDecorator` objects (the abstract product).
- `AbstractStyleFactory` implements those three methods once. It creates `ColorFilterDecorator`, `BorderDecorator` and `WatermarkDecorator` and gets the values that change per style from three small abstract methods: `defaultFilter()`, `defaultBorderColor()` and `watermarkColor()`.
- The concrete factories are `ClassicStyleFactory` (grayscale, black border, white text), `VibrantStyleFactory` (high contrast, red border, yellow text) and `FreshStyleFactory` (brighten, green border, mint text). Each one creates a whole family of decorators that look good together.
- `StyleCatalog` finds a factory by id.
- `DecoratorCatalog` calls the chosen `StyleFactory` for `COLOR_FILTER`, `BORDER` and `WATERMARK`. If the request has no parameter, the style default is used. If it has a parameter, that value wins.

### Builder

- `PipelineBuilder` (`pipeline/PipelineBuilder.java`) is the builder interface: `reset()`, `photo(...)`, `style(...)`, `addTreatment(...)` and `build()`. Every method except `build()` returns the builder, so the calls are chained.
- `DecoratorPipelineBuilder` is the concrete builder. It stores the photo, the style and the list of treatments. When `build()` is called it asks `PipelineValidator` to check the rules, starts with a `BaseImage`, wraps it with one decorator per treatment (created by `DecoratorCatalog`) and adds a `DiscountDecorator` when there are 4 or more treatments. The product is a `ProductImage`.
- `PipelineDirector` is the director. `construct(...)` builds a pipeline from a list of requests, and `constructMarketplacePhoto(...)` builds a fixed recipe (remove background, resize, watermark, compression).
- `ImageProcessingService` creates a new builder for every request, so requests running at the same time do not share state.

### Decorator

- `ProductImage` is the component interface: `process()`, `getCost()`, `getProcessingSeconds()`, `getDescription()` and `getLayers()`.
- `BaseImage` is the concrete component. It is the original photo, always at the center of the chain.
- `TreatmentDecorator` is the abstract decorator. It implements `ProductImage` and holds another `ProductImage` in the `wrapped` field. Its `process()` first calls `wrapped.process()` and then `applyTo(...)`. Its cost, time, description and layers add its own values to the ones from `wrapped`.
- The concrete decorators (`treatments/` package) are `RemoveBackgroundDecorator`, `ResizeDecorator`, `ColorFilterDecorator`, `WatermarkDecorator`, `BorderDecorator`, `CompressionDecorator` and `DiscountDecorator`. Each one only implements `applyTo(...)` (and overrides its own cost or name when needed).
- `DiscountDecorator` does not change the image. It only overrides `getOwnCost()` to return a negative value.
- The builder creates the chain in the same order the customer chose, because the order of the wrappers is the order of the treatments.

## Project structure

```
src/com/pixelforge/
├── Main.java                         Console demo
├── core/                             ProductImage, BaseImage, TreatmentDecorator, TreatmentType, LayerInfo
├── treatments/                       Concrete decorators
├── factory/                          StyleFactory, AbstractStyleFactory, Classic/Vibrant/Fresh factories, StyleCatalog
├── prototype/                        Prototype, PipelinePreset, PresetRegistry
├── pipeline/                         PipelineBuilder, DecoratorPipelineBuilder, PipelineDirector,
│                                     PipelineValidator, DecoratorCatalog, DecoratorCreator, TreatmentRequest
├── orders/                           Order, OrderHistory
├── service/                          ImageProcessingService
└── api/                              ApiServer, Json
test/com/pixelforge/                  DecoratorTests, PatternTests
frontend/                             index.html, app.js, styles.css, config.js
```

## Run it

```
javac -d out $(find src test -name "*.java")
java -cp out com.pixelforge.DecoratorTests
java -cp out com.pixelforge.PatternTests
java -cp out com.pixelforge.api.ApiServer
```

Then open http://localhost:8080.

## Developers

- Juan Esteban Cuaran Santander
- Nicolas Mora
