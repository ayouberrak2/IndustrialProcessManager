package org.example.dto;

import java.util.ArrayList;
import java.util.List;

public class ChefDashboardDto {

    private int totalEquipements;
    private int totalTechniciensLabo;
    private int totalOperations;
    private int totalLots;
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
