package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.dto.crud.TeamProfileAddDto;
import io.github.isuru.oasis.services.dto.crud.TeamScopeAddDto;
import io.github.isuru.oasis.services.dto.crud.UserProfileAddDto;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author iweerarathna
 */
class ApiProfileTest extends AbstractApiTest {

    @Test
    void testUsers() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        UserProfileAddDto user1 = new UserProfileAddDto();
        user1.setName("Arnold Weber");
        user1.setMale(true);
        user1.setEmail("arnold@westworld.com");
        user1.setAvatarId("arnold32.png");
        user1.setExtId(10001L);
        long id = profileService.addUserProfile(user1);

        Assertions.assertTrue(id > 0);
        UserProfile profile = profileService.readUserProfile(id);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(user1.getName(), profile.getName());
        Assertions.assertEquals(user1.getAvatarId(), profile.getAvatarId());
        Assertions.assertEquals(user1.getEmail(), profile.getEmail());
        Assertions.assertEquals(user1.isMale(), profile.isMale());
        Assertions.assertEquals(user1.getExtId(), profile.getExtId());
        Assertions.assertTrue(profile.isActive());

        profile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(user1.getName(), profile.getName());
        Assertions.assertEquals(user1.getAvatarId(), profile.getAvatarId());
        Assertions.assertEquals(user1.getEmail(), profile.getEmail());
        Assertions.assertEquals(user1.isMale(), profile.isMale());
        Assertions.assertEquals(user1.getExtId(), profile.getExtId());
        Assertions.assertTrue(profile.isActive());

        UserProfile arnoldProfile = profileService.readUserProfile("arnold@westworld.com");
        Assertions.assertNotNull(arnoldProfile);
        Assertions.assertEquals(arnoldProfile.getName(), user1.getName());
        Assertions.assertEquals(arnoldProfile.getAvatarId(), user1.getAvatarId());
        Assertions.assertEquals(arnoldProfile.getEmail(), user1.getEmail());
        Assertions.assertEquals(arnoldProfile.getId(), id);

        profile.setName("Bernard Lowe");
        ////Assertions.assertTrue(profileService.editUserProfile(profile.getId(), profile));

        UserProfile tmpProfile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertEquals(profile.getName(), tmpProfile.getName());

        profile.setAvatarId("bernard.png");
        profile.setMale(false);
        ////Assertions.assertTrue(profileService.editUserProfile(profile.getId(), profile));

        List<UserProfile> searchRes = profileService.findUser("arn", null);
        Assertions.assertEquals(0, searchRes.size());
        searchRes = profileService.findUser("arno", null);
        Assertions.assertEquals(1, searchRes.size());
        searchRes = profileService.findUser("arno%", null);
        Assertions.assertEquals(0, searchRes.size());

        tmpProfile = profileService.readUserProfileByExtId(10001L);
        Assertions.assertFalse(tmpProfile.isMale());
        Assertions.assertEquals(profile.getAvatarId(), tmpProfile.getAvatarId());

        Assertions.assertTrue(profileService.deleteUserProfile(profile.getId()));
        tmpProfile = profileService.readUserProfile(profile.getId());

        Assertions.assertNotNull(tmpProfile);
        Assertions.assertFalse(tmpProfile.isActive());

        // after deletion there should be no users
        searchRes = profileService.findUser("arno", null);
        Assertions.assertEquals(0, searchRes.size());
    }

    @Test
    void testTeamCrud() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        TeamScopeAddDto scope = new TeamScopeAddDto();
        scope.setName("Finance");
        scope.setDisplayName("Department Finance");
        long l = profileService.addTeamScope(scope);

        // default team must be added
        List<TeamProfile> defTeams = profileService.listTeams(l);
        Assertions.assertEquals(1, defTeams.size());
        Assertions.assertEquals("default_" + scope.getName(), defTeams.get(0).getName());
        List<UserProfile> defUsers = profileService.listUsers(defTeams.get(0).getId(), 0, 500);
        Assertions.assertEquals(1, defUsers.size());
        Assertions.assertEquals(scope.getName(), defUsers.get(0).getName());

        TeamProfileAddDto teamProfile = new TeamProfileAddDto();
        teamProfile.setName("QA Team");
        teamProfile.setAvatarId("image_qa.png");
        teamProfile.setTeamScope((int)l);
        long tid = profileService.addTeam(teamProfile);
        Assertions.assertTrue(tid > 0);

        TeamProfile profile = profileService.readTeam(tid);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(teamProfile.getName(), profile.getName());
        Assertions.assertEquals((int)profile.getId(), tid);
        Assertions.assertEquals(teamProfile.getAvatarId(), profile.getAvatarId());
        Assertions.assertEquals(teamProfile.getTeamScope(), profile.getTeamScope());
        Assertions.assertTrue(profile.isActive());
        Assertions.assertNotNull(profile.getCreatedAt());
        Assertions.assertNotNull(profile.getUpdatedAt());

        profile.setAvatarId("new_team_image.png");
        ////Assertions.assertTrue(profileService.editTeam(profile.getId(), profile));

//        teamProfile = profileService.readTeam(profile.getId());
//        Assertions.assertEquals(profile.getName(), teamProfile.getName());
//        Assertions.assertEquals(profile.getAvatarId(), teamProfile.getAvatarId());
//        Assertions.assertEquals(profile.getTeamScope(), teamProfile.getTeamScope());

        profile.setName("QA Team Modified");
        ////Assertions.assertTrue(profileService.editTeam(profile.getId(), profile));

//        teamProfile = profileService.readTeam(profile.getId());
//        Assertions.assertEquals(profile.getName(), teamProfile.getName());
//        Assertions.assertEquals(profile.getAvatarId(), teamProfile.getAvatarId());
//        Assertions.assertEquals(profile.getTeamScope(), teamProfile.getTeamScope());

        List<TeamProfile> teamProfiles = profileService.listTeams(l);
        Assertions.assertEquals(2, teamProfiles.size());

        clearTables("OA_TEAM_SCOPE");
    }

    @Test
    void testTeamScopeCrud() throws Exception {
        IProfileService profileService = apiService.getProfileService();
        clearTables("OA_TEAM_SCOPE");

        TeamScopeAddDto teamScope = new TeamScopeAddDto();
        teamScope.setName("sales");
        teamScope.setDisplayName("Sales Project");
        teamScope.setExtId(100L);
        long tid = profileService.addTeamScope(teamScope);
        Assertions.assertTrue(tid > 0);

        List<TeamScope> teamScopes = profileService.listTeamScopes();
        Assertions.assertEquals(1, teamScopes.size());

        TeamScope profile = profileService.readTeamScope(tid);
        Assertions.assertNotNull(profile);
        Assertions.assertEquals(teamScope.getName(), profile.getName());
        Assertions.assertEquals(teamScope.getDisplayName(), profile.getDisplayName());
        Assertions.assertEquals(teamScope.getExtId(), profile.getExtId());
        Assertions.assertEquals((int)profile.getId(), tid);
        Assertions.assertTrue(profile.isActive());
        Assertions.assertNotNull(profile.getCreatedAt());
        Assertions.assertNotNull(profile.getUpdatedAt());

        profile.setDisplayName("Saled Project - UK");
        ////Assertions.assertTrue(profileService.editTeamScope(profile.getId(), profile));

        teamScopes = profileService.listTeamScopes();
        Assertions.assertEquals(1, teamScopes.size());

//        teamScope = profileService.readTeamScope(profile.getId());
//        Assertions.assertEquals(teamScope.getName(), profile.getName());
//        Assertions.assertEquals(teamScope.getDisplayName(), profile.getDisplayName());
//        Assertions.assertEquals(teamScope.getExtId(), profile.getExtId());
    }

    @Test
    void testUserTeamAssociation() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        UserProfileAddDto user1 = new UserProfileAddDto();
        user1.setName("Robert Ford");
        user1.setEmail("ford@westworld.com");
        user1.setMale(true);

        TeamScopeAddDto testProject = new TeamScopeAddDto();
        testProject.setName("Test Project");
        testProject.setDisplayName(testProject.getName());
        int scopeId = (int) profileService.addTeamScope(testProject);

        // default team / scope user / user
        List<TeamProfile> defTeams = profileService.listTeams(scopeId);
        Assertions.assertEquals(1, defTeams.size());
        Assertions.assertEquals("default_" + testProject.getName(), defTeams.get(0).getName());
        List<UserProfile> defUsers = profileService.listUsers(defTeams.get(0).getId(), 0, 500);
        Assertions.assertEquals(1, defUsers.size());
        Assertions.assertEquals(testProject.getName(), defUsers.get(0).getName());

        TeamProfileAddDto team1 = new TeamProfileAddDto();
        team1.setName("leadership");
        team1.setTeamScope(scopeId);
        TeamProfileAddDto team2 = new TeamProfileAddDto();
        team2.setName("robot");
        team2.setTeamScope(scopeId);
        TeamProfileAddDto team3 = new TeamProfileAddDto();
        team3.setName("qa");
        team3.setTeamScope(scopeId);

        long u1id = profileService.addUserProfile(user1);
        Assertions.assertTrue(u1id > 0);

        long t1id = profileService.addTeam(team1);
        // default user by team name
        List<UserProfile> defTeamUsers = profileService.listUsers(t1id, 0, 500);
        Assertions.assertEquals(1, defTeamUsers.size());
        Assertions.assertEquals(team1.getName(), defTeamUsers.get(0).getName());

        long t2id = profileService.addTeam(team2);
        defTeamUsers = profileService.listUsers(t2id, 0, 500);
        Assertions.assertEquals(1, defTeamUsers.size());
        Assertions.assertEquals(team2.getName(), defTeamUsers.get(0).getName());


        Assertions.assertTrue(profileService.addUserToTeam(u1id, t1id, 1));
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(u1id);
        Assertions.assertNotNull(currentTeamOfUser);
        Assertions.assertEquals(t1id, (long) currentTeamOfUser.getTeamId());
        Assertions.assertEquals(u1id, (long) currentTeamOfUser.getUserId());
        Assertions.assertEquals(1, (int) currentTeamOfUser.getRoleId());
        Assertions.assertTrue(currentTeamOfUser.getJoinedTime() < System.currentTimeMillis());

        // add again to the same team with same role should return false
        Assertions.assertFalse(profileService.addUserToTeam(u1id, t1id, 1));

        // but should be able to change the role
        Assertions.assertTrue(profileService.addUserToTeam(u1id, t1id, 2));
        currentTeamOfUser = profileService.findCurrentTeamOfUser(u1id);
        Assertions.assertNotNull(currentTeamOfUser);
        Assertions.assertEquals(t1id, (long) currentTeamOfUser.getTeamId());
        Assertions.assertEquals(u1id, (long) currentTeamOfUser.getUserId());
        Assertions.assertEquals(2, (int) currentTeamOfUser.getRoleId());

        // one user in team1
        List<UserProfile> userProfiles = profileService.listUsers(t1id, 0, 100);
        Assertions.assertNotNull(userProfiles);
        Assertions.assertEquals(2, userProfiles.size());

        // add to robot team
        Assertions.assertTrue(profileService.addUserToTeam(u1id, t2id, 8));
        currentTeamOfUser = profileService.findCurrentTeamOfUser(u1id);
        Assertions.assertNotNull(currentTeamOfUser);
        Assertions.assertEquals(t2id, (long) currentTeamOfUser.getTeamId());
        Assertions.assertEquals(u1id, (long) currentTeamOfUser.getUserId());
        Assertions.assertEquals(8, (int) currentTeamOfUser.getRoleId());
        Assertions.assertTrue(currentTeamOfUser.getJoinedTime() < System.currentTimeMillis());

        clearTables("OA_TEAM_SCOPE");
    }

    @BeforeEach
    void clearTables() throws Exception {
        clearTables("OA_USER", "OA_TEAM", "OA_TEAM_USER", "OA_TEAM_SCOPE");
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        dbStart();
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        dbClose("OA_USER", "OA_TEAM", "OA_TEAM_USER", "OA_TEAM_SCOPE");
    }

}
