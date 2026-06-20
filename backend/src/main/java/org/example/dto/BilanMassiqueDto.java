package org.example.dto;

public class BilanMassiqueDto {

    private int id;
    private float totalEntreesT;
    private float totalSortiesT;
    private float ecartPertesT;
    private float rendementVal;
    private String dateCalcul;

    public BilanMassiqueDto() {
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

    public String getDateCalcul() {
        return dateCalcul;
    }

    public void setDateCalcul(String dateCalcul) {
        this.dateCalcul = dateCalcul;
    }
}
