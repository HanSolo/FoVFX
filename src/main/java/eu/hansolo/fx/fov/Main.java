/*
 * Copyright (c) 2020 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.fov;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * User: hansolo
 * Date: 13.03.20
 * Time: 11:59
 */
public class Main extends Application {
    private static final Dimension2D              SIZE               = new Dimension2D(1000, 700);
    private static final String                   OPEN_STREET_MAP    = "osm.html";
    private static       Location                 currentLocation    = new Location(51.91178, 7.63379, 57.0, "currentLocation");
    private static       Location                 currentMapLocation = new Location(0, 0, 0, "currentMapLocation");
    private static final Dimension2D              ELEVATION_BOUNDS   = new Dimension2D(500, 300);
    private final        HttpClient               httpClient         = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private              Stage                    stage;
    private              ObservableList<Location> elevationData;
    private              Popup                    popup;
    private              Canvas                   elevationCanvas;
    private              GraphicsContext          elevationCtx;
    private              ToggleButton             cameraButton;
    private              ToggleButton             motifButton;
    private              ToggleButton             elevationButton;
    private              ComboBox<Lens>           lenses;
    private              Label                    focalLengthLabel;
    private              Slider                   focalLength;
    private              VBox                     focalLengthBox;
    private              Label                    apertureLabel;
    private              Slider                   aperture;
    private              VBox                     apertureBox;
    private              RadioButton              portraitButton;
    private              RadioButton              landscapeButton;
    private              VBox                     orientationBox;
    private              WebView                  webView;
    private              WebEngine                webEngine;
    private              Canvas                   canvas;
    private              GraphicsContext          ctx;
    private              Location                 cameraPosition;
    private              Location                 motifPosition;
    private              BooleanProperty          readyToGo;
    private              BooleanProperty          setCameraPosition;
    private              BooleanProperty          cameraMoving;
    private              BooleanProperty          setMotifPosition;
    private              BooleanProperty          motifMoving;
    private              Triangle                 triangle;
    private              Trapezoid                trapezoid;
    private              ObjectProperty<Data>     data;


    @Override public void init() {
        elevationData = FXCollections.observableArrayList();

        ToggleGroup buttonToggleGroup = new ToggleGroup();

        Region cameraRegion = new Region();
        cameraRegion.getStyleClass().add("camera");

        cameraButton = new ToggleButton();
        cameraButton.setTooltip(new Tooltip("Place camera"));
        cameraButton.setGraphic(cameraRegion);
        cameraButton.setToggleGroup(buttonToggleGroup);

        Region motifRegion = new Region();
        motifRegion.getStyleClass().add("motif");

        motifButton  = new ToggleButton();
        motifButton.setTooltip(new Tooltip("Place motif"));
        motifButton.setGraphic(motifRegion);
        motifButton.setToggleGroup(buttonToggleGroup);

        Region elevationRegion = new Region();
        elevationRegion.getStyleClass().add("elevation");

        elevationButton = new ToggleButton();
        elevationButton.setTooltip(new Tooltip("Show elevation"));
        elevationButton.setGraphic(elevationRegion);
        elevationButton.setVisible(false);

        lenses = new ComboBox<>();
        lenses.setCellFactory(new Callback<>() {
            @Override public ListCell<Lens> call(final ListView<Lens> param) {
                final ListCell<Lens> cell = new ListCell<>() {
                    @Override public void updateItem(Lens lens, boolean empty) {
                        super.updateItem(lens, empty);
                        if (lens == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(lens.getName());
                        }
                    }
                };
                return cell;
            }
        });
        lenses.setConverter(new StringConverter<>() {
            @Override public String toString(final Lens lens) { return null == lens ? null : lens.getName(); }
            @Override public Lens fromString(final String string) { return null; }
        });
        lenses.getItems().add(Lenses.IRIX_11);
        lenses.getItems().add(Lenses.SIGMA_14);
        lenses.getItems().add(Lenses.TAMRON_SP_15_30);
        lenses.getItems().add(Lenses.TAMRON_SP_24_70);
        lenses.getItems().add(Lenses.NIKON_24_70);
        lenses.getItems().add(Lenses.TAMRON_SP_35);
        lenses.getItems().add(Lenses.TOKINA_50);
        lenses.getItems().add(Lenses.NIKON_85);
        lenses.getItems().add(Lenses.TAMRON_SP_90_MACRO);
        lenses.getItems().add(Lenses.SIGMA_105);
        lenses.getItems().add(Lenses.NIKON_70_200);
        lenses.getItems().add(Lenses.NIKON_200_500);
        lenses.getItems().add(Lenses.MAK_1000);

        lenses.getSelectionModel().select(Lenses.IRIX_11);

        focalLengthLabel = new Label(Lenses.IRIX_11.getMinFocalLength() + " mm");

        focalLength  = new Slider(Lenses.IRIX_11.getMinFocalLength(), Lenses.IRIX_11.getMaxFocalLength(), 1);
        focalLength.setMajorTickUnit(1);
        focalLength.setMinorTickCount(1);
        focalLength.setSnapToTicks(true);
        focalLength.setValue(Lenses.IRIX_11.getMinFocalLength());

        focalLengthBox = new VBox(5, focalLengthLabel, focalLength);
        focalLengthBox.setAlignment(Pos.CENTER);

        apertureLabel = new Label("f " + Lenses.IRIX_11.getMinAperture());

        aperture     = new Slider(Lenses.IRIX_11.getMinAperture(), Lenses.IRIX_11.getMaxAperture(), 0.1);
        aperture.setMajorTickUnit(0.1);
        aperture.setMinorTickCount(1);
        aperture.setSnapToTicks(true);
        aperture.setValue(Lenses.IRIX_11.getMinAperture());

        apertureBox = new VBox(5, apertureLabel, aperture);
        apertureBox.setAlignment(Pos.CENTER);

        ToggleGroup orientationGroup = new ToggleGroup();

        portraitButton  = new RadioButton("Portrait");
        portraitButton.setToggleGroup(orientationGroup);

        landscapeButton = new RadioButton("Landscape");
        landscapeButton.setSelected(true);
        landscapeButton.setToggleGroup(orientationGroup);

        orientationBox = new VBox(5, portraitButton, landscapeButton);

        cameraPosition    = new Location(0, 0, 0, "camera");
        motifPosition     = new Location(0, 0, 0, "motif");

        readyToGo         = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "readyToGo"; }
        };
        setCameraPosition = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "setCameraPosition"; }
        };
        cameraMoving      = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "cameraMoving"; }
        };
        setMotifPosition  = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "setMotifPosition"; }
        };
        motifMoving       = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "motifMoving"; }
        };

        triangle          = new Triangle();
        trapezoid         = new Trapezoid();

        data              = new ObjectPropertyBase<>() {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return Main.this; }
            @Override public String getName() { return "data"; }
        };

        canvas            = new Canvas(SIZE.getWidth(), SIZE.getHeight());
        canvas.setMouseTransparent(true);
        ctx               = canvas.getGraphicsContext2D();

        registerListeners();
    }

    private void initOnFXApplicationThread() {
        webView   = new WebView();
        webView.setPrefSize(SIZE.getWidth(), SIZE.getHeight());
        webEngine = webView.getEngine();
        URL maps = Main.class.getResource(OPEN_STREET_MAP);
        webEngine.load(maps.toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener((ov, o, n) -> {
            if (Worker.State.SUCCEEDED == n) {
                JSObject jsObject = (JSObject) webEngine.executeScript("window");
                jsObject.setMember("java", currentMapLocation);

                // Pan map to current location
                StringBuilder command = new StringBuilder();
                command.append("window.lat=").append(currentLocation.getLatitude()).append(";")
                       .append("window.lon=").append(currentLocation.getLongitude()).append(";")
                       .append("document.panTo(window.lat, window.lon);");
                Platform.runLater(() -> webEngine.executeScript(command.toString()));

                webEngine.setOnStatusChanged(webEvent -> handleWebEvent(webEvent.getData()));

                readyToGo.set(true);
            } else {
                readyToGo.set(false);
            }
        });

        elevationCanvas = new Canvas(ELEVATION_BOUNDS.getWidth(), ELEVATION_BOUNDS.getHeight());
        elevationCtx    = elevationCanvas.getGraphicsContext2D();
        popup = new Popup();
        popup.getContent().add(elevationCanvas);
        popup.setOnShowing(e -> drawElevation());
    }

    private void registerListeners() {
        elevationData.addListener((ListChangeListener<Location>) c -> {
            elevationButton.setVisible(!elevationData.isEmpty());
            drawElevation();
        });

        readyToGo.addListener((o, ov, nv) -> {
            if (nv) {

            }
        });

        cameraButton.selectedProperty().addListener((o, ov, nv) -> setCameraPosition.set(nv.booleanValue()));

        motifButton.selectedProperty().addListener((o, ov, nv) -> setMotifPosition.set(nv.booleanValue()));

        elevationButton.selectedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                double x = stage.getScene().getWindow().getX();
                double y = stage.getScene().getWindow().getY();
                double w = stage.getScene().getWindow().getWidth();
                double h = stage.getScene().getWindow().getHeight();
                popup.show(stage, x + (w - ELEVATION_BOUNDS.getWidth()) * 0.5, y + (h - ELEVATION_BOUNDS.getHeight()) * 0.75);
            } else {
                popup.hide();
            }
        });

        lenses.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            focalLength.setMin(nv.getMinFocalLength());
            focalLength.setMax(nv.getMaxFocalLength());
            focalLength.setValue(nv.getMinFocalLength());
            aperture.setMin(nv.getMinAperture());
            aperture.setMax(nv.getMaxAperture());
            aperture.setValue(nv.getMinAperture());
            calculateTriangle();
        });

        focalLength.valueChangingProperty().addListener((o, ov, nv) -> {
            if (!nv) { calculateTriangle(); }
        });
        focalLength.valueProperty().addListener((o, ov, nv) -> {
            focalLengthLabel.setText(String.format(Locale.US, "%.0f mm", nv));
            focalLength.setValue(nv.intValue());
        });

        aperture.valueChangingProperty().addListener((o, ov, nv) -> {
            if (!nv) { calculateTriangle(); }
        });
        aperture.valueProperty().addListener((o, ov, nv) -> {
            apertureLabel.setText(String.format(Locale.US, "f %.1f", nv));
        });

        portraitButton.selectedProperty().addListener(o -> calculateTriangle());
        landscapeButton.selectedProperty().addListener(o -> calculateTriangle());

        data.addListener((o, ov, nv) -> {
            draw();
            //            a
            //        __________
            //       /    |     \
            //   b  /     |h     \  b
            //     /      |       \
            //    /_______|________\
            //            c
            //
            // h = Math.sqrt((b * b) - ((c - a) / 2) * ((c - a) / 2))
            // b = Math.sqrt(((c - a) / 2) * ((c - a) / 2) + (h * h))
        });

        cameraPosition.setOnLocationChanged(e -> copyToClipboard(e));
        motifPosition.setOnLocationChanged(e -> copyToClipboard(e));
    }


    private void handleWebEvent(final String webData) {
        //System.out.println("WebData: " + webData);
        if (null == webData || webData.isEmpty() || !webData.contains(",")) {
            if (webData.equals("moveCameraStart")) {
                cameraMoving.set(true);
            } else if (webData.equals("moveCameraStop")) {
                cameraMoving.set(false);
                calculateTriangle();
            } else if (webData.equals("moveMotifStart")) {
                motifMoving.set(true);
            } else if (webData.equals("moveMotifStop")) {
                motifMoving.set(false);
                calculateTriangle();
            } else {
                return;
            }
        } else {
            double lat = Double.parseDouble(webData.split(",")[0]);
            double lng = Double.parseDouble(webData.split(",")[1]);
            if (setCameraPosition.get()) {
                cameraPosition.set(lat, lng);
                setCamera();
                cameraButton.setSelected(false);
            } else if (setMotifPosition.get()) {
                motifPosition.set(lat, lng);
                setMotif();
                motifButton.setSelected(false);
            } else if (cameraMoving.get()) {
                cameraPosition.set(lat, lng);
            } else if (motifMoving.get()) {
                motifPosition.set(lat, lng);
            }
        }
    }

    private void calculateTriangle() {
        int    fl = Double.compare(focalLength.getMin(), focalLength.getMax()) == 0 ? (int) focalLength.getMin() : focalLength.valueProperty().intValue();
        double ap = Double.compare(aperture.getMin(), aperture.getMax()) == 0 ? aperture.getMin() : aperture.getValue();
        double d  = cameraPosition.getDistanceInMeterTo(motifPosition);
        if (d < 0.01 || d > 9999) { return; }
        data.set(Helper.calc(cameraPosition, motifPosition, fl, ap, SensorFormat.FULL_FORMAT, landscapeButton.isSelected() ? Orientation.LANDSCAPE : Orientation.PORTRAIT));
        setTriangle();
        setTrapezoid();
        getElevation();
    }

    private void setCamera() {
        StringBuilder command = new StringBuilder();
        command.append("window.poiLat=").append(cameraPosition.getLatitude()).append(";")
               .append("window.poiLon=").append(cameraPosition.getLongitude()).append(";")
               .append("document.addCameraMarker(window.poiLat, window.poiLon);");
        Platform.runLater(() -> webEngine.executeScript(command.toString()));
        calculateTriangle();
    }

    private void setMotif() {
        StringBuilder command = new StringBuilder();
        command.append("window.poiLat=").append(motifPosition.getLatitude()).append(";")
               .append("window.poiLon=").append(motifPosition.getLongitude()).append(";")
               .append("document.addMotifMarker(window.poiLat, window.poiLon);");
        Platform.runLater(() -> webEngine.executeScript(command.toString()));
        calculateTriangle();
    }

    private void setTriangle() {
        double d  = cameraPosition.getDistanceInMeterTo(motifPosition);
        if (d < 0.01 || d > 9999) { return; }
        int    fl = Double.compare(focalLength.getMin(), focalLength.getMax()) == 0 ? (int) focalLength.getMin() : focalLength.valueProperty().intValue();
        double ap = Double.compare(aperture.getMin(), aperture.getMax()) == 0 ? aperture.getMin() : aperture.getValue();
        Helper.updateTriangle(cameraPosition, motifPosition, fl, ap, SensorFormat.FULL_FORMAT, landscapeButton.isSelected() ? Orientation.LANDSCAPE : Orientation.PORTRAIT, triangle);

        StringJoiner pointsJoiner = new StringJoiner(",");
        triangle.getPointsAsList().forEach(coord -> pointsJoiner.add(Double.toString(coord)));

        double angle = Math.toRadians(Helper.calculateBearing(cameraPosition, motifPosition));

        Platform.runLater(() -> webEngine.executeScript("document.addTriangle(\"" + pointsJoiner.toString() + "\",\"" + angle+ "\");"));
    }

    private void setTrapezoid() {
        double d  = cameraPosition.getDistanceInMeterTo(motifPosition);
        if (d < 0.01 || d > 9999) { return; }
        int    fl = Double.compare(focalLength.getMin(), focalLength.getMax()) == 0 ? (int) focalLength.getMin() : focalLength.valueProperty().intValue();
        double ap = Double.compare(aperture.getMin(), aperture.getMax()) == 0 ? aperture.getMin() : aperture.getValue();
        Helper.updateTrapezoid(cameraPosition, motifPosition, fl, ap, SensorFormat.FULL_FORMAT, landscapeButton.isSelected() ? Orientation.LANDSCAPE : Orientation.PORTRAIT, trapezoid);

        StringJoiner pointsJoiner = new StringJoiner(",");
        pointsJoiner.add(Double.toString(cameraPosition.getLatitude())).add(Double.toString(cameraPosition.getLongitude()));
        trapezoid.getPointsAsList().forEach(coord -> pointsJoiner.add(Double.toString(coord)));

        double angle = Math.toRadians(Helper.calculateBearing(cameraPosition, motifPosition));

        Platform.runLater(() -> webEngine.executeScript("document.addTrapezoid(\"" + pointsJoiner.toString() + "\",\"" + angle+ "\");"));
    }

    private void getElevation() {
        String uri = new StringBuilder("https://api.elevationapi.com/api/Elevation/line/").append(String.format(Locale.US, "%.7f", cameraPosition.getLatitude()))
                                                                                          .append(",")
                                                                                          .append(String.format(Locale.US, "%.7f", cameraPosition.getLongitude()))
                                                                                          .append("/")
                                                                                          .append(String.format(Locale.US, "%.7f", motifPosition.getLatitude()))
                                                                                          .append(",")
                                                                                          .append(String.format(Locale.US, "%.7f", motifPosition.getLongitude()))
                                                                                          .append("?dataSet=SRTM_GL3&reduceResolution=0")
                                                                                          .toString();

        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         //.setHeader("User-Agent", "Java 11 HttpClient Bot")
                                         .build();

        CompletableFuture<HttpResponse<String>> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        try {
            elevationData.clear();

            String jsonResult = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

            Object     obj       = JSONValue.parse(jsonResult);
            JSONObject jsonObj   = (JSONObject) obj;
            JSONArray  geoPoints = (JSONArray) jsonObj.get("geoPoints");

            geoPoints.forEach(gp -> {
                JSONObject geoPoint = (JSONObject) gp;
                elevationData.add(new Location(Double.parseDouble(geoPoint.get("latitude").toString()),
                                               Double.parseDouble(geoPoint.get("longitude").toString()),
                                               Double.parseDouble(geoPoint.get("elevation").toString())));
            });
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Error retrieving elevation data: " + e.getMessage());
        }
    }

    private void copyToClipboard(final LocationEvent evt) {
        final Clipboard        clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content   = new ClipboardContent();
        content.putString(String.format(Locale.US, "%.5f,%.5f", evt.getLocation().getLatitude(), evt.getLocation().getLongitude()));
        clipboard.setContent(content);
    }


    private void draw() {
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFill(Color.rgb(0, 0, 0, 0.7));
        ctx.fillRect(0, 0, 250, 220);
        ctx.setFont(Font.font("Courier", 13));
        ctx.setFill(Color.WHITE);
        ctx.fillText(data.get().toString(), 10, 20);
    }

    private void drawElevation() {
        elevationCtx.clearRect(0, 0, ELEVATION_BOUNDS.getWidth(), ELEVATION_BOUNDS.getHeight());

        double      minElevation    = elevationData.stream().mapToDouble(location -> location.getElevation()).min().orElse(0);
        double      maxElevation    = elevationData.stream().mapToDouble(location -> location.getElevation()).max().orElse(0);
        double      elevationDelta  = Math.abs(maxElevation - minElevation);
        double      offsetTop       = 20;
        double      offsetRight     = 20;
        double      offsetBottom    = 20;
        double      offsetLeft      = 20;
        double      elevationStep   = (ELEVATION_BOUNDS.getHeight() - offsetTop - offsetBottom) / elevationDelta;
        double      distanceStep    = (ELEVATION_BOUNDS.getWidth() - offsetLeft - offsetRight) / data.get().distance;

        if (elevationData.isEmpty() || elevationDelta == 0) { return; }

        elevationCtx.setFill(Color.rgb(0, 0, 0, 0.7));
        elevationCtx.fillRoundRect(0, 0, ELEVATION_BOUNDS.getWidth(), ELEVATION_BOUNDS.getHeight(), 10, 10);
        elevationCtx.setStroke(Color.WHITE);
        elevationCtx.setLineWidth(1);
        elevationCtx.setFill(Color.WHITE);
        elevationCtx.setTextAlign(TextAlignment.LEFT);
        elevationCtx.setTextBaseline(VPos.TOP);
        elevationCtx.setFont(Font.font("Courier", 13));
        elevationCtx.fillText("Elevation", 10, 10);
        elevationCtx.setTextAlign(TextAlignment.CENTER);
        elevationCtx.setTextBaseline(VPos.CENTER);
        elevationCtx.setFont(Font.font("Courier", 10));
        boolean toggle = true;
        elevationCtx.beginPath();
        elevationCtx.moveTo(offsetLeft, ELEVATION_BOUNDS.getHeight() - offsetBottom);
        for (Location loc : elevationData) {
            double x = offsetLeft + cameraPosition.getDistanceInMeterTo(loc) * distanceStep;
            double y = ELEVATION_BOUNDS.getHeight() - offsetBottom - (loc.getElevation() - minElevation) * elevationStep;
            elevationCtx.save();
            elevationCtx.setLineWidth(0.5);
            elevationCtx.strokeLine(x, y, x, ELEVATION_BOUNDS.getHeight() - offsetBottom);
            elevationCtx.restore();
            if (toggle) {
                elevationCtx.fillText(String.format(Locale.US, "%.1fm", loc.getElevation()), x, y - 10);
                elevationCtx.fillText(String.format(Locale.US, "%.0fm", cameraPosition.getDistanceInMeterTo(loc)), x, ELEVATION_BOUNDS.getHeight() - offsetBottom + 10);
            }
            toggle ^= true;
            elevationCtx.lineTo(x, y);
        };
        elevationCtx.lineTo(ELEVATION_BOUNDS.getWidth() - offsetRight, ELEVATION_BOUNDS.getHeight() - offsetBottom);
        elevationCtx.closePath();
        elevationCtx.stroke();
    }


    @Override public void start(Stage stage) {
        this.stage = stage;

        initOnFXApplicationThread();

        VBox buttonPane = new VBox(10, cameraButton, motifButton, elevationButton);
        buttonPane.setPrefWidth(32);
        buttonPane.setAlignment(Pos.TOP_CENTER);
        buttonPane.setPadding(new Insets(10));
        buttonPane.getStyleClass().add("overlay");

        HBox controlsPane = new HBox(10, lenses, focalLengthBox, apertureBox, orientationBox);
        controlsPane.setPrefHeight(32);
        controlsPane.setAlignment(Pos.CENTER);
        controlsPane.setPadding(new Insets(10));
        controlsPane.getStyleClass().add("overlay");
        HBox.setHgrow(lenses, Priority.ALWAYS);
        HBox.setHgrow(focalLengthBox, Priority.ALWAYS);
        HBox.setHgrow(apertureBox, Priority.ALWAYS);
        HBox.setHgrow(orientationBox, Priority.NEVER);

        StackPane webPane = new StackPane(webView, canvas);

        BorderPane pane = new BorderPane();
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.2), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setCenter(webPane);
        pane.setRight(buttonPane);
        pane.setBottom(controlsPane);

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());

        stage.setTitle("FoVFX");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
