package org.example.dto;

public class RecentOperationDto {

    private int id;
    private String numOrdreFab;
    private String typeOperation;
    private String statutOperation;
    private String dateDebut;
    private String dateFin;

    public RecentOperationDto() {
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
}
