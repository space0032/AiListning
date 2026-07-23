package com.ailisting.controller;

import com.ailisting.exception.GlobalExceptionHandler;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Role;
import com.ailisting.repository.AiGenerationLogRepository;
import com.ailisting.repository.ListingRepository;
import com.ailisting.repository.UserRepository;
import com.ailisting.security.JwtAuthenticationFilter;
import com.ailisting.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ailisting.config.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private AiGenerationLogRepository generationLogRepository;

    @InjectMocks
    private AdminController adminController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User adminUser;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        adminUser = createAdminUser();

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken("admin", null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getAllUsers_Admin_ReturnsOk() throws Exception {
        User user = createRegularUser();
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("user1"));
    }

    @Test
    void getUserById_Admin_ReturnsOk() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void toggleUserStatus_Admin_ReturnsOk() throws Exception {
        User user = createRegularUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/admin/users/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAnalyticsOverview_Admin_ReturnsOk() throws Exception {
        when(userRepository.count()).thenReturn(10L);
        when(listingRepository.count()).thenReturn(50L);
        when(userRepository.countByEnabledTrue()).thenReturn(8L);
        when(generationLogRepository.countAllSince(any(LocalDateTime.class)))
                .thenReturn(100L);

        mockMvc.perform(get("/admin/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(10))
                .andExpect(jsonPath("$.data.activeUsers").value(8))
                .andExpect(jsonPath("$.data.aiGenerations").value(100));
    }

    @Test
    void getGenerationStats_Admin_ReturnsOk() throws Exception {
        when(generationLogRepository.countAllSince(any(LocalDateTime.class)))
                .thenReturn(10L);
        when(generationLogRepository.countSuccessfulAllSince(any(LocalDateTime.class)))
                .thenReturn(8L);
        when(generationLogRepository.avgGenerationTimeAll())
                .thenReturn(1500.0);
        when(generationLogRepository.countByPlatformAll())
                .thenReturn(List.of());
        when(generationLogRepository.countByDaySince(any(LocalDateTime.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/analytics/generation-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalGenerations").value(10));
    }

    @Test
    void getDetailedHealth_Admin_ReturnsOk() throws Exception {
        when(userRepository.count()).thenReturn(10L);

        mockMvc.perform(get("/admin/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}