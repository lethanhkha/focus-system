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

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarController {
    private final CalendarView view;
    private final ObservableList<Task> taskList;
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;

    public CalendarController(ObservableList<Task> taskList) {
        this.taskList = taskList;
        this.view = new CalendarView();
        this.currentYearMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

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
            selectedDate = LocalDate.now();
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
        
        // Remove old days
        grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null);

        // Compute calendar days
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Sunday should map to column 0
        int startCol = firstOfMonth.getDayOfWeek().getValue() % 7;

        int totalCells = startCol + daysInMonth;
        int rowCount = (int) Math.ceil(totalCells / 7.0);

        // Clear existing row constraints to avoid duplicate accumulation
        grid.getRowConstraints().clear();
        
        // Create only the needed rows
        for (int i = 0; i < rowCount; i++) {
            javafx.scene.layout.RowConstraints rowRules = new javafx.scene.layout.RowConstraints();
            rowRules.setPercentHeight(100.0 / rowCount);
            grid.getRowConstraints().add(rowRules);
        }

        LocalDate currentDate = firstOfMonth.minusDays(startCol); // Starting date for the grid

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < 7; col++) {
                boolean isFirstCell = row == 0 && col == 0;
                VBox dayCell = createDayCell(currentDate, isFirstCell);
                grid.add(dayCell, col, row);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                
                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private VBox createDayCell(LocalDate date, boolean isFirstCell) {
        VBox cell = new VBox(2);
        cell.setPadding(new Insets(4));
        cell.getStyleClass().add("calendar-cell");
        if (date.equals(selectedDate)) {
            cell.getStyleClass().add("calendar-cell-selected");
        }
        if (!date.getMonth().equals(currentYearMonth.getMonth())) {
            cell.getStyleClass().add("out-of-month");
        }
        
        boolean showMonthLabel = isFirstCell || date.getDayOfMonth() == 1;
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d 'Thg' M");
        String dateText = showMonthLabel ? date.format(dayFormatter) : String.valueOf(date.getDayOfMonth());
        Label dateLabel = new Label(dateText);
        cell.getChildren().add(dateLabel);

        // Find tasks starting on this date
        List<Task> dailyTasks = taskList.stream()
                .filter(t -> t.getStartDate() != null && t.getStartDate().isEqual(date))
                .collect(Collectors.toList());

        VBox tasksBox = new VBox(2);
        for (Task task : dailyTasks) {
            Label taskLbl = new Label(task.getTitle());
            taskLbl.setMaxWidth(Double.MAX_VALUE);
            taskLbl.setWrapText(false);
            taskLbl.getStyleClass().add("calendar-task-badge");
            
            String tooltipText = "Task: " + task.getTitle() + "\n" +
                                 "Môn học: " + task.getSubject() + "\n" +
                                 "Ngày bắt đầu: " + (task.getStartDate() != null ? task.getStartDate().toString() : "N/A") + "\n" +
                                 "Hạn chót: " + (task.getDueDate() != null ? task.getDueDate().toString() : "N/A") + "\n" +
                                 "Trạng thái: " + task.getStatus() + "\n" +
                                 "Ghi chú: " + (task.getDescription() != null && !task.getDescription().isEmpty() ? task.getDescription() : "Không có");
            
            Tooltip tt = new Tooltip(tooltipText);
            Tooltip.install(taskLbl, tt);
            
            tasksBox.getChildren().add(taskLbl);
        }
        
        // Wrap tasks in scrollpane in case there are too many
        ScrollPane scroll = new ScrollPane(tasksBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        VBox.setVgrow(scroll, Priority.ALWAYS);
        cell.getChildren().add(scroll);

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            renderCalendar();
        });

        return cell;
    }

    public CalendarView getView() {
        return view;
    }
}
