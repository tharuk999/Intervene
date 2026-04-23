package com.interactout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interactout.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InteractOutIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void getAllProfiles_returnsSeededData() throws Exception {
        mvc.perform(get("/api/profiles"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").isArray())
           .andExpect(jsonPath("$[0].packageName").exists());
    }

    @Test
    void saveAndRetrieveProfile() throws Exception {
        AppProfileEntity profile = new AppProfileEntity();
        profile.setPackageName("com.test.app");
        profile.setAppName("Test App");
        profile.setEnabled(true);
        profile.setDailyLimitMs(3_600_000L);

        InterventionEntity tapDelay = new InterventionEntity();
        tapDelay.setType(InterventionType.TAP_DELAY);
        tapDelay.setEnabled(true);
        tapDelay.setIntensity(0.5f);
        profile.syncInterventions(List.of(tapDelay));

        mvc.perform(post("/api/profiles")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(profile)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.packageName").value("com.test.app"));

        mvc.perform(get("/api/profiles/com.test.app"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.appName").value("Test App"))
           .andExpect(jsonPath("$.dailyLimitMs").value(3_600_000));
    }

    @Test
    void toggleEnabled() throws Exception {
        AppProfileEntity p = new AppProfileEntity();
        p.setPackageName("com.toggle.test");
        p.setAppName("Toggle Test");
        p.setEnabled(true);
        p.setDailyLimitMs(0L);
        mvc.perform(post("/api/profiles")
               .contentType(MediaType.APPLICATION_JSON)
               .content(mapper.writeValueAsString(p)))
           .andExpect(status().isOk());

        mvc.perform(put("/api/profiles/com.toggle.test/enabled")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"enabled\": false}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void updateIntensity() throws Exception {
        mvc.perform(put("/api/profiles/com.instagram.android/interventions/TAP_DELAY/intensity")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"intensity\": 0.75}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.packageName").value("com.instagram.android"));
    }

    @Test
    void recordSessionAndGetUsage() throws Exception {
        String body = """
            {"packageName":"com.instagram.android","durationMs":1800000,"timestampMs":%d}
            """.formatted(System.currentTimeMillis());

        mvc.perform(post("/api/usage/session")
               .contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.packageName").value("com.instagram.android"));

        mvc.perform(get("/api/usage/today/com.instagram.android"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.usedMs").isNumber())
           .andExpect(jsonPath("$.bypassActive").value(false));
    }

    @Test
    void grantBypassAndCheck() throws Exception {
        mvc.perform(post("/api/usage/bypass")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"packageName\":\"com.instagram.android\",\"durationMs\":60000}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("granted"));

        mvc.perform(get("/api/usage/bypass/com.instagram.android"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getDashboard() throws Exception {
        mvc.perform(get("/api/usage/dashboard"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.totalScreenTimeMs").isNumber())
           .andExpect(jsonPath("$.resistedUrges").isNumber())
           .andExpect(jsonPath("$.activeFrictionApps").isNumber())
           .andExpect(jsonPath("$.percentChangeVsYesterday").isNumber())
           .andExpect(jsonPath("$.dailyBreakdown").exists());
    }
}
