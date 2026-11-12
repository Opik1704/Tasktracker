package com.site.webapp.repo;

import com.site.webapp.models.Team;
import com.site.webapp.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для TeamRepository.
 * <p>
 * Проверяют:
 * - поиск команд по=id owners (AC-4, AC-5)
 * - поиск команд, где пользователь является участником (AC-4, AC-5)
 * - поиск команды по id и ownerId (AC-5, AC-6)
 * - загрузку members через EntityGraph (AC-5, раздел 4 спецификации)
 * <p>
 * Зависимости: TeamRepository, UserRepository (существуют в проекте).
 * TeamRepository должен быть реализован как JpaRepository<Team, Long> с методами,
 * указанными в спецификации stage-1.md.
 */
@SpringBootTest
@Transactional
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User member;
    private User otherUser;

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

        member = new User();
        member.setFirstName("Member");
        member.setLastName("Memberov");
        member.setEmail("member@test.com");
        member.setPassword("password");
        member = userRepository.save(member);

        otherUser = new User();
        otherUser.setFirstName("Other");
        otherUser.setLastName("Otherov");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void findByOwnerId_returnsOnlyOwnedTeams() {
        Team team1 = new Team("Team One", owner.getId());
        Team team2 = new Team("Team Two", owner.getId());
        Team team3 = new Team("Team Three", otherUser.getId());
        teamRepository.save(team1);
        teamRepository.save(team2);
        teamRepository.save(team3);

        List<Team> owned = teamRepository.findByOwnerId(owner.getId());

        assertEquals(2, owned.size());
        assertTrue(owned.stream().allMatch(t -> t.getOwnerId().equals(owner.getId())));
    }

    @Test
    void findByOwnerId_emptyWhenNoTeams() {
        List<Team> result = teamRepository.findByOwnerId(owner.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    void findByMembersContaining_returnsTeamsWhereUserIsMember() {
        Team team1 = new Team("Team One", owner.getId());
        team1.addMember(owner);
        team1.addMember(member);
        teamRepository.save(team1);

        Team team2 = new Team("Team Two", owner.getId());
        team2.addMember(owner);
        teamRepository.save(team2);

        List<Team> memberTeams = teamRepository.findByMembersContaining(member);

        assertEquals(1, memberTeams.size());
        assertEquals("Team One", memberTeams.get(0).getName());
    }

    @Test
    void findByMembersContaining_emptyWhenNotAMember() {
        Team team1 = new Team("Team One", owner.getId());
        team1.addMember(owner);
        teamRepository.save(team1);

        List<Team> result = teamRepository.findByMembersContaining(otherUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdAndOwnerId_returnsTeamWhenOwnerMatches() {
        Team team = new Team("Team One", owner.getId());
        teamRepository.save(team);

        Optional<Team> found = teamRepository.findByIdAndOwnerId(team.getId(), owner.getId());

        assertTrue(found.isPresent());
        assertEquals(team.getId(), found.get().getId());
    }

    @Test
    void findByIdAndOwnerId_emptyWhenOwnerDoesNotMatch() {
        Team team = new Team("Team One", owner.getId());
        teamRepository.save(team);

        Optional<Team> found = teamRepository.findByIdAndOwnerId(team.getId(), otherUser.getId());

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByName_returnsTrueWhenNameExists() {
        Team team = new Team("Unique Team", owner.getId());
        teamRepository.save(team);

        assertTrue(teamRepository.existsByName("Unique Team"));
    }

    @Test
    void existsByName_returnsFalseWhenNameDoesNotExist() {
        assertFalse(teamRepository.existsByName("Nonexistent Team"));
    }

    /**
     * Проверяет загрузку members через EntityGraph/JOIN FETCH (раздел 4 спецификации).
     * Без загрузки members доступ к ним после закрытия сессии вызовет LazyInitializationException.
     */
    @Test
    void findWithMembersById_loadsMembersEagerly() {
        Team team = new Team("Team With Members", owner.getId());
        team.addMember(owner);
        team.addMember(member);
        teamRepository.save(team);

        Team loaded = teamRepository.findWithMembersById(team.getId())
                .orElseThrow(() -> new AssertionError("Team not found"));

        assertEquals(2, loaded.getMembers().size());
        assertTrue(loaded.getMembers().contains(owner));
        assertTrue(loaded.getMembers().contains(member));
    }

    @Test
    void findWithMembersById_emptyMembersWhenNoMembersAdded() {
        Team team = new Team("Empty Team", owner.getId());
        teamRepository.save(team);

        Team loaded = teamRepository.findWithMembersById(team.getId())
                .orElseThrow(() -> new AssertionError("Team not found"));

        assertEquals(0, loaded.getMembers().size());
    }
}
