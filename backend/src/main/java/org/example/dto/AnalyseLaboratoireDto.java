package org.example.dto;

public class AnalyseLaboratoireDto {

    private int id;
    private Integer lotProductionId;
    private float tauxP2O5;
    private float tauxCadmiumPpm;
    private float solidesSuspendu;
    private String dateAnalyse;

    public AnalyseLaboratoireDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getLotProductionId() {
        return lotProductionId;
    }

    public void setLotProductionId(Integer lotProductionId) {
        this.lotProductionId = lotProductionId;
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

    public String getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(String dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }
}
