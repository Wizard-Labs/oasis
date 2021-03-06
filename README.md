[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Build Status](https://github.com/isuru89/oasis/workflows/Oasis-ci-test/badge.svg)
[![Known Vulnerabilities](https://snyk.io/test/github/isuru89/oasis/badge.svg)](https://snyk.io/test/github/isuru89/oasis)
[![coverage](https://codecov.io/gh/isuru89/oasis/branch/master/graph/badge.svg)](https://codecov.io/gh/isuru89/oasis)


# OASIS
Open-source Gamification framework based on Redis.

_This project is still under development_

Oasis, is an event-driven gamification framework having ability to define the game rules for events
coming from your applications. This is inspired from Stackoverflow badge system, and extended into 
supporting many game elements. Such as, Points, Badges, Leaderboards,
Milestones, Challenges, and Races. 

## Features:
  * Different types of customizable gamification elements. (see below)
  * Near real-time status updates
  * Team support
  * Character driven game playing <To-be-implemented>
  * Narrative Play <To-be-implemented>
  * Cloud friendly <To-be-implemented>
  
## Key Gamification Elements in Oasis

### Points
This is 0one of the core element type in Oasis. The points indicate a measurement about a user. Users
can accumulate points over the time as rules defined by the admin. Sometimes, points can be negative,
and it called penalties. 

### Badges
A badge is a collectible achievement by a user based on correlating one or several
events. Every badge has an attribute. An attribute is like a Gold, Silver, Bronze type, and 
can be defined by an admin.

There are several kinds of badges supported by Oasis.

  * An event has occurred for the first time (eg: [Stackoverflow Altruist badge](https://stackoverflow.com/help/badges/222/altruist) )
  * An event satisfies a certain criteria (eg: [Stackoverflow Popular Question](https://stackoverflow.com/help/badges/26/popular-question) )
     * For different thresholds can award different sub-badges
     * (eg: [Stackoverflow Famous question](https://stackoverflow.com/help/badges/28/famous-question) )
  * Streaks:
     * Satisfies a condition for N consecutive times. (eg: [Stackoverflow Enthusiast](https://stackoverflow.com/help/badges/71/enthusiast) )
     * Satisfies a condition for N consecutive times within T time-unit.
     * Satisfies a condition for N times within T time-unit. (eg: [Stackoverflow Curious badge](https://stackoverflow.com/help/badges/4127/curious) )
  * Earn K points within a single time-unit (daily/weekly/monthly)
     * Eg: [Stackoverflow Mortarboard badge](https://stackoverflow.com/help/badges/144/mortarboard)
  * Daily accumulation of an event field is higher than a threshold (T) for,
     * N consecutive days. (eg: Earn 50 daily reputation for 10 consecutive days)
     * N separate days (eg: Earn 200 daily reputation for 50 consecutive days)
  * Manually
     * Curators and admin can award badges to players based on non-measurable activities.

### Milestones
Milestone can be created to accumulate points over the lifecycle of game.
It indicates the progress gained by a user. Milestones are always being based on the points
scored by a user.

Milestones can be used to give a *rank* to a user based on the current status.
Eg: In Stackoverflow, the total Reputation earned can be defined as a milestone definition and levels
can be defined in such a way,
  * Scoring 10k reputation - Level 1
  * Scoring 50k reputation - Level 2
  * Scoring 100k reputation - Level 3
  * Scoring 500k reputation - Level 4
  * Scoring 1M reputation - Level 5
  
### Leaderboards
Oasis provides leaderboards based on points scored by users. There are several leaderboards
supported by a game. Such as,
  1. Game Leaderboard
  2. Team Leaderboard

Each leaderboard can be viewed scoped on the time slots. Supported time slots are;
   1. Daily
   2. Weekly
   3. Monthly
   4. Quarterly
   5. Annually

### Challenges
Challenge can be created by a curator at any time of game lifecycle
to motivate a user towards a very short term goal. Challenge must have a start time
and end time.
A challenge can be scoped to a single user, team, or a game. It also can be defined to
have single winner or multiple winners. Winners are being selected First-come basis.

A challenge can be over in two ways.
  * Number of all winners found.
  * Time expired

### Races
Races are point-awarding leaderboards for non-overlapping time windows. 
Think of this is as an award for being top in a particular leaderboard.
At a pre-defined time range (daily, weekly, monthly), 
top N leaderboard winners will be awarded a set of points. 
This will continue in each time window as specified in a definition of race. 

### Ratings
Ratings indicate the current state of a user at a particular time. Based on the events, user's
status will be calculated and from that status, some amount of net points will be awarded.
A user can only be in one state at a time. A difference between Milestone and Rating would
be ratings can fluctuate to any direction, while milestones can only grow.

For Eg: someone can define a rating (good/bad) based on total good answer ratio. As long as
a user has positive good answer ratio, then that user will have, say 100 points, with him/her.
Once the ratio goes down below a threshold, status will be changed to _'bad'_, and he/she will
lose 100 points he/she had.

## Entities in Oasis
There are several entities in Oasis.
  1. Game
  2. User
  3. Event Source
  4. Team
  
Relationship between above entities are as below.
  1. There can be many games running at the same time in a single deployment of Oasis.
  2. A user can play in many games at once.
  3. A user can only belong to a single team within a particular game at a time.
  4. A user may change his/her team, or leave the game
  5. An event source can emit different types of events.
  6. A single event type can be multi-cast to any number of games.
  7. All game elements based on those events.

## Why Oasis?

Ultimate objective of the Oasis is to increase the user engagement in applications
through a gamified environment. 
Oasis might help your applications/community to increase the productivity
and could help in creating a better and enjoyable ride.

Following gamifiable environments have been identified.
   - SDLC: whole software development lifecycle (coding, bug fixing, deployments) using the
   application stack (Code Quality, CI/CD, ALM tools)
   - Support Systems: IT helpdesk systems
   - Q/A sites: Stackoverflow, Reddit like sites
   - Social Networking

## Kudos!

This project could not have existed thanks to these awesome open-source projects.

  * [Redis](https://redis.io/)
  * [Akka](https://akka.io/)
  * [Vert.x](https://vertx.io/)
  * [Spring-boot](https://spring.io/projects/spring-boot)
  * [MVEL](https://github.com/mvel/mvel)
  
## License

Apache License - version 2.0

