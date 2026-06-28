package org.example.dto;

import java.util.ArrayList;
import java.util.List;

public class ChefDashboardDto {

    private int totalEquipements;
    private int totalTechniciensLabo;
    private int totalOperations;
    private int totalLots;
    private int equipementsEnPanne;
    private int operationsEnCours;
    private int lotsNonConformes;
    private double totalFluxEntree;
    private double totalFluxSortie;
    private RecentOperationDto activeOperation;
    private List<RecentOperationDto> recentOperations = new ArrayList<>();

    public ChefDashboardDto() {
    }

    public int getTotalEquipements() {
        return totalEquipements;
    }

    public void setTotalEquipements(int totalEquipements) {
        this.totalEquipements = totalEquipements;
    }

    public int getTotalTechniciensLabo() {
        return totalTechniciensLabo;
    }

    public void setTotalTechniciensLabo(int totalTechniciensLabo) {
        this.totalTechniciensLabo = totalTechniciensLabo;
    }

    public int getTotalOperations() {
        return totalOperations;
    }

    public void setTotalOperations(int totalOperations) {
        this.totalOperations = totalOperations;
    }

    public int getTotalLots() {
        return totalLots;
    }

    public void setTotalLots(int totalLots) {
        this.totalLots = totalLots;
    }

    public int getEquipementsEnPanne() {
        return equipementsEnPanne;
    }

    public void setEquipementsEnPanne(int equipementsEnPanne) {
        this.equipementsEnPanne = equipementsEnPanne;
    }

    public int getOperationsEnCours() {
        return operationsEnCours;
    }

    public void setOperationsEnCours(int operationsEnCours) {
        this.operationsEnCours = operationsEnCours;
    }

    public int getLotsNonConformes() {
        return lotsNonConformes;
    }

    public void setLotsNonConformes(int lotsNonConformes) {
        this.lotsNonConformes = lotsNonConformes;
    }

    public double getTotalFluxEntree() {
        return totalFluxEntree;
    }

    public void setTotalFluxEntree(double totalFluxEntree) {
        this.totalFluxEntree = totalFluxEntree;
    }

    public double getTotalFluxSortie() {
        return totalFluxSortie;
    }

    public void setTotalFluxSortie(double totalFluxSortie) {
        this.totalFluxSortie = totalFluxSortie;
    }

    public RecentOperationDto getActiveOperation() {
        return activeOperation;
    }

    public void setActiveOperation(RecentOperationDto activeOperation) {
        this.activeOperation = activeOperation;
    }

    public List<RecentOperationDto> getRecentOperations() {
        return recentOperations;
    }

    public void setRecentOperations(List<RecentOperationDto> recentOperations) {
        this.recentOperations = recentOperations;
    }
}
