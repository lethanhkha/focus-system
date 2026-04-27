module com.focussystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens com.focussystem.model to com.google.gson, javafx.base;

    opens com.focussystem to javafx.fxml;
    exports com.focussystem;
    exports com.focussystem.model;
    exports com.focussystem.controller;
    exports com.focussystem.view;
    exports com.focussystem.service;
    exports com.focussystem.util;
    exports com.focussystem.component;
}