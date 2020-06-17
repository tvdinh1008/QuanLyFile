package com.example.quanlyfile.model;

public class itemModel {
    String name;
    String lastModified;
    int countFile;
    double sizeFile;
    int properties;
    /*
    0 là thư mục
    1 là file
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProperties() {
        return properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getCountFile() {
        return countFile;
    }

    public void setCountFile(int countFile) {
        this.countFile = countFile;
    }

    public double getSizeFile() {
        return sizeFile;
    }

    public void setSizeFile(double sizeFile) {
        this.sizeFile = sizeFile;
    }
}
