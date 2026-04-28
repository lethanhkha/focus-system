package com.focussystem.view;

import com.focussystem.model.Subject;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class SubjectView {
    private VBox layout;
    private TableView<Subject> table;
    private Button btnImport, btnSave, btnDel;
    private Consumer<Subject> onActiveChanged;

    public SubjectView() {
        buildUI();
    }

    private void buildUI() {
        table = new TableView<>();
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Subject, String> colHk = new TableColumn<>("Học Kỳ");
        colHk.setCellValueFactory(new PropertyValueFactory<>("semester"));
        
        TableColumn<Subject, String> colCode = new TableColumn<>("Mã HP");
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Subject, String> colName = new TableColumn<>("Tên Môn");
        colName.setCellValueFactory(new PropertyValueFactory<>("name")); 
        colName.setMinWidth(200);
        
        TableColumn<Subject, String> colType = new TableColumn<>("Loại");
        colType.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTypeString()));
        
        TableColumn<Subject, Boolean> colActive = new TableColumn<>("Đăng ký");
        colActive.setCellValueFactory(cell -> {
            Subject s = cell.getValue();
            SimpleBooleanProperty prop = new SimpleBooleanProperty(s.isActive());
            prop.addListener((o, old, val) -> {
                s.setActive(val);
                if (onActiveChanged != null) {
                    onActiveChanged.accept(s);
                }
            });
            return prop;
        });
        colActive.setCellFactory(CheckBoxTableCell.forTableColumn(colActive)); 
        colActive.setEditable(true);

        table.getColumns().setAll(java.util.List.of(colHk, colCode, colName, colType, colActive));
        VBox.setVgrow(table, Priority.ALWAYS);

        btnImport = new Button("⬇ Import từ File (.txt)");

        btnSave = new Button("💾 Cập nhật Đăng ký");

        btnDel = new Button("✕ Xóa Môn");

        HBox tools = new HBox(10, btnImport, btnSave, btnDel); 
        tools.setPadding(new Insets(10));

        Label header = new Label("📚 Quản lý Đăng ký Môn học ");
        
        layout = new VBox(10, header, table, tools);
        layout.setPadding(new Insets(4));
    }

    public VBox getLayout() { return layout; }
    public TableView<Subject> getTable() { return table; }
    public Button getBtnImport() { return btnImport; }
    public Button getBtnSave() { return btnSave; }
    public Button getBtnDel() { return btnDel; }
    public void setOnActiveChanged(Consumer<Subject> listener) { this.onActiveChanged = listener; }
}
