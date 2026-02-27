package com.hsbc.ci.engine.core.model;

public class Gate {

    private String type;
    private Integer min;
    private Integer maxCritical;
    private Integer maxHigh;
    private String qualityGate;
    private String rating;

    public Gate() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMaxCritical() {
        return maxCritical;
    }

    public void setMaxCritical(Integer maxCritical) {
        this.maxCritical = maxCritical;
    }

    public Integer getMaxHigh() {
        return maxHigh;
    }

    public void setMaxHigh(Integer maxHigh) {
        this.maxHigh = maxHigh;
    }

    public String getQualityGate() {
        return qualityGate;
    }

    public void setQualityGate(String qualityGate) {
        this.qualityGate = qualityGate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
