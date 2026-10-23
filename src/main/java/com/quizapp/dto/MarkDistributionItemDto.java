package com.quizapp.dto;

public class MarkDistributionItemDto {
    private int marks;
    private int count;

    public MarkDistributionItemDto() {}

    public MarkDistributionItemDto(int marks, int count) {
        this.marks = marks;
        this.count = count;
    }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
