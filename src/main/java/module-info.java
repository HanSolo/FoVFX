module fovfx {

    // Java
    requires java.base;
    requires java.logging;
    requires java.net.http;

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    //requires javafx.fxml;
    requires javafx.media;
    requires javafx.web;
    requires jdk.jsobject;

    // 3rd party
    requires json.simple;

    exports eu.hansolo.fx.fov;
}