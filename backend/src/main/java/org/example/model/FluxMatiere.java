package org.example.model;

import java.time.LocalDate;

public class FluxMatiere {

    private int id;
    private String typeFlux;
    private float mesureCapteur;
    private float mesureDiametre;
    private LocalDate dateMesure;

    public FluxMatiere() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeFlux() {
        return typeFlux;
    }

    public void setTypeFlux(String typeFlux) {
        this.typeFlux = typeFlux;
    }

    public float getMesureCapteur() {
        return mesureCapteur;
    }

    public void setMesureCapteur(float mesureCapteur) {
        this.mesureCapteur = mesureCapteur;
    }

    public float getMesureDiametre() {
        return mesureDiametre;
    }

    public void setMesureDiametre(float mesureDiametre) {
        this.mesureDiametre = mesureDiametre;
    }

    public LocalDate getDateMesure() {
        return dateMesure;
    }

    public void setDateMesure(LocalDate dateMesure) {
        this.dateMesure = dateMesure;
    }
}
