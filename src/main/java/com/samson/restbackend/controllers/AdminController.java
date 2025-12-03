package com.samson.restbackend.controllers;

import com.samson.restbackend.services.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/rebuild-search-index")
    @PreAuthorize("hasRole('ADMIN')")
    public void rebuildSearchIndex() {
        adminService.rebuildSearchIndex();
    }

}
