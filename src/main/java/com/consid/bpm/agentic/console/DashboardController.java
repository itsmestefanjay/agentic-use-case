package com.consid.bpm.agentic.console;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class DashboardController {

    private final DashboardService dashboardService;

    DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardService.load());
        return "dashboard";
    }
}
