package org.example.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardStatsDto {

    private int totalUsers;
    private int totalAdmins;
    private int totalChefsAtelier;
    private int totalTechniciensLabo;
    private int totalAteliers;
    private int totalEquipements;
    private int totalOperations;
    private int totalLots;
    private List<RecentOperationDto> recentOperations = new ArrayList<>();

    public DashboardStatsDto() {
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getTotalChefsAtelier() {
        return totalChefsAtelier;
    }

    public void setTotalChefsAtelier(int totalChefsAtelier) {
        this.totalChefsAtelier = totalChefsAtelier;
    }

    public int getTotalTechniciensLabo() {
        return totalTechniciensLabo;
    }

    public void setTotalTechniciensLabo(int totalTechniciensLabo) {
        this.totalTechniciensLabo = totalTechniciensLabo;
    }

    public int getTotalAteliers() {
        return totalAteliers;
    }

    public void setTotalAteliers(int totalAteliers) {
        this.totalAteliers = totalAteliers;
    }

    public int getTotalEquipements() {
        return totalEquipements;
    }

    public void setTotalEquipements(int totalEquipements) {
        this.totalEquipements = totalEquipements;
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

    public List<RecentOperationDto> getRecentOperations() {
        return recentOperations;
    }

    public void setRecentOperations(List<RecentOperationDto> recentOperations) {
        this.recentOperations = recentOperations;
    }
}
