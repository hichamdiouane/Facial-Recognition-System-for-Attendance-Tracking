module ma.enset.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires okhttp3;
    requires java.net.http;
    requires java.sql;
    requires java.desktop;
    requires annotations;

    // Open packages for JavaFX FXML
    opens ma.enset.app to javafx.fxml;
    opens ma.enset.presentation.controllers to javafx.fxml;

    // Export packages
    exports ma.enset.app;
    exports ma.enset.presentation.controllers;
    exports ma.enset.entities;
    exports ma.enset.dao;
    exports ma.enset.service;

    // Export to specific modules if needed
}