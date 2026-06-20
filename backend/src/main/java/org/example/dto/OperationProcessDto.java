package org.example.dto;

import java.util.ArrayList;
import java.util.List;

public class OperationProcessDto {

    private int id;
    private String numOrdreFab;
    private String typeOperation;
    private String statutOperation;
    private String dateDebut;
    private String dateFin;
    private String operateur;
    private Integer equipementId;
    private String equipementName;
    private Integer dureeEstimee;
    private Integer entreeArticleMatiereId;
    private Float entreeMesureCapteur;
    private Float entreeMesureDiametre;
    private List<FluxMatiereDto> entreeFlux = new ArrayList<>();
    private Integer sortieArticleMatiereId;
    private Float sortieMesureCapteur;
    private Float sortieMesureDiametre;

    public OperationProcessDto() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumOrdreFab() {
        return numOrdreFab;
    }

    public void setNumOrdreFab(String numOrdreFab) {
        this.numOrdreFab = numOrdreFab;
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public void setTypeOperation(String typeOperation) {
        this.typeOperation = typeOperation;
    }

    public String getStatutOperation() {
        return statutOperation;
    }

    public void setStatutOperation(String statutOperation) {
        this.statutOperation = statutOperation;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getOperateur() {
        return operateur;
    }

    public void setOperateur(String operateur) {
        this.operateur = operateur;
    }

    public Integer getEquipementId() {
        return equipementId;
    }

    public void setEquipementId(Integer equipementId) {
        this.equipementId = equipementId;
    }

    public String getEquipementName() {
        return equipementName;
    }

    public void setEquipementName(String equipementName) {
        this.equipementName = equipementName;
    }

    public Integer getDureeEstimee() {
        return dureeEstimee;
    }

    public void setDureeEstimee(Integer dureeEstimee) {
        this.dureeEstimee = dureeEstimee;
    }

    public Integer getEntreeArticleMatiereId() {
        return entreeArticleMatiereId;
    }

    public void setEntreeArticleMatiereId(Integer entreeArticleMatiereId) {
        this.entreeArticleMatiereId = entreeArticleMatiereId;
    }

    public Float getEntreeMesureCapteur() {
        return entreeMesureCapteur;
    }

    public void setEntreeMesureCapteur(Float entreeMesureCapteur) {
        this.entreeMesureCapteur = entreeMesureCapteur;
    }

    public Float getEntreeMesureDiametre() {
        return entreeMesureDiametre;
    }

    public void setEntreeMesureDiametre(Float entreeMesureDiametre) {
        this.entreeMesureDiametre = entreeMesureDiametre;
    }

    public List<FluxMatiereDto> getEntreeFlux() {
        return entreeFlux;
    }

    public void setEntreeFlux(List<FluxMatiereDto> entreeFlux) {
        this.entreeFlux = entreeFlux;
    }

    public Integer getSortieArticleMatiereId() {
        return sortieArticleMatiereId;
    }

    public void setSortieArticleMatiereId(Integer sortieArticleMatiereId) {
        this.sortieArticleMatiereId = sortieArticleMatiereId;
    }

    public Float getSortieMesureCapteur() {
        return sortieMesureCapteur;
    }

    public void setSortieMesureCapteur(Float sortieMesureCapteur) {
        this.sortieMesureCapteur = sortieMesureCapteur;
    }

    public Float getSortieMesureDiametre() {
        return sortieMesureDiametre;
    }

    public void setSortieMesureDiametre(Float sortieMesureDiametre) {
        this.sortieMesureDiametre = sortieMesureDiametre;
    }
}
