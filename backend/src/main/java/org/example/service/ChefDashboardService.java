package org.example.service;

import org.example.dto.ChefDashboardDto;
import org.example.repository.DashboardRepository;
import org.example.repository.UserRepository;

public class ChefDashboardService {

    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;

    public ChefDashboardService(
            UserRepository userRepository,
            DashboardRepository dashboardRepository
    ) {
        this.userRepository = userRepository;
        this.dashboardRepository = dashboardRepository;
    }

    public ChefDashboardDto getDashboard(int atelierId) {
        ChefDashboardDto dashboard = new ChefDashboardDto();

        dashboard.setTotalEquipements(dashboardRepository.countEquipementsByAtelier(atelierId));
        dashboard.setTotalTechniciensLabo(userRepository.countByRoleAndAtelier("TECHNICIEN_LABO", atelierId));
        dashboard.setTotalOperations(dashboardRepository.countOperationsByAtelier(atelierId));
        dashboard.setTotalLots(dashboardRepository.countLotsByAtelier(atelierId));
        dashboard.setEquipementsEnPanne(dashboardRepository.countEquipementsEnPanneByAtelier(atelierId));
        dashboard.setOperationsEnCours(dashboardRepository.countOperationsEnCoursByAtelier(atelierId));
        dashboard.setLotsNonConformes(dashboardRepository.countLotsNonConformesByAtelier(atelierId));
        dashboard.setTotalFluxEntree(dashboardRepository.sumFluxByAtelierAndType(atelierId, "ENTREE"));
        dashboard.setTotalFluxSortie(dashboardRepository.sumFluxByAtelierAndType(atelierId, "SORTIE"));
        dashboard.setActiveOperation(dashboardRepository.findActiveOperationByAtelier(atelierId));
        dashboard.setRecentOperations(dashboardRepository.findRecentOperationsByAtelier(atelierId));

        return dashboard;
    }
}
