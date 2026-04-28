package com.focussystem.view;

import com.focussystem.component.DateEditingCell;
import com.focussystem.model.Priority;
import com.focussystem.model.Subject;
import com.focussystem.model.Task;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.converter.DefaultStringConverter;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * TaskView: Builds the entire Task management UI including:
 * - Task TableView with conditional row styling (PseudoClass)
 * - Filter bar (search, status, time, export/import)
 * - Input form (semester, subject, title, description, dates)
 * - Status bar (task summary label)
 */
public class TaskView {

    // PseudoClasses for conditional row styling
    private static final PseudoClass OVERDUE_PSEUDO_CLASS = PseudoClass.getPseudoClass("overdue");
    private static final PseudoClass COMPLETED_PSEUDO_CLASS = PseudoClass.getPseudoClass("completed");
    private static final PseudoClass TODAY_PSEUDO_CLASS = PseudoClass.getPseudoClass("today");

    private VBox layout;

    // Table
    private TableView<Task> table;
    private TableColumn<Task, String> colSubject;
    private TableColumn<Task, String> colTitle;
    private TableColumn<Task, String> colDesc;
    private TableColumn<Task, Priority> colPriority;
    private TableColumn<Task, LocalDate> colStart;
    private TableColumn<Task, LocalDate> colDue;
    private TableColumn<Task, LocalDate> colDeadline;
    private TableColumn<Task, Long> colRemaining;
    private TableColumn<Task, Boolean> colStatus;

    // Filter Bar
    private TextField txtSearch;
    private ComboBox<String> cbFilterStatus;
    private ComboBox<String> cbFilterTime;
    private Button btnReset, btnExport, btnImport, btnRefresh;

    // Input Form
    private ComboBox<String> cbSemester;
    private ComboBox<Subject> cbSubject;
    private ComboBox<Priority> cbPriority;
    private TextField txtTitle;
    private TextField txtDesc;
    private DatePicker dpStart;
    private DatePicker dpDue;
    private DatePicker dpDeadline;
    private Button btnAdd, btnDelete, btnDeleteAll;

    // Status Bar
    private Label lblStatusBar;

    public TaskView() {
        buildUI();
    }

    private void buildUI() {
        table = new TableView<>();
        table.setEditable(true);

        // --- Row Factory with PseudoClass-based conditional styling ---
        table.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>() {
                @Override
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);
                    // Clear all pseudo-classes first
                    pseudoClassStateChanged(OVERDUE_PSEUDO_CLASS, false);
                    pseudoClassStateChanged(COMPLETED_PSEUDO_CLASS, false);
                    pseudoClassStateChanged(TODAY_PSEUDO_CLASS, false);

                    if (empty || task == null) {
                        return;
                    }

                    LocalDate today = LocalDate.now();
                    boolean isCompleted = "Hoàn thành".equals(task.getStatus());
                    boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(today) && !isCompleted;
                    boolean isToday = task.getDueDate() != null && task.getDueDate().isEqual(today) && !isCompleted;

                    pseudoClassStateChanged(COMPLETED_PSEUDO_CLASS, isCompleted);
                    pseudoClassStateChanged(OVERDUE_PSEUDO_CLASS, isOverdue);
                    pseudoClassStateChanged(TODAY_PSEUDO_CLASS, isToday);
                }
            };
            return row;
        });

        // --- COLUMNS ---
        colSubject = new TableColumn<>("Môn học");
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colSubject.setPrefWidth(150);

        colTitle = new TableColumn<>("Nội dung");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(250);
        // Custom CellFactory: gạch ngang + làm mờ khi task đã hoàn thành
        colTitle.setCellFactory(col -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setOpacity(1.0);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    Text text = new Text(item);
                    if (task != null && task.isCompleted()) {
                        text.setStrikethrough(true);
                        setOpacity(0.55);
                    } else {
                        text.setStrikethrough(false);
                        setOpacity(1.0);
                    }
                    setGraphic(text);
                    setText(null);
                }
            }
        });

        colDesc = new TableColumn<>("Ghi chú");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(200);
        colDesc.setCellFactory(TextFieldTableCell.forTableColumn());

        colPriority = new TableColumn<>("Mức độ");
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colPriority.setPrefWidth(90);
        colPriority.setCellFactory(column -> new ComboBoxTableCell<Task, Priority>(Priority.values()) {
            @Override
            public void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case HIGH:
                            setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 2; -fx-background-radius: 4; -fx-background-insets: 2;");
                            break;
                        case MEDIUM:
                            setStyle("-fx-text-fill: black; -fx-background-color: #f1c40f; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 2; -fx-background-radius: 4; -fx-background-insets: 2;");
                            break;
                        case LOW:
                            setStyle("-fx-text-fill: white; -fx-background-color: #2ecc71; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 2; -fx-background-radius: 4; -fx-background-insets: 2;");
                            break;
                    }
                }
            }
        });

        colStart = new TableColumn<>("Bắt đầu");
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStart.setPrefWidth(110);
        colStart.setCellFactory(column -> new DateEditingCell<>());

        colDue = new TableColumn<>("Hạn chót");
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colDue.setPrefWidth(110);
        colDue.setCellFactory(column -> new DateEditingCell<>());

        colDeadline = new TableColumn<>("Deadline");
        colDeadline.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        colDeadline.setPrefWidth(110);
        colDeadline.setCellFactory(column -> new DateEditingCell<>());

        colRemaining = new TableColumn<>("Còn lại");
        colRemaining.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDaysRemaining()));
        colRemaining.setPrefWidth(90);
        colRemaining.setCellFactory(column -> new TableCell<Task, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    getStyleClass().removeAll("urgent-task", "warning-task", "normal-task");
                } else {
                    setText(item + " ngày");
                    getStyleClass().removeAll("urgent-task", "warning-task", "normal-task");
                    if (item < 0) {
                        getStyleClass().add("urgent-task");
                    } else if (item <= 2) {
                        getStyleClass().add("warning-task");
                    } else {
                        getStyleClass().add("normal-task");
                    }
                }
            }
        });

        colStatus = new TableColumn<>("✔");
        colStatus.setCellValueFactory(cellData -> cellData.getValue().completedProperty());
        colStatus.setPrefWidth(50);
        colStatus.setCellFactory(CheckBoxTableCell.forTableColumn(colStatus));
        colStatus.setEditable(true);
        colStatus.setSortable(false);

        table.getColumns().setAll(java.util.List.of(colSubject, colTitle, colDesc, colPriority, colStart, colDue, colDeadline, colRemaining, colStatus));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        // --- FILTER BAR ---
        txtSearch = new TextField();
        txtSearch.setPromptText("\uD83D\uDD0D Tìm tên task, môn, ghi chú...");
        txtSearch.setPrefWidth(240);
        txtSearch.getStyleClass().add("search-box");

        cbFilterStatus = new ComboBox<>();
        cbFilterStatus.setItems(FXCollections.observableArrayList("Tất cả", "Chưa hoàn thành", "Hoàn thành"));
        cbFilterStatus.setValue("Tất cả");
        cbFilterStatus.getStyleClass().add("filter-combo");

        cbFilterTime = new ComboBox<>();
        cbFilterTime.setItems(FXCollections.observableArrayList("Tất cả", "Hôm nay", "Tuần này", "Quá hạn", "Tương lai"));
        cbFilterTime.setValue("Tất cả");
        cbFilterTime.getStyleClass().add("filter-combo");

        btnReset = new Button("⟳ Xóa lọc");
        btnReset.getStyleClass().add("btn-reset");

        btnExport = new Button("⬆ Xuất File");
        btnExport.getStyleClass().add("btn-secondary");

        btnImport = new Button("⬇ Nạp File");
        btnImport.getStyleClass().add("btn-secondary");

        btnRefresh = new Button("🔄 Cập nhật");
        btnRefresh.getStyleClass().add("btn-reset");
        btnRefresh.setTooltip(new Tooltip("Tải lại dữ liệu từ file tasks.json"));

        Separator separator = new Separator(Orientation.VERTICAL);

        // Spacer to push export/import to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox filterBar = new HBox(10,
                new Label("Tìm:"), txtSearch,
                new Label("Trạng thái:"), cbFilterStatus,
                new Label("Thời gian:"), cbFilterTime,
                btnReset,
                spacer,
                separator,
                btnRefresh, btnExport, btnImport
        );
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("filter-bar");

        // --- INPUT FORM ---
        cbSemester = new ComboBox<>();
        cbSemester.setPromptText("Chọn Học Kỳ");

        cbSubject = new ComboBox<>();
        cbSubject.setPromptText("Chọn môn...");
        cbSubject.setPrefWidth(160);

        cbPriority = new ComboBox<>(FXCollections.observableArrayList(Priority.values()));
        cbPriority.setValue(Priority.MEDIUM);
        cbPriority.setPrefWidth(100);

        txtTitle = new TextField();
        txtTitle.setPromptText("Nội dung công việc...");
        txtTitle.setPrefWidth(200);

        txtDesc = new TextField();
        txtDesc.setPromptText("Ghi chú chi tiết...");
        txtDesc.setPrefWidth(180);

        dpStart = new DatePicker(LocalDate.now());
        dpStart.setPromptText("Bắt đầu");
        dpStart.setPrefWidth(120);

        dpDue = new DatePicker(LocalDate.now().plusDays(2));
        dpDue.setPromptText("Hạn chót");
        dpDue.setPrefWidth(110);

        dpDeadline = new DatePicker(LocalDate.now().plusDays(3));
        dpDeadline.setPromptText("Deadline");
        dpDeadline.setPrefWidth(110);

        btnAdd = new Button("✚ Thêm Task");
        btnAdd.getStyleClass().add("btn-primary");

        btnDelete = new Button("✕ Xóa Task");
        btnDelete.getStyleClass().add("btn-danger");

        btnDeleteAll = new Button("🗑 Xóa tất cả");
        btnDeleteAll.getStyleClass().add("btn-danger");
        btnDeleteAll.setStyle("-fx-background-color: #c0392b;"); // đỏ đậm hơn để phân biệt

        HBox row1 = new HBox(10, new Label("Học kỳ:"), cbSemester, new Label("Môn:"), cbSubject, new Label("Mức độ:"), cbPriority);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(8, txtTitle, txtDesc, dpStart, new Label("→"), dpDue, new Label("⚑"), dpDeadline, btnAdd, btnDelete, btnDeleteAll);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox inputForm = new VBox(8, row1, row2);
        inputForm.getStyleClass().add("input-form");

        // --- STATUS BAR ---
        lblStatusBar = new Label("Tổng: 0 task");
        HBox statusBar = new HBox(lblStatusBar);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        // --- HEADER ---
        Label header = new Label("📋 Danh sách nhiệm vụ & Bộ lọc");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 8 0 0 8;");

        layout = new VBox(8, header, filterBar, table, inputForm, statusBar);
        layout.setPadding(new Insets(4));
    }

    // --- GETTERS ---
    public VBox getLayout() { return layout; }

    public TableView<Task> getTable() { return table; }
    public TableColumn<Task, String> getColSubject() { return colSubject; }
    public TableColumn<Task, String> getColTitle() { return colTitle; }
    public TableColumn<Task, String> getColDesc() { return colDesc; }
    public TableColumn<Task, Priority> getColPriority() { return colPriority; }
    public TableColumn<Task, LocalDate> getColStart() { return colStart; }
    public TableColumn<Task, LocalDate> getColDue() { return colDue; }
    public TableColumn<Task, LocalDate> getColDeadline() { return colDeadline; }
    public TableColumn<Task, Boolean> getColStatus() { return colStatus; }

    public TextField getTxtSearch() { return txtSearch; }
    public ComboBox<String> getCbFilterStatus() { return cbFilterStatus; }
    public ComboBox<String> getCbFilterTime() { return cbFilterTime; }
    public Button getBtnReset() { return btnReset; }
    public Button getBtnExport() { return btnExport; }
    public Button getBtnImport() { return btnImport; }
    public Button getBtnRefresh() { return btnRefresh; }

    public ComboBox<String> getCbSemester() { return cbSemester; }
    public ComboBox<Subject> getCbSubject() { return cbSubject; }
    public ComboBox<Priority> getCbPriority() { return cbPriority; }
    public TextField getTxtTitle() { return txtTitle; }
    public TextField getTxtDesc() { return txtDesc; }
    public DatePicker getDpStart() { return dpStart; }
    public DatePicker getDpDue() { return dpDue; }
    public DatePicker getDpDeadline() { return dpDeadline; }
    public Button getBtnAdd() { return btnAdd; }
    public Button getBtnDelete() { return btnDelete; }
    public Button getBtnDeleteAll() { return btnDeleteAll; }

    public Label getLblStatusBar() { return lblStatusBar; }

    /**
     * Marks a control with the "input-error" CSS class for a brief duration (1.5s),
     * then automatically restores it. This provides a visual red-border flash effect.
     */
    public void flashError(Control control) {
        control.getStyleClass().add("input-error");
        // Auto-remove after 1.5 seconds
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
        pause.setOnFinished(e -> control.getStyleClass().remove("input-error"));
        pause.play();
    }

    /**
     * Helper for ComboBox cell factory in table.
     */
    public void setupSubjectColumnChoices(ObservableList<Subject> subjectList) {
        colSubject.setCellFactory(column -> {
            ObservableList<String> activeSubjectNames = FXCollections.observableArrayList(
                    subjectList.stream().filter(Subject::isActive).map(Subject::getName).collect(Collectors.toList())
            );
            return new ComboBoxTableCell<>(new DefaultStringConverter(), activeSubjectNames);
        });
    }
}
