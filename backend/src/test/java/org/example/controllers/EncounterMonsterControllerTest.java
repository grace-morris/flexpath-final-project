package org.example.controllers;

import org.example.daos.EncounterDao;
import org.example.daos.EncounterMonsterDao;
import org.example.daos.MonsterDao;
import org.example.models.Encounter;
import org.example.models.EncounterMonster;
import org.example.models.Monster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test for EncounterMonsterController - see MonsterControllerTest
 * for the general @WithMockUser / fixture-username explanation.
 *
 * The most important thing this file proves is the visibility fix made to
 * EncounterMonsterService.getMonsterList(): before that fix, GET on this
 * endpoint had no ownership or visibility check at all, so a logged-in
 * stranger could read the combatants of *any* encounter - including a
 * private one - just by knowing its id. The GetMonsterList nested class
 * below is what that fix is verified against, end-to-end over real HTTP.
 *
 * Every other action here (add/updateHealth/remove/etc.) uses canModify(),
 * not canView() - those require ownership (or admin), not just visibility,
 * so a stranger is rejected even on a *public* encounter.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EncounterMonsterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private MonsterDao monsterDao;
    @Autowired
    private EncounterMonsterDao encounterMonsterDao;

    private int goblinId;

    @BeforeEach
    void setUp() {
        Monster goblin = monsterDao.create(new Monster(0, "ZZTest Fixture Goblin", "Humanoid", 7, 0.25, 15,
                "", true, "user", null, 0));
        goblinId = goblin.getId();
    }

    private Encounter newEncounter(boolean isPublic, String creatorUsername) {
        return encounterDao.create(new Encounter(0, "ZZTest Fixture Encounter", "", isPublic, creatorUsername, null, 1));
    }

    @Nested
    class GetMonsterList {

        @Test
        @WithMockUser(username = "user")
        void aPublicEncountersMonsterList_isVisibleToAnyLoggedInUser() throws Exception {
            Encounter encounter = newEncounter(true, "admin");
            encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/monsters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateEncountersMonsterList_isRejectedForAStranger() throws Exception {
            Encounter encounter = newEncounter(false, "admin");
            encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/monsters"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "user")
        void aPrivateEncountersMonsterList_isVisibleToItsOwner() throws Exception {
            Encounter encounter = newEncounter(false, "user");
            encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/monsters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ADMIN")
        void aPrivateEncountersMonsterList_isVisibleToAnAdmin() throws Exception {
            Encounter encounter = newEncounter(false, "user");
            encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(get("/api/encounters/" + encounter.getId() + "/monsters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    class Add {

        @Test
        @WithMockUser(username = "user")
        void owner_canAddAMonsterToTheirOwnEncounter() throws Exception {
            Encounter encounter = newEncounter(true, "user");

            mockMvc.perform(post("/api/encounters/" + encounter.getId() + "/monsters/" + goblinId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.monsterName").value("ZZTest Fixture Goblin"));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotAddAMonster_evenToAPublicEncounter() throws Exception {
            Encounter encounter = newEncounter(true, "admin");

            mockMvc.perform(post("/api/encounters/" + encounter.getId() + "/monsters/" + goblinId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateHealth {

        @Test
        @WithMockUser(username = "user")
        void owner_canUpdateAMonstersHealth() throws Exception {
            Encounter encounter = newEncounter(true, "user");
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(patch("/api/encounters/" + encounter.getId() + "/monsters/" + added.getId() + "/health")
                            .contentType("application/json")
                            .content("{\"currentHealth\": 3}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentHealth").value(3));
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotUpdateHealth() throws Exception {
            Encounter encounter = newEncounter(true, "admin");
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(patch("/api/encounters/" + encounter.getId() + "/monsters/" + added.getId() + "/health")
                            .contentType("application/json")
                            .content("{\"currentHealth\": 3}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class Remove {

        @Test
        @WithMockUser(username = "user")
        void owner_canRemoveAMonsterFromTheirOwnEncounter() throws Exception {
            Encounter encounter = newEncounter(true, "user");
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(delete("/api/encounters/" + encounter.getId() + "/monsters/" + added.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "user")
        void nonOwnerNonAdmin_cannotRemoveAMonster() throws Exception {
            Encounter encounter = newEncounter(true, "admin");
            EncounterMonster added = encounterMonsterDao.addMonsterToEncounter(encounter.getId(), goblinId);

            mockMvc.perform(delete("/api/encounters/" + encounter.getId() + "/monsters/" + added.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
