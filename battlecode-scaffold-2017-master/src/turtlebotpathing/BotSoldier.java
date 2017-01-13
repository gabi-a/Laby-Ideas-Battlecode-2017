package turtlebotpathing;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		Team enemy = rc.getTeam().opponent();

		MapLocation myLocation = rc.getLocation();
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo target = null;
		float targetRating = 10000;
		float curRating;
		for (int i = 0; i < robots.length; i++) {
			curRating = rateTarget(robots[i]);
			if (curRating < targetRating) {
				target = robots[i];
				targetRating = curRating;
			}
			
			if(robots[i].getType() == RobotType.GARDENER || robots[i].getType() == RobotType.ARCHON) {
				Comms.writeAttackEnemy(rc, robots[i].getLocation(), robots[i].getID());
			}
			
			if(robots[i].getID() == Comms.readAttackID(rc)) {
				if(robots[i].getHealth() < 20f) {
					Comms.clearAttackEnemy(rc);
				}
			}
		}

		boolean moved = false;
		float dist = 0;
		Direction dir = new Direction(0);
		RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
		if (target != null) {
			dir = rc.getLocation().directionTo(target.location);
			dist = target.location.distanceTo(rc.getLocation());
		}

	        if(!Nav.avoidBullets(rc, myLocation)) {
			if (target != null) {
				if (dist >= 6) {
					moved = Nav.tryMove(rc, dir);
				}
				else if (dist < 4 && rc.canMove(dir.opposite())) {
					moved = Nav.tryMove(rc, dir.opposite());
				}
			}
			else if(!rc.hasMoved()) {
				RobotInfo allyBot;
				for(int i = allyRobots.length;i-->0;) {
					allyBot = allyRobots[i];
					if(allyBot.getType() == RobotType.GARDENER) {
						float distance = myLocation.distanceTo(allyBot.getLocation());
						if (distance > 6) {
							moved = Nav.tryMove(rc, myLocation.directionTo(allyBot.getLocation()));
							break;
						}
					}
				}
				if(!moved) {
					MapLocation attackLocation = Comms.readAttackLocation(rc);
					if(attackLocation != null) {
						moved = Nav.pathTo(rc, attackLocation, new RobotType[]{RobotType.SCOUT,RobotType.SOLDIER,RobotType.LUMBERJACK});
					}
					if(!moved) {
						moved = Nav.explore(rc);
					}
				}
			}
		}

		if (target != null && Nav.safeToShoot(rc, allyRobots, dir) && rc.canFireSingleShot()) {
			rc.fireSingleShot(dir.rotateRightDegrees(10f*((float)Math.random()-0.5f)));
		}
	}

        private static float rateTarget(RobotInfo target) {
		float score;
		score = target.location.distanceTo(rc.getLocation());
		if (target.type == RobotType.ARCHON) score -= 2;
		return score;
	}

	
		
	
	
}
