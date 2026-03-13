package com.samson.restbackend.coverage.bluebox;

import com.samson.restbackend.config.JwtAuthFilter;
import com.samson.restbackend.config.SecurityConfig;
import com.samson.restbackend.controllers.AdminController;
import com.samson.restbackend.security.MethodSecurityConfig;
import com.samson.restbackend.services.AdminService;
import com.samson.restbackend.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, JwtAuthFilter.class})
class AdminSecurityBlueBoxTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessMaintenanceEndpoint() throws Exception {
        mockMvc.perform(post("/api/admin/rebuild-search-index"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void nonAdminIsForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/rebuild-search-index"))
                .andExpect(status().isForbidden());
    }
}
