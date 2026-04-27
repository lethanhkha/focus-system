package com.focussystem.controller;

import com.focussystem.model.Subject;
import com.focussystem.service.DataManager;
import com.focussystem.util.AlertHelper;
import com.focussystem.view.SubjectView;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SubjectController {
    private SubjectView view;
    private ObservableList<Subject> subjectList;
    private DataManager dataManager;

    public SubjectController(DataManager dataManager, ObservableList<Subject> subjectList) {
        this.dataManager = dataManager;
        this.subjectList = subjectList;
        this.view = new SubjectView();
        
        initView();
    }

    private void initView() {
        view.getTable().setItems(subjectList);
        
        view.getBtnImport().setOnAction(e -> handleImport());
        view.getBtnSave().setOnAction(e -> handleSave());
        view.getBtnDel().setOnAction(e -> handleDelete());
        view.getTable().setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.DELETE) handleDelete();
        });
        
        view.setOnActiveChanged(subject -> {
            // Can notify MainController or TaskController if needed
        });
    }

    private void handleImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import Môn Học");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File f = fc.showOpenDialog(view.getLayout().getScene() != null ? view.getLayout().getScene().getWindow() : null);
        if (f != null) {
            List<Subject> imported = dataManager.importSubjectsFromTextFile(f);
            if(imported.isEmpty()) {
                AlertHelper.showWarning("Không có môn học nào được import (file rỗng hoặc sai định dạng)!");
                return;
            }
            subjectList.addAll(imported);
            dataManager.saveSubjects(new ArrayList<>(subjectList));
            AlertHelper.showSuccess("Đã import " + imported.size() + " môn!");
        }
    }

    private void handleSave() {
        dataManager.saveSubjects(new ArrayList<>(subjectList));
        AlertHelper.showSuccess("Đã lưu!");
    }

    private void handleDelete() {
        ObservableList<Subject> selected = view.getTable().getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) { 
            AlertHelper.showWarning("Chưa chọn môn để xóa!"); 
            return; 
        }
        if (AlertHelper.showConfirmation("Xác nhận xóa", "Xóa " + selected.size() + " môn học?")) {
            subjectList.removeAll(new ArrayList<>(selected));
            dataManager.saveSubjects(new ArrayList<>(subjectList));
            view.getTable().getSelectionModel().clearSelection();
            AlertHelper.showSuccess("Đã xóa thành công!");
        }
    }

    public SubjectView getView() {
        return view;
    }
}
