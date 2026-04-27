package com.focussystem.controller;

import com.focussystem.model.Task;
import com.focussystem.view.CalendarView;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarController {
    private final CalendarView view;
    private final ObservableList<Task> taskList;
    private YearMonth currentYearMonth;

    public CalendarController(ObservableList<Task> taskList) {
        this.taskList = taskList;
        this.view = new CalendarView();
        this.currentYearMonth = YearMonth.now();

        setupEvents();
        renderCalendar();
    }

    private void setupEvents() {
        view.getBtnPrevMonth().setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            renderCalendar();
        });

        view.getBtnNextMonth().setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            renderCalendar();
        });

        view.getBtnToday().setOnAction(e -> {
            currentYearMonth = YearMonth.now();
            renderCalendar();
        });

        // Listen to task changes to re-render calendar
        taskList.addListener((ListChangeListener<Task>) c -> renderCalendar());
    }

    private void renderCalendar() {
        // Update header
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        view.getLblMonthYear().setText("Tháng " + currentYearMonth.format(formatter));

        GridPane grid = view.getCalendarGrid();
        
        // Remove old days (row > 0)
        grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        // Compute calendar days
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        DayOfWeek startDayOfWeek = firstOfMonth.getDayOfWeek();
        
        // In Java, Monday is 1, Sunday is 7. Our grid columns: 0 (Thứ 2) to 6 (Chủ nhật)
        int startCol = startDayOfWeek.getValue() - 1;

        // Clear existing row constraints to avoid duplicate accumulation
        grid.getRowConstraints().clear();
        
        // Ensure 6 rows exist (1 to 6)
        for (int i = 1; i <= 6; i++) {
            javafx.scene.layout.RowConstraints rowRules = new javafx.scene.layout.RowConstraints();
            rowRules.setPercentHeight(100.0 / 6);
            grid.getRowConstraints().add(rowRules);
        }

        LocalDate currentDate = firstOfMonth.minusDays(startCol); // Starting date for the 6x7 grid

        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = createDayCell(currentDate);
                grid.add(dayCell, col, row);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                
                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(2);
        cell.getStyleClass().add("calendar-cell");
        cell.setPadding(new Insets(4));
        
        if (!YearMonth.from(date).equals(currentYearMonth)) {
            cell.getStyleClass().add("calendar-cell-other-month");
        }
        if (date.isEqual(LocalDate.now())) {
            cell.getStyleClass().add("calendar-today");
        }

        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setStyle("-fx-font-weight: bold;");
        cell.getChildren().add(dateLabel);

        // Find tasks starting on this date
        List<Task> dailyTasks = taskList.stream()
                .filter(t -> t.getStartDate() != null && t.getStartDate().isEqual(date))
                .collect(Collectors.toList());

        VBox tasksBox = new VBox(2);
        for (Task task : dailyTasks) {
            Label taskLbl = new Label("• " + task.getTitle());
            taskLbl.setMaxWidth(Double.MAX_VALUE);
            taskLbl.setWrapText(false);
            
            // Apply styles based on completion
            if (task.isCompleted()) {
                taskLbl.getStyleClass().add("calendar-task-completed");
            } else {
                taskLbl.getStyleClass().add("calendar-task-pending");
            }
            
            String tooltipText = "Task: " + task.getTitle() + "\n" +
                                 "Môn học: " + task.getSubject() + "\n" +
                                 "Ngày bắt đầu: " + (task.getStartDate() != null ? task.getStartDate().toString() : "N/A") + "\n" +
                                 "Hạn chót: " + (task.getDueDate() != null ? task.getDueDate().toString() : "N/A") + "\n" +
                                 "Trạng thái: " + task.getStatus() + "\n" +
                                 "Ghi chú: " + (task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "Không có");
            
            Tooltip tt = new Tooltip(tooltipText);
            tt.setStyle("-fx-font-size: 12px;");
            Tooltip.install(taskLbl, tt);
            
            tasksBox.getChildren().add(taskLbl);
        }
        
        // Wrap tasks in scrollpane in case there are too many
        ScrollPane scroll = new ScrollPane(tasksBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        
        VBox.setVgrow(scroll, Priority.ALWAYS);
        cell.getChildren().add(scroll);

        return cell;
    }

    public CalendarView getView() {
        return view;
    }
}
