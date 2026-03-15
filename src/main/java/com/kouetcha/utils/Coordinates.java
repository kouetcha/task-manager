package com.kouetcha.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public  class Coordinates {
    private int pageNum;
    private float x;
    private float y;
    private float width;
    private float height;

    public Coordinates(int pageNum, float x, float y, float width, float height) {
        this.pageNum = pageNum;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "Page: " + pageNum + ", x: " + x + ", y: " + y + ", Width: " + width + ", Height: " + height;
    }

}