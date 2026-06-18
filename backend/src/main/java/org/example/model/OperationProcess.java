package org.example.model;

import java.time.LocalDate;

public class OperationProcess {

    private int id;
    private String numOrdreFab;
    private String typeOperation;
    private String statutOperation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String operateur;

    public OperationProcess() {
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

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getOperateur() {
        return operateur;
    }

    public void setOperateur(String operateur) {
        this.operateur = operateur;
    }
}
