package com.focussystem.controller;

import com.focussystem.model.Config;
import com.focussystem.model.Subject;
import com.focussystem.model.Task;
import com.focussystem.service.DataManager;
import com.focussystem.view.MainView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;

public class MainController {
    private final MainView mainView;
    private final DataManager dataManager;

    private final ObservableList<Task> taskList;
    private final ObservableList<Subject> subjectList;
    private final Config currentConfig;

    private final TaskController taskController;
    private final SubjectController subjectController;
    private final CalendarController calendarController;

    public MainController() {
        dataManager = DataManager.getInstance();

        // Load data
        taskList = FXCollections.observableArrayList(dataManager.loadTasks());
        subjectList = FXCollections.observableArrayList(dataManager.loadSubjects());
        currentConfig = dataManager.loadConfig();

        // Initialize sub-controllers
        taskController = new TaskController(dataManager, taskList, subjectList, currentConfig);
        subjectController = new SubjectController(dataManager, subjectList);
        calendarController = new CalendarController(taskList);

        // When subjects change or become active/inactive, refresh tasks filter
        subjectController.getView().setOnActiveChanged(subject -> taskController.triggerRefresh());

        // Initialize main view
        mainView = new MainView(taskController.getView().getLayout(), subjectController.getView().getLayout(), calendarController.getView().getLayout());
    }

    public Scene getPrimaryScene() {
        Scene scene = new Scene(mainView.getLayout(), 1200, 750);

        var cssUrl = getClass().getResource("/css/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Warning: Cannot load css/styles.css");
        }

        // Set up click outside table to cancel edit
        scene.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            // Guard: event target might be a Scene, not a Node
            if (!(target instanceof Node source)) {
                return;
            }
            if (!isDescendant(mainView.getTaskTabContent(), source)) {
                taskController.getView().getTable().edit(-1, null);
                taskController.getView().getTable().getSelectionModel().clearSelection();
            }
            if (!isDescendant(mainView.getSubjectTabContent(), source)) {
                subjectController.getView().getTable().edit(-1, null);
                subjectController.getView().getTable().getSelectionModel().clearSelection();
            }
        });

        return scene;
    }

    private boolean isDescendant(Node parent, Node child) {
        if (child == null || parent == null) return false;
        if (child == parent) return true;
        return isDescendant(parent, child.getParent());
    }
}
