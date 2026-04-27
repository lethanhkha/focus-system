package com.focussystem.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class CalendarView {
    private VBox layout;
    
    private Button btnPrevMonth;
    private Button btnNextMonth;
    private Button btnToday;
    private Label lblMonthYear;
    
    private GridPane calendarGrid;

    public CalendarView() {
        buildUI();
    }

    private void buildUI() {
        layout = new VBox(10);
        layout.setPadding(new Insets(10));
        
        // --- HEADER ---
        btnPrevMonth = new Button("◀");
        btnPrevMonth.getStyleClass().add("btn-secondary");
        
        lblMonthYear = new Label("Tháng X Năm YYYY");
        lblMonthYear.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        lblMonthYear.setPrefWidth(200);
        lblMonthYear.setAlignment(Pos.CENTER);
        
        btnNextMonth = new Button("▶");
        btnNextMonth.getStyleClass().add("btn-secondary");
        
        btnToday = new Button("Hôm nay");
        btnToday.getStyleClass().add("btn-primary");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        HBox header = new HBox(10, spacer1, btnPrevMonth, lblMonthYear, btnNextMonth, btnToday, spacer2);
        header.setAlignment(Pos.CENTER);
        
        // --- GRID ---
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);
        
        // Set column constraints so 7 columns have equal width
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            calendarGrid.getColumnConstraints().add(col);
        }
        
        // Add days of week header
        String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.getStyleClass().add("calendar-day-header");
            calendarGrid.add(dayLabel, i, 0);
        }
        
        // Ensure calendar grows
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);
        
        layout.getChildren().addAll(header, calendarGrid);
    }

    // --- GETTERS ---
    public VBox getLayout() { return layout; }
    public Button getBtnPrevMonth() { return btnPrevMonth; }
    public Button getBtnNextMonth() { return btnNextMonth; }
    public Button getBtnToday() { return btnToday; }
    public Label getLblMonthYear() { return lblMonthYear; }
    public GridPane getCalendarGrid() { return calendarGrid; }
}
