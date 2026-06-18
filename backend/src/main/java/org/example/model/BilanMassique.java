package org.example.model;

import java.time.LocalDate;

public class BilanMassique {

    private int id;
    private float totalEntreesT;
    private float totalSortiesT;
    private float ecartPertesT;
    private float rendementVal;
    private LocalDate dateCalcul;

    public BilanMassique() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTotalEntreesT() {
        return totalEntreesT;
    }

    public void setTotalEntreesT(float totalEntreesT) {
        this.totalEntreesT = totalEntreesT;
    }

    public float getTotalSortiesT() {
        return totalSortiesT;
    }

    public void setTotalSortiesT(float totalSortiesT) {
        this.totalSortiesT = totalSortiesT;
    }

    public float getEcartPertesT() {
        return ecartPertesT;
    }

    public void setEcartPertesT(float ecartPertesT) {
        this.ecartPertesT = ecartPertesT;
    }

    public float getRendementVal() {
        return rendementVal;
    }

    public void setRendementVal(float rendementVal) {
        this.rendementVal = rendementVal;
    }

    public LocalDate getDateCalcul() {
        return dateCalcul;
    }

    public void setDateCalcul(LocalDate dateCalcul) {
        this.dateCalcul = dateCalcul;
    }
}
