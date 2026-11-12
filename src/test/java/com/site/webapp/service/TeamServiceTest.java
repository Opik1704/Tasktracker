package com.site.webapp.service;

import com.site.webapp.models.Team;
import com.site.webapp.models.User;
import com.site.webapp.repo.TeamRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для TeamService.
 * <p>
 * Проверяют:
 * - автоматическое добавление владельца в members при создании команды (AC-2)
 * - получение команды по ID с загруженными members (AC-5)
 * - получение списка команд, где пользователь участник (AC-4)
 * - проверку участия пользователя в команде (AC-6)
 * <p>
 * Зависимости: TeamService, TeamRepository, UserRepository (реализуются на следующем этапе).
 */
@SpringBootTest
@Transactional
class TeamServiceTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private CustomUserDetails ownerDetails;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setFirstName("Owner");
        owner.setLastName("Ownerov");
        owner.setEmail("owner@test.com");
        owner.setPassword("password");
        owner = userRepository.save(owner);
        ownerDetails = new CustomUserDetails(owner);
    }

    @Test
    void createTeam_setsOwnerAndAddsOwnerToMembers() {
        Team team = new Team("My Team", null);

        Team created = teamService.createTeam(team, ownerDetails);

        assertNotNull(created.getId());
        assertEquals(owner.getId(), created.getOwnerId());
        assertTrue(created.getMembers().contains(owner));
    }

    @Test
    void getTeamById_loadsMembers() {
        Team team = new Team("Team With Members", owner.getId());
        team.addMember(owner);
        teamService.createTeam(team, ownerDetails);

        Team loaded = teamService.getTeamById(team.getId());

        assertEquals(team.getId(), loaded.getId());
        assertEquals(owner.getId(), loaded.getOwnerId());
        assertTrue(loaded.getMembers().contains(owner));
    }

    @Test
    void getUserTeams_returnsTeamsWhereUserIsMember() {
        Team team1 = new Team("Team One", owner.getId());
        teamService.createTeam(team1, ownerDetails);

        Team team2 = new Team("Team Two", owner.getId());
        teamService.createTeam(team2, ownerDetails);

        List<Team> userTeams = teamService.getUserTeams(owner.getId());

        assertEquals(2, userTeams.size());
    }

    @Test
    void isMember_returnsTrueWhenUserIsMember() {
        Team team = new Team("My Team", owner.getId());
        teamService.createTeam(team, ownerDetails);

        assertTrue(teamService.isMember(team.getId(), owner.getId()));
    }

    @Test
    void isMember_returnsFalseWhenUserIsNotMember() {
        User other = new User();
        other.setFirstName("Other");
        other.setLastName("Otherov");
        other.setEmail("other@test.com");
        other.setPassword("password");
        other = userRepository.save(other);

        Team team = new Team("My Team", owner.getId());
        teamService.createTeam(team, ownerDetails);

        assertFalse(teamService.isMember(team.getId(), other.getId()));
    }
}