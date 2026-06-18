package org.example.model;

public class OperationEquipement {

    private int id;
    private int operationProcessId;
    private int equipementId;
    private int dureeEstimee;

    public OperationEquipement() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOperationProcessId() {
        return operationProcessId;
    }

    public void setOperationProcessId(int operationProcessId) {
        this.operationProcessId = operationProcessId;
    }

    public int getEquipementId() {
        return equipementId;
    }

    public void setEquipementId(int equipementId) {
        this.equipementId = equipementId;
    }

    public int getDureeEstimee() {
        return dureeEstimee;
    }

    public void setDureeEstimee(int dureeEstimee) {
        this.dureeEstimee = dureeEstimee;
    }
}
