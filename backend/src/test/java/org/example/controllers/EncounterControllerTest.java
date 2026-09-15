package org.example.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.daos.EncounterDao;
import org.example.models.Encounter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test for EncounterController - same shape and reasoning as
 * MonsterControllerTest (see that class for the full explanation of
 * @WithMockUser, the authorities = "ADMIN" requirement, and why fixture
 * usernames are limited to "user" and "admin"). Also covers /next-round,
 * which the Monster/PlayerCharacter controllers don't have an equivalent of.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EncounterDao encounterDao;

    private Encounter newEncounter(String name, boolean isPublic, String creatorUsername) {
        Encounter encounter = new Encounter(0, name,
                "An encounter created by EncounterControllerTest.", isPublic, creatorUsername, null, 1);
        return encounterDao.create(encounter);
    }

    @Nested
    class Authentication {

        @Test
        void anUnauthenticatedRequest_isRejectedWithoutReachingTheController() throws Exception {
            mockMvc.perform(get("/api/encounters"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Get {

        @Test
        @WithMockUser(username = "user")
        void aPublicEncounter_isVisibleToAnyLoggedInUser() throws Exception {
            Encounter encounter = newEncounter("ZZTest Public Encounter", true, "admin");

            mockMvc.perform(get("/api/encounters/" + encounter.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest Public Encounter"));
        }

        @Test
        @WithMockUser(username = "user")
        void anEncounterThatDoesNotExist_returns404() throws Exception {
            mockMvc.perform(get("/api/encounters/999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateEncounterOwnedBySomeoneElse_returns403() throws Exception {
            Encounter encounter = newEncounter("ZZTest Someones Secret Encounter", false, "admin");

            mockMvc.perform(get("/api/encounters/" + encounter.getId()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class Search {

        @Test
        @WithMockUser(username = "user")
        void returnsAPageOfResults_matchingTheGivenFilters() throws Exception {
            newEncounter("ZZTest Search Ambush", true, "user");

            mockMvc.perform(get("/api/encounters")
                            .param("name", "ZZTest Search")
                            .param("visibility", "all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").value(1))
                    .andExpect(jsonPath("$.items[0].name").value("ZZTest Search Ambush"));
        }
    }

    @Nested
    class Create {

        @Test
        @WithMockUser(username = "user")
        void createsTheEncounter_ignoringAnyCreatorUsernameInTheRequestBody() throws Exception {
            Map<String, Object> body = Map.of(
                    "name", "ZZTest New Encounter",
                    "description", "",
                    "public", true,
                    "creatorUsername", "admin",
                    "currentRound", 1
            );

            mockMvc.perform(post("/api/encounters")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("ZZTest New Encounter"))
                    .andExpect(jsonPath("$.creatorUsername").value("user"));
        }
    }

    @Nested
    class Update {

        @Test
        @WithMockUser(username = "user")
        void owner_canUpdateTheirOwnEncounter() throws Exception {
            Encounter encounter = newEncounter("ZZTest Before", false, "user");
            encounter.setName("ZZTest After");

            mockMvc.perform(put("/api/encounters/" + encounter.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(encounter)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest After"));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotUpdateSomeoneElsesEncounter() throws Exception {
            Encounter encounter = newEncounter("ZZTest Not Yours", false, "admin");

            mockMvc.perform(put("/api/encounters/" + encounter.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(encounter)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ADMIN")
        void anAdmin_canUpdateSomeoneElsesEncounter() throws Exception {
            Encounter encounter = newEncounter("ZZTest Admin Target", false, "user");
            encounter.setName("ZZTest Admin Edited");

            mockMvc.perform(put("/api/encounters/" + encounter.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(encounter)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("ZZTest Admin Edited"));
        }
    }

    @Nested
    class Delete {

        @Test
        @WithMockUser(username = "user")
        void owner_canDeleteTheirOwnEncounter() throws Exception {
            Encounter encounter = newEncounter("ZZTest Doomed", true, "user");

            mockMvc.perform(delete("/api/encounters/" + encounter.getId()))
                    .andExpect(status().isNoContent());

            assertNull(encounterDao.getEncounterById(encounter.getId()));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotDeleteSomeoneElsesEncounter() throws Exception {
            Encounter encounter = newEncounter("ZZTest Protected", true, "admin");

            mockMvc.perform(delete("/api/encounters/" + encounter.getId()))
                    .andExpect(status().isForbidden());

            assertNotNull(encounterDao.getEncounterById(encounter.getId()));
        }
    }

    @Nested
    class NextRound {

        @Test
        @WithMockUser(username = "user")
        void owner_advancingTheRound_incrementsCurrentRound() throws Exception {
            Encounter encounter = newEncounter("ZZTest Round Tracker", true, "user");

            mockMvc.perform(post("/api/encounters/" + encounter.getId() + "/next-round"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentRound").value(2));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotAdvanceSomeoneElsesEncounter() throws Exception {
            Encounter encounter = newEncounter("ZZTest Not Your Round", true, "admin");

            mockMvc.perform(post("/api/encounters/" + encounter.getId() + "/next-round"))
                    .andExpect(status().isForbidden());
        }
    }
}
