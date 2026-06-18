package org.example.model;

import java.time.LocalDate;

public class LotProduction {

    private int id;
    private LocalDate date;
    private String statutQualite;

    public LotProduction() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatutQualite() {
        return statutQualite;
    }

    public void setStatutQualite(String statutQualite) {
        this.statutQualite = statutQualite;
    }
}
