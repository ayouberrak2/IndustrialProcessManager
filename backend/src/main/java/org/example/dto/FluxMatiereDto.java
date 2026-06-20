package org.example.dto;

public class FluxMatiereDto {

    private int id;
    private Integer articleMatiereId;
    private String nomArticle;
    private String typeFlux;
    private float mesureCapteur;
    private float mesureDiametre;
    private String dateMesure;

    public FluxMatiereDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getArticleMatiereId() {
        return articleMatiereId;
    }

    public void setArticleMatiereId(Integer articleMatiereId) {
        this.articleMatiereId = articleMatiereId;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
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

    public String getDateMesure() {
        return dateMesure;
    }

    public void setDateMesure(String dateMesure) {
        this.dateMesure = dateMesure;
    }
}
