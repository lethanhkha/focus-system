package com.focussystem.view;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class MainView {
    private TabPane layout;
    private VBox taskTabContent;
    private VBox subjectTabContent;
    private VBox calendarTabContent;

    public MainView(VBox taskTabContent, VBox subjectTabContent, VBox calendarTabContent) {
        this.taskTabContent = taskTabContent;
        this.subjectTabContent = subjectTabContent;
        this.calendarTabContent = calendarTabContent;
        buildUI();
    }

    private void buildUI() {
        layout = new TabPane();

        Tab taskTab = new Tab("Quản lý Task", taskTabContent);
        taskTab.setClosable(false);

        Tab subjectTab = new Tab("Quản lý Môn học", subjectTabContent);
        subjectTab.setClosable(false);

        Tab calendarTab = new Tab("Lịch làm việc", calendarTabContent);
        calendarTab.setClosable(false);

        layout.getTabs().addAll(taskTab, subjectTab, calendarTab);
    }

    public TabPane getLayout() { return layout; }

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
