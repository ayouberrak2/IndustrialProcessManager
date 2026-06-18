package org.example.model;

import java.time.LocalDate;

public class AnalyseLaboratoire {

    private int id;
    private float tauxP2O5;
    private float tauxCadmiumPpm;
    private float solidesSuspendu;
    private LocalDate dateAnalyse;

    public AnalyseLaboratoire() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTauxP2O5() {
        return tauxP2O5;
    }

    public void setTauxP2O5(float tauxP2O5) {
        this.tauxP2O5 = tauxP2O5;
    }

    public float getTauxCadmiumPpm() {
        return tauxCadmiumPpm;
    }

    public void setTauxCadmiumPpm(float tauxCadmiumPpm) {
        this.tauxCadmiumPpm = tauxCadmiumPpm;
    }

    public float getSolidesSuspendu() {
        return solidesSuspendu;
    }

    public void setSolidesSuspendu(float solidesSuspendu) {
        this.solidesSuspendu = solidesSuspendu;
    }

    public LocalDate getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(LocalDate dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }
}
