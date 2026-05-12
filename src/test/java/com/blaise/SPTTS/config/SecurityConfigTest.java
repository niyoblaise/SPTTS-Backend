package com.blaise.SPTTS.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicRoutes_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/routes")).andExpect(status().isOk());
    }

    @Test
    void ruraEndpoints_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/rura/incidents")).andExpect(status().isOk());
        mockMvc.perform(get("/rura/stats")).andExpect(status().isOk());
        mockMvc.perform(get("/rura/reports/monthly")).andExpect(status().isOk());
    }

    @Test
    void adminUsersEndpoint_ShouldRequireAuth() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminReportsEndpoint_ShouldRequireAuth() throws Exception {
        mockMvc.perform(get("/admin/reports/summary")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminDashboardEndpoint_ShouldRequireAuth() throws Exception {
        mockMvc.perform(get("/admin/dashboard/stats")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void adminEndpoints_ShouldBeAccessibleWithAdminRole() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/reports/summary")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/dashboard/stats")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void adminEndpoints_ShouldBeForbiddenForPassenger() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void busManagement_ShouldBeAccessibleForAdmin() throws Exception {
        mockMvc.perform(post("/buses")).andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void busCreation_ShouldBeForbiddenForPassenger() throws Exception {
        mockMvc.perform(post("/buses")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void unsupportedMethod_ShouldReturn405() throws Exception {
        mockMvc.perform(delete("/rura/incidents")).andExpect(status().is4xxClientError());
    }
}
