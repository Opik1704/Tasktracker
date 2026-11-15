package com.site.webapp.service;

import com.site.webapp.exception.team.TeamNotFoundException;
import com.site.webapp.models.Team;
import com.site.webapp.models.User;
import com.site.webapp.repo.TeamRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository, AuditLogService auditLogService) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public Team createTeam(Team team, CustomUserDetails userDetails) {
        team.setOwnerId(userDetails.getId());
        User owner = userRepository.findById(userDetails.getId()).orElseThrow();
        team.addMember(owner);
        Team saved = teamRepository.save(team);

        auditLogService.record(userDetails.getId(), userDetails.getUsername(),
                AuditLogService.ACTION_TEAM_CREATED, AuditLogService.ENTITY_TEAM, saved.getId(),
                "Создана команда '" + saved.getName() + "'");

        return saved;
    }

    @Transactional(readOnly = true)
    public Team getTeamById(Long id) {
        return teamRepository.findWithMembersById(id)
                .orElseThrow(() -> new TeamNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Team> getUserTeams(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return teamRepository.findByMembersContaining(user);
    }

    @Transactional(readOnly = true)
    public boolean isMember(Long teamId, Long userId) {
        Team team = teamRepository.findWithMembersById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
        return team.isMember(userId);
    }
}
