package org.example;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import org.example.config.DatabaseMigration;
import org.example.controller.AdminAtelierController;
import org.example.controller.AdminDashboardController;
import org.example.controller.AdminUserController;
import org.example.controller.AuthController;
import org.example.controller.ChefArticleController;
import org.example.controller.ChefDashboardController;
import org.example.controller.ChefEquipementController;
import org.example.controller.ChefLotController;
import org.example.controller.ChefOperationController;
import org.example.controller.TechnicienLaboController;
import org.example.repository.AtelierRepository;
import org.example.repository.DashboardRepository;
import org.example.repository.UserRepository;
import org.example.service.AdminUserService;
import org.example.service.AtelierService;
import org.example.service.AuthService;
import org.example.service.ChefDashboardService;
import org.example.service.DashboardService;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8090;

        DatabaseMigration databaseMigration = new DatabaseMigration();
        databaseMigration.run();

        UserRepository userRepository = new UserRepository();
        AtelierRepository atelierRepository = new AtelierRepository();
        DashboardRepository dashboardRepository = new DashboardRepository();

        AuthService authService = new AuthService(userRepository);
        AdminUserService adminUserService = new AdminUserService(userRepository);
        AtelierService atelierService = new AtelierService(atelierRepository);
        DashboardService dashboardService = new DashboardService(
                userRepository,
                atelierRepository,
                dashboardRepository
        );
        ChefDashboardService chefDashboardService = new ChefDashboardService(
                userRepository,
                dashboardRepository
        );

        AuthController authController = new AuthController(authService);
        AdminUserController adminUserController = new AdminUserController(adminUserService);
        AdminAtelierController adminAtelierController = new AdminAtelierController(atelierService);
        AdminDashboardController adminDashboardController = new AdminDashboardController(dashboardService);
        ChefDashboardController chefDashboardController = new ChefDashboardController(chefDashboardService);
        ChefEquipementController chefEquipementController = new ChefEquipementController();
        ChefOperationController chefOperationController = new ChefOperationController();
        ChefLotController chefLotController = new ChefLotController();
        ChefArticleController chefArticleController = new ChefArticleController();
        TechnicienLaboController technicienLaboController = new TechnicienLaboController();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/auth/login", authController);
        server.createContext("/api/admin/users", adminUserController);
        server.createContext("/api/admin/ateliers", adminAtelierController);
        server.createContext("/api/admin/dashboard", adminDashboardController);
        server.createContext("/api/chef/dashboard", chefDashboardController);
        server.createContext("/api/chef/equipements", chefEquipementController);
        server.createContext("/api/chef/operations", chefOperationController);
        server.createContext("/api/chef/lots", chefLotController);
        server.createContext("/api/chef/articles", chefArticleController);
        server.createContext("/api/labo", technicienLaboController);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("API demarree sur http://localhost:" + port);
        System.out.println("Endpoint : POST /api/auth/login");
        System.out.println("Admin : /api/admin/dashboard, /api/admin/users, /api/admin/ateliers");
        System.out.println("Chef atelier : /api/chef/dashboard, /api/chef/equipements, /api/chef/operations, /api/chef/lots");
        System.out.println("Technicien labo : /api/labo/operations, /api/labo/analyses");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }
}
