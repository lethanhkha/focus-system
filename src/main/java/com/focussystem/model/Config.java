package com.focussystem.model;

public class Config {
    private String lastSelectedSemester;

    public Config(String lastSelectedSemester) {
        this.lastSelectedSemester = lastSelectedSemester;
    }

    public String getLastSelectedSemester() { return lastSelectedSemester; }
    public void setLastSelectedSemester(String lastSelectedSemester) { this.lastSelectedSemester = lastSelectedSemester; }
}