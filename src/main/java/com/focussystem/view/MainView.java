package com.focussystem.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainView {
    private BorderPane layout;
    private VBox taskTabContent;
    private VBox subjectTabContent;
    private VBox calendarTabContent;

    private StackPane contentArea;

    public MainView(VBox taskTabContent, VBox subjectTabContent, VBox calendarTabContent) {
        this.taskTabContent = taskTabContent;
        this.subjectTabContent = subjectTabContent;
        this.calendarTabContent = calendarTabContent;
        buildUI();
    }

    private void buildUI() {
        layout = new BorderPane();
        layout.getStyleClass().add("app-root");

        Label title = new Label("Focus System");
        title.getStyleClass().add("sidebar-title");

        ToggleGroup navGroup = new ToggleGroup();
        ToggleButton btnTasks = createNavButton("Quản lý Task", navGroup, taskTabContent);
        ToggleButton btnSubjects = createNavButton("Quản lý Môn học", navGroup, subjectTabContent);
        ToggleButton btnCalendar = createNavButton("Lịch làm việc", navGroup, calendarTabContent);

        VBox navBox = new VBox(6, btnTasks, btnSubjects, btnCalendar);
        navBox.getStyleClass().add("sidebar-nav");

        VBox sidebar = new VBox(12, title, navBox);
        sidebar.getStyleClass().add("sidebar");

        contentArea = new StackPane(taskTabContent, subjectTabContent, calendarTabContent);
        contentArea.getStyleClass().add("content-area");

        layout.setLeft(sidebar);
        layout.setCenter(contentArea);

        btnTasks.setSelected(true);
        showContent(taskTabContent);
    }

    private ToggleButton createNavButton(String text, ToggleGroup group, VBox targetContent) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(group);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-button");
        button.setOnAction(e -> showContent(targetContent));
        return button;
    }

    private void showContent(VBox target) {
        for (Node node : contentArea.getChildren()) {
            boolean isTarget = node == target;
            node.setVisible(isTarget);
            node.setManaged(isTarget);
        }
    }

    public BorderPane getLayout() { return layout; }

    public VBox getTaskTabContent() {
        return taskTabContent;
    }

    public VBox getSubjectTabContent() {
        return subjectTabContent;
    }

    public VBox getCalendarTabContent() {
        return calendarTabContent;
    }
}
