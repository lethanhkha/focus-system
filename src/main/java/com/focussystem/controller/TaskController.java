package com.focussystem.controller;

import com.focussystem.model.Config;
import com.focussystem.model.Subject;
import com.focussystem.model.Task;
import com.focussystem.service.DataManager;
import com.focussystem.util.AlertHelper;
import com.focussystem.view.TaskView;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskController: Handles all business logic for the Task management screen.
 * Responsibilities:
 * - Table inline editing events
 * - Filter logic (search, status, time, semester)
 * - Input form validation with visual cues (red border flash + auto-focus)
 * - Keyboard shortcuts (ENTER to add, ESC to clear)
 * - Status bar updates
 * - Import/Export tasks
 */
public class TaskController {
    private TaskView view;
    private DataManager dataManager;
    private ObservableList<Task> taskList;
    private ObservableList<Subject> subjectList;
    private Config currentConfig;

    private FilteredList<Task> filteredData;

    // Thêm task vào đây sau khi gắn listener để tránh memory leak
    private final java.util.WeakHashMap<Task, javafx.beans.value.ChangeListener<Boolean>> checkboxListeners = new java.util.WeakHashMap<>();

    public TaskController(DataManager dataManager, ObservableList<Task> taskList, ObservableList<Subject> subjectList, Config currentConfig) {
        this.dataManager = dataManager;
        this.taskList = taskList;
        this.subjectList = subjectList;
        this.currentConfig = currentConfig;
        this.view = new TaskView();

        initController();
    }

    private void initController() {
        // Setup table data wrapping
        filteredData = new FilteredList<>(taskList, p -> true);
        SortedList<Task> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(view.getTable().comparatorProperty());
        view.getTable().setItems(sortedData);

        // Bind Subject ComboBox in table
        view.setupSubjectColumnChoices(subjectList);

        // Listener to refresh Subject combo if subjects change
        subjectList.addListener((ListChangeListener<Subject>) c -> {
            view.setupSubjectColumnChoices(subjectList);
            refreshSemesterChoices();
        });

        // Event for Table Edit Commits
        setupTableEditEvents();

        // Setup Checkbox Listeners (save on tick)
        setupCheckboxListeners();
        taskList.addListener((javafx.collections.ListChangeListener<Task>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(this::attachCheckboxListener);
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(t -> checkboxListeners.remove(t));
                }
            }
        });

        // Setup Filter Bar Events
        setupFilterEvents();

        // Setup Input Form Events
        setupInputFormEvents();

        // Setup keyboard shortcuts
        setupKeyboardShortcuts();

        // Initialize Semester data in input form
        refreshSemesterChoices();
        initSemesterSelection();

        // Listen to task list changes to update status bar
        taskList.addListener((ListChangeListener<Task>) c -> updateStatusBar());

        // Initial Filter update & status bar
        updateFilter();
        updateStatusBar();
    }

    private void setupTableEditEvents() {
        view.getColSubject().setOnEditCommit(e -> {
            e.getRowValue().setSubject(e.getNewValue());
            saveAndRefresh();
            view.getTable().refresh(); // Refresh row styling
        });

        view.getColTitle().setOnEditCommit(e -> {
            e.getRowValue().setTitle(e.getNewValue());
            saveAndRefresh();
        });

        view.getColDesc().setOnEditCommit(e -> {
            e.getRowValue().setDescription(e.getNewValue());
            saveAndRefresh();
        });

        view.getColStart().setOnEditCommit(e -> {
            e.getRowValue().setStartDate(e.getNewValue());
            saveAndRefresh();
            view.getTable().refresh();
        });

        view.getColDue().setOnEditCommit(e -> {
            if (e.getNewValue().isBefore(e.getRowValue().getStartDate())) {
                AlertHelper.showError("Lỗi ngày", "Hạn chót không thể trước ngày bắt đầu!");
                view.getTable().refresh();
                return;
            }
            e.getRowValue().setDueDate(e.getNewValue());
            saveAndRefresh();
            view.getTable().refresh();
        });

        view.getColDeadline().setOnEditCommit(e -> {
            e.getRowValue().setDeadline(e.getNewValue());
            saveAndRefresh();
            view.getTable().refresh();
        });
        // colStatus: không dùng OnEditCommit nữa – CheckBoxTableCell tự bind vào completedProperty()

        // Delete via key
        view.getTable().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) handleDelete();
        });

        // --- Task 3: Deselect khi click vào vùng trống của Table ---
        view.getTable().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                // Nếu click vào phần không có row (empty area bên dưới các dòng)
                javafx.scene.Node picked = e.getPickResult().getIntersectedNode();
                // Pick result là chính bảng hoặc virtual flow -> không phải cell
                boolean isEmptyArea = picked instanceof TableView
                        || (picked != null && picked.getStyleClass().contains("table-row-cell") && ((TableRow<?>) picked).isEmpty());
                if (isEmptyArea) {
                    view.getTable().getSelectionModel().clearSelection();
                    e.consume();
                }
            }
        });

        // --- Task 3: Deselect khi click ra ngoài bảng (vào vùng nền của layout) ---
        view.getLayout().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                javafx.scene.Node picked = e.getPickResult().getIntersectedNode();
                // Nếu node được click không nằm trong table thì clearSelection
                if (picked != null && !isInsideNode(picked, view.getTable())) {
                    view.getTable().getSelectionModel().clearSelection();
                }
            }
        });
    }

    /** Kiểm tra xem một node có nằm trong cây con của parent node hay không. */
    private boolean isInsideNode(javafx.scene.Node node, javafx.scene.Node parent) {
        javafx.scene.Node current = node;
        while (current != null) {
            if (current == parent) return true;
            current = current.getParent();
        }
        return false;
    }

    private void setupFilterEvents() {
        view.getTxtSearch().textProperty().addListener((o, old, val) -> updateFilter());
        view.getCbFilterStatus().valueProperty().addListener((o, old, val) -> updateFilter());
        view.getCbFilterTime().valueProperty().addListener((o, old, val) -> updateFilter());

        view.getBtnReset().setOnAction(e -> {
            view.getTxtSearch().clear();
            view.getCbFilterStatus().setValue("Tất cả");
            view.getCbFilterTime().setValue("Tất cả");
        });

        view.getBtnExport().setOnAction(e -> handleExport());
        view.getBtnImport().setOnAction(e -> handleImport());
        view.getBtnRefresh().setOnAction(e -> handleRefresh());
    }

    private void setupInputFormEvents() {
        view.getCbSemester().setOnAction(e -> {
            String selectedSem = view.getCbSemester().getValue();
            if (selectedSem != null) {
                view.getCbSubject().setItems(FXCollections.observableArrayList(
                        subjectList.stream().filter(s -> s.getSemester().equals(selectedSem) && s.isActive()).collect(Collectors.toList())
                ));
                view.getCbSubject().getSelectionModel().clearSelection();

                currentConfig.setLastSelectedSemester(selectedSem);
                dataManager.saveConfig(currentConfig);
                updateFilter();
            }
        });

        view.getBtnAdd().setOnAction(e -> handleAdd());
        view.getBtnDelete().setOnAction(e -> handleDelete());
        view.getBtnDeleteAll().setOnAction(e -> handleDeleteAll());
    }

    /**
     * Sets up keyboard shortcuts for the input form:
     * - ENTER: triggers Add Task
     * - ESC: clears the input form
     */
    private void setupKeyboardShortcuts() {
        // ENTER on title field triggers add
        view.getTxtTitle().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { handleAdd(); e.consume(); }
            if (e.getCode() == KeyCode.ESCAPE) { clearForm(); e.consume(); }
        });
        // ENTER on description field triggers add
        view.getTxtDesc().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { handleAdd(); e.consume(); }
            if (e.getCode() == KeyCode.ESCAPE) { clearForm(); e.consume(); }
        });
    }

    private void refreshSemesterChoices() {
        var semesters = subjectList.stream().map(Subject::getSemester).distinct().sorted().collect(Collectors.toList());
        view.getCbSemester().setItems(FXCollections.observableArrayList(semesters));
    }

    private void initSemesterSelection() {
        String lastSem = currentConfig.getLastSelectedSemester();
        if (lastSem != null && !lastSem.isEmpty() && view.getCbSemester().getItems().contains(lastSem)) {
            view.getCbSemester().setValue(lastSem);
            view.getCbSubject().setItems(FXCollections.observableArrayList(
                    subjectList.stream().filter(s -> s.getSemester().equals(lastSem) && s.isActive()).collect(Collectors.toList())
            ));
        }
    }

    private void updateFilter() {
        String searchText = view.getTxtSearch().getText().toLowerCase();
        String statusFilter = view.getCbFilterStatus().getValue();
        String timeFilter = view.getCbFilterTime().getValue();
        String currentSemester = currentConfig.getLastSelectedSemester();

        filteredData.setPredicate(task -> {
            boolean matchSemester = true;
            if (currentSemester != null && !currentSemester.isEmpty()) {
                Subject subject = subjectList.stream()
                        .filter(s -> s.getName().equals(task.getSubject()))
                        .findFirst().orElse(null);

                matchSemester = subject != null && subject.getSemester().equals(currentSemester);
            }
            if (!matchSemester) return false;

            boolean matchSearch = true;
            if (searchText != null && !searchText.isEmpty()) {
                String title = task.getTitle().toLowerCase();
                String subj = task.getSubject().toLowerCase();
                String desc = (task.getDescription() != null) ? task.getDescription().toLowerCase() : "";

                matchSearch = title.contains(searchText) || subj.contains(searchText) || desc.contains(searchText);
            }

            boolean matchStatus = true;
            if (statusFilter != null && !"Tất cả".equals(statusFilter)) {
                matchStatus = task.getStatus().equals(statusFilter);
            }

            boolean matchTime = true;
            LocalDate today = LocalDate.now();
            if (timeFilter != null && !"Tất cả".equals(timeFilter)) {
                switch (timeFilter) {
                    case "Hôm nay":
                        matchTime = task.getDueDate() != null && task.getDueDate().isEqual(today);
                        break;
                    case "Tuần này":
                        matchTime = task.getDueDate() != null && !task.getDueDate().isBefore(today) && task.getDueDate().isBefore(today.plusDays(7));
                        break;
                    case "Quá hạn":
                        matchTime = task.getDueDate() != null && task.getDueDate().isBefore(today) && !"Hoàn thành".equals(task.getStatus());
                        break;
                    case "Tương lai":
                        matchTime = task.getDueDate() != null && task.getDueDate().isAfter(today);
                        break;
                }
            }

            return matchSearch && matchStatus && matchTime;
        });

        updateStatusBar();
    }

    /**
     * Smart Add with visual validation cues:
     * - Flashes red border on invalid fields
     * - Auto-focuses the first invalid field
     */
    private void handleAdd() {
        Subject sub = view.getCbSubject().getValue();
        String titleText = view.getTxtTitle().getText();

        // Validate: Subject must be selected
        if (sub == null) {
            view.flashError(view.getCbSubject());
            view.getCbSubject().requestFocus();
            AlertHelper.showError("Thiếu dữ liệu", "Vui lòng chọn môn học!");
            return;
        }

        // Validate: Title must not be empty
        if (titleText == null || titleText.trim().isEmpty()) {
            view.flashError(view.getTxtTitle());
            view.getTxtTitle().requestFocus();
            AlertHelper.showError("Thiếu dữ liệu", "Vui lòng nhập nội dung công việc!");
            return;
        }

        // Validate: Due date must not be before start date
        if (view.getDpDue().getValue().isBefore(view.getDpStart().getValue())) {
            view.flashError(view.getDpDue());
            view.getDpDue().requestFocus();
            AlertHelper.showError("Lỗi ngày", "Hạn chót không được trước ngày bắt đầu!");
            return;
        }

        // Validate: Deadline must not be before start date (optional, but good practice)
        if (view.getDpDeadline().getValue() != null && view.getDpDeadline().getValue().isBefore(view.getDpStart().getValue())) {
            view.flashError(view.getDpDeadline());
            view.getDpDeadline().requestFocus();
            AlertHelper.showError("Lỗi ngày", "Deadline không được trước ngày bắt đầu!");
            return;
        }

        Task newTask = new Task(titleText.trim(), sub.getName(), view.getDpStart().getValue(), view.getDpDue().getValue(), view.getDpDeadline().getValue(), view.getTxtDesc().getText());
        newTask.setStatus("Chưa hoàn thành");
        taskList.add(newTask);
        saveAndRefresh();

        // Clear inputs after successful add
        view.getTxtTitle().clear();
        view.getTxtDesc().clear();
        view.getTxtTitle().requestFocus();
        AlertHelper.showSuccess("Đã thêm công việc mới!");
    }

    private void handleDelete() {
        Task selected = view.getTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Vui lòng chọn công việc cần xóa!");
            return;
        }
        if (AlertHelper.showConfirmation("Xóa công việc", "Bạn muốn xóa: " + selected.getTitle() + "?")) {
            taskList.remove(selected);
            saveAndRefresh();
            AlertHelper.showSuccess("Đã xóa thành công!");
        }
    }

    /** Task 5: Xóa toàn bộ công việc sau khi xác nhận. */
    private void handleDeleteAll() {
        if (taskList.isEmpty()) {
            AlertHelper.showWarning("Danh sách đã trống, không có gì để xóa!");
            return;
        }
        boolean confirmed = AlertHelper.showConfirmation(
                "🗑 Xóa tất cả công việc",
                "Bạn chắc chắn muốn xóa toàn bộ " + taskList.size() + " công việc?\nHành động này không thể hoàn tác!"
        );
        if (confirmed) {
            taskList.clear();
            saveAndRefresh();
            AlertHelper.showSuccess("Đã xóa toàn bộ danh sách công việc!");
        }
    }

    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file danh sách Task");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("my_study_tasks.json");
        File file = fileChooser.showSaveDialog(view.getLayout().getScene() != null ? view.getLayout().getScene().getWindow() : null);

        if (file != null) {
            dataManager.exportTasksToJson(file, new ArrayList<>(taskList));
            AlertHelper.showSuccess("Đã xuất " + taskList.size() + " công việc ra file thành công!");
        }
    }

    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Task JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(view.getLayout().getScene() != null ? view.getLayout().getScene().getWindow() : null);

        if (file != null) {
            List<Task> importedTasks = dataManager.importTasksFromJson(file);
            if (!importedTasks.isEmpty()) {
                taskList.addAll(importedTasks);
                saveAndRefresh();
                AlertHelper.showSuccess("Đã nạp thêm " + importedTasks.size() + " công việc vào hệ thống!");
            } else {
                AlertHelper.showWarning("File rỗng hoặc không đúng định dạng!");
            }
        }
    }

    /**
     * Task 4: Làm mới (Refresh) dữ liệu từ file tasks.json.
     * Xoá sạch ObservableList hiện tại → đọc lại JSON → đổ vào bảng.
     */
    private void handleRefresh() {
        List<Task> freshTasks = dataManager.loadTasks();
        taskList.clear();
        taskList.addAll(freshTasks);
        // attachCheckboxListener được gắn tự động qua taskList ListChangeListener
        updateFilter();
        view.getTable().refresh();
        AlertHelper.showSuccess("🔄 Đã làm mới dữ liệu từ tasks.json!");
    }

    /** Gắn listener vào completedProperty của mọt task: khi tick/untick -> lưu JSON + refresh table. */
    private void attachCheckboxListener(Task task) {
        javafx.beans.value.ChangeListener<Boolean> listener = (obs, oldVal, newVal) -> {
            saveAndRefresh();
            view.getTable().refresh(); // Làm mới cột Nội dung để hiện strikethrough
        };
        checkboxListeners.put(task, listener);
        task.completedProperty().addListener(listener);
    }

    /** Gắn listener cho tất cả tasks hiện tại trong list. */
    private void setupCheckboxListeners() {
        taskList.forEach(this::attachCheckboxListener);
    }

    /** Clears all input form fields. */
    private void clearForm() {
        view.getTxtTitle().clear();
        view.getTxtDesc().clear();
        view.getCbSubject().getSelectionModel().clearSelection();
        view.getTable().getSelectionModel().clearSelection();
    }

    private void saveAndRefresh() {
        dataManager.saveTasks(new ArrayList<>(taskList));
        view.getTable().refresh(); // Ensure PseudoClass row styles update
    }

    /**
     * Updates the status bar label with a summary of total, overdue, and completed tasks.
     */
    private void updateStatusBar() {
        int total = filteredData.size();
        LocalDate today = LocalDate.now();
        long overdue = filteredData.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && !t.isCompleted())
                .count();
        long completed = filteredData.stream()
                .filter(Task::isCompleted)
                .count();

        view.getLblStatusBar().setText(
                String.format("📊 Tổng: %d task  |  ⚠ Quá hạn: %d  |  ✅ Hoàn thành: %d", total, overdue, completed)
        );
    }

    public TaskView getView() {
        return view;
    }

    public void triggerRefresh() {
        updateFilter();
        view.getTable().refresh();
    }
}
