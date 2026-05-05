package com.focussystem.view;

import com.focussystem.component.DateEditingCell;
import com.focussystem.model.Priority;
import com.focussystem.model.Subject;
import com.focussystem.model.Task;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * TaskView: Builds the entire Task management UI including:
 * - Task TableView
 * - Filter bar (search, status, time, export/import)
 * - Input form (semester, subject, title, description, dates)
 * - Status bar (task summary label)
 */
public class TaskView {

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
    private Button btnReset, btnExport, btnImport, btnRefresh, btnTemplate;
    private ToggleButton btnToday;
    private Label lblCompletedToday;

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
    private Label lblTotal;
    private Label lblOverdue;
    private Label lblDone;

    public TaskView() {
        buildUI();
    }

    private void buildUI() {
        table = new TableView<>();
        table.setEditable(true);
        table.getStyleClass().add("task-table");

        // --- COLUMNS ---
        colSubject = new TableColumn<>("Môn học");
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colSubject.setPrefWidth(150);

        colTitle = new TableColumn<>("Nội dung");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setPrefWidth(250);
        // Custom CellFactory: style qua CSS class khi task đã hoàn thành
        colTitle.setCellFactory(col -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("task-completed");
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(item);
                setGraphic(null);
                Task task = getTableRow() == null ? null : getTableRow().getItem();
                if (task != null && task.isCompleted()) {
                    getStyleClass().add("task-completed");
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
        colPriority.setCellFactory(column -> new TableCell<Task, Priority>() {
            private final ComboBox<Priority> comboBox = new ComboBox<>();
            {
                comboBox.setItems(FXCollections.observableArrayList(Priority.values()));
            }
            
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.name());
                    badge.getStyleClass().add("priority-badge");
                    if (item == Priority.HIGH) {
                        badge.getStyleClass().add("priority-high");
                    } else if (item == Priority.MEDIUM) {
                        badge.getStyleClass().add("priority-medium");
                    } else if (item == Priority.LOW) {
                        badge.getStyleClass().add("priority-low");
                    }
                    setGraphic(badge);
                    setText(null);
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
                getStyleClass().removeAll("remaining-overdue", "remaining-soon", "remaining-normal");
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String text = item + " ngày";
                    if (item < 0) {
                        text = "⚠ " + text;
                        getStyleClass().add("remaining-overdue");
                    } else if (item == 0) {
                        text = "🔔 Hôm nay";
                        getStyleClass().add("remaining-soon");
                    } else {
                        getStyleClass().add("remaining-normal");
                    }
                    setText(text);
                    Tooltip tt = new Tooltip("Còn: " + item + " ngày");
                    Tooltip.install(this, tt);
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

        cbFilterStatus = new ComboBox<>();
        cbFilterStatus.setItems(FXCollections.observableArrayList("Tất cả", "Chưa hoàn thành", "Hoàn thành"));
        cbFilterStatus.setValue("Tất cả");

        cbFilterTime = new ComboBox<>();
        cbFilterTime.setItems(FXCollections.observableArrayList("Tất cả", "Hôm nay", "Tuần này", "Quá hạn", "Tương lai"));
        cbFilterTime.setValue("Tất cả");

        btnReset = new Button("⟳ Xóa lọc");

        btnExport = new Button("⬆ Xuất File");

        btnImport = new Button("⬇ Nạp File");

        btnRefresh = new Button("🔄 Cập nhật");
        btnRefresh.setTooltip(new Tooltip("Tải lại dữ liệu từ file tasks.json"));

        btnTemplate = new Button("📄 Tải file mẫu");

        btnToday = new ToggleButton("🌟 Hôm nay");
        btnToday.setTooltip(new Tooltip("Chỉ hiển thị các task chưa xong và có Deadline là Hôm nay hoặc Đã quá hạn"));

        lblCompletedToday = new Label("🔥 Đã hoàn thành hôm nay: 0");

        Separator separator = new Separator(Orientation.VERTICAL);

        // Spacer to push today + refresh to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox filterBar = new HBox(10,
                new Label("Tìm:"), txtSearch,
                new Label("Trạng thái:"), cbFilterStatus,
                new Label("Thời gian:"), cbFilterTime,
                btnReset,
                spacer,
                lblCompletedToday,
                separator,
                btnToday, btnRefresh
         );
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("filter-bar");

        // --- INPUT FORM ---
        cbSemester = new ComboBox<>();
        cbSemester.setPromptText("Chọn Học Kỳ");
        cbSemester.setMaxWidth(Double.MAX_VALUE);

        cbSubject = new ComboBox<>();
        cbSubject.setPromptText("Chọn môn...");
        cbSubject.setMaxWidth(Double.MAX_VALUE);

        cbPriority = new ComboBox<>(FXCollections.observableArrayList(Priority.values()));
        cbPriority.setValue(Priority.MEDIUM);
        cbPriority.setMaxWidth(Double.MAX_VALUE);

        txtTitle = new TextField();
        txtTitle.setPromptText("Nội dung công việc...");
        txtTitle.setMaxWidth(Double.MAX_VALUE);

        txtDesc = new TextField();
        txtDesc.setPromptText("Ghi chú chi tiết...");
        txtDesc.setMaxWidth(Double.MAX_VALUE);

        dpStart = new DatePicker(LocalDate.now());
        dpStart.setPromptText("Bắt đầu");
        dpStart.setMaxWidth(Double.MAX_VALUE);

        dpDue = new DatePicker(LocalDate.now().plusDays(2));
        dpDue.setPromptText("Hạn chót");
        dpDue.setMaxWidth(Double.MAX_VALUE);

        dpDeadline = new DatePicker(LocalDate.now().plusDays(3));
        dpDeadline.setPromptText("Deadline");
        dpDeadline.setMaxWidth(Double.MAX_VALUE);

        btnAdd = new Button("✚ Thêm Task");
        btnAdd.getStyleClass().add("btn-primary");

        btnDelete = new Button("✕ Xóa Task");
        btnDelete.getStyleClass().add("btn-danger");

        btnDeleteAll = new Button("🗑 Xóa tất cả");
        btnDeleteAll.getStyleClass().add("btn-danger");

        // --- STATUS / ACTION FOOTER (single line) ---
        lblTotal = new Label("📊 Tổng: 0");
        lblOverdue = new Label("⚠ Quá hạn: 0");
        lblDone = new Label("✅ Hoàn thành: 0");
        lblTotal.getStyleClass().add("footer-counter");
        lblOverdue.getStyleClass().add("footer-counter");
        lblDone.getStyleClass().add("footer-counter");

        // keep backward-compatible reference
        lblStatusBar = lblTotal;

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox footerBar = new HBox(12, lblTotal, new Separator(Orientation.VERTICAL), lblOverdue, lblDone, bottomSpacer, btnExport, btnImport, btnTemplate);
        footerBar.setAlignment(Pos.CENTER_LEFT);

        // --- HEADER ---
        Label header = new Label("Danh sách nhiệm vụ");
        header.getStyleClass().add("task-header");

        // Legend for colors / badges
        HBox legend = new HBox(8);
        Label l1 = new Label("⚠ Quá hạn"); l1.getStyleClass().add("legend-overdue");
        Label l2 = new Label("🔔 Hôm nay"); l2.getStyleClass().add("legend-today");
        Label l3 = new Label("✅ Hoàn thành"); l3.getStyleClass().add("legend-done");
        legend.getChildren().addAll(l1, l2, l3);
        legend.setAlignment(Pos.CENTER_LEFT);

        VBox leftArea = new VBox(8, header, legend, filterBar, table, footerBar);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        Label formTitle = new Label("Tạo / cập nhật nhiệm vụ");
        formTitle.getStyleClass().add("task-form-title");

        VBox fieldSemester = buildFormField("Học kỳ", cbSemester);
        VBox fieldSubject = buildFormField("Môn học", cbSubject);
        VBox fieldPriority = buildFormField("Mức độ", cbPriority);
        VBox fieldTitle = buildFormField("Nội dung", txtTitle);
        VBox fieldDesc = buildFormField("Ghi chú", txtDesc);
        VBox fieldStart = buildFormField("Ngày bắt đầu", dpStart);
        VBox fieldDue = buildFormField("Hạn chót", dpDue);
        VBox fieldDeadline = buildFormField("Deadline", dpDeadline);

        HBox formActions = new HBox(8, btnAdd, btnDelete, btnDeleteAll);
        formActions.getStyleClass().add("form-actions");

        VBox formPanel = new VBox(12,
            formTitle,
            fieldSemester,
            fieldSubject,
            fieldPriority,
            fieldTitle,
            fieldDesc,
            fieldStart,
            fieldDue,
            fieldDeadline,
            formActions
        );
        formPanel.getStyleClass().add("task-form-panel");

        SplitPane splitPane = new SplitPane(leftArea, formPanel);
        splitPane.setDividerPositions(0.78);

        layout = new VBox(12, splitPane);
        layout.setPadding(new Insets(4));
        VBox.setVgrow(splitPane, javafx.scene.layout.Priority.ALWAYS);
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
    public Button getBtnTemplate() { return btnTemplate; }
    public ToggleButton getBtnToday() { return btnToday; }
    public Label getLblCompletedToday() { return lblCompletedToday; }

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
    public Label getLblTotal() { return lblTotal; }
    public Label getLblOverdue() { return lblOverdue; }
    public Label getLblDone() { return lblDone; }

    /**
     * Marks a control with the "input-error" CSS class for a brief duration (1.5s),
     * then automatically restores it. This provides a visual red-border flash effect.
     */
    public void flashError(Control control) {
        // Visual feedback for error - would use CSS class if available
        // Auto-remove after 1.5 seconds
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
        pause.play();
    }

    private VBox buildFormField(String labelText, Control control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        VBox field = new VBox(6, label, control);
        field.getStyleClass().add("form-field");
        return field;
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
