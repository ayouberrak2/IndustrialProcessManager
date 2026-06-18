package org.example.service;

import org.example.dto.DashboardStatsDto;
import org.example.repository.AtelierRepository;
import org.example.repository.DashboardRepository;
import org.example.repository.UserRepository;

public class DashboardService {

    private final UserRepository userRepository;
    private final AtelierRepository atelierRepository;
    private final DashboardRepository dashboardRepository;

    public DashboardService(
            UserRepository userRepository,
            AtelierRepository atelierRepository,
            DashboardRepository dashboardRepository
    ) {
        this.userRepository = userRepository;
        this.atelierRepository = atelierRepository;
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalUsers(userRepository.countAll());
        stats.setTotalAdmins(userRepository.countByRole("ADMIN"));
        stats.setTotalChefsAtelier(userRepository.countByRole("CHEF_ATELIER"));
        stats.setTotalTechniciensLabo(userRepository.countByRole("TECHNICIEN_LABO"));
        stats.setTotalAteliers(atelierRepository.countAll());
        stats.setTotalEquipements(dashboardRepository.countEquipements());
        stats.setTotalOperations(dashboardRepository.countOperations());
        stats.setTotalLots(dashboardRepository.countLots());
        stats.setRecentOperations(dashboardRepository.findRecentOperations());

        return stats;
    }
}
