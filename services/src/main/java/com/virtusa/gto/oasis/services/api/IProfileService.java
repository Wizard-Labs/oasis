package com.virtusa.gto.oasis.services.api;

import com.virtusa.gto.oasis.services.model.TeamProfile;
import com.virtusa.gto.oasis.services.model.UserProfile;

public interface IProfileService {

    void addUserProfile(UserProfile profile);
    UserProfile readUserProfile(long userId);
    void editUserProfile(long userId, UserProfile profile);
    void deleteUserProfile(long userId);

    void addTeam(TeamProfile teamProfile);
    TeamProfile readTeam(long teamId);

    void addUserToTeam(long userId, long teamId);

    void readUserGameStats(long userId);
    void readUserGameTimeline(long userId);
    void readUserBadges(long userId);
    void readUserPoints(long userId);
    void readUserMilestones(long userId);
    void readUserRankings(long userId);


}
