package vppointfarmer;

import battlecode.common.*;

public class BotScout {
	public static RobotInfo target = null;

    public static void turn(RobotController rc) throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		if(target == null){
			Nav.explore(rc);
			RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

			for(int i = 0; i < enemies.length; i++){
				if(enemies[i].getType() == RobotType.ARCHON){
					Comms.writeStack(rc, Comms.ENEMY_ARCHON_START, Comms.ENEMY_ARCHON_END, enemies[i].getLocation());
					target = enemies[i];
				} else {
					Comms.writeStack(rc, Comms.ENEMY_START, Comms.ENEMY_END, enemies[i].getLocation());
				}
			}
		}

		if(target != null){
			Direction dir = rc.getLocation().directionTo(target.location);
			float dist = target.location.distanceTo(rc.getLocation());
			if(!Nav.avoidBullets(rc, myLocation)) {
				if (dist >= 6) {
					Nav.tryMove(rc, dir);
				}
				else if (dist < 4 && rc.canMove(dir.opposite())) {
					Nav.tryMove(rc, dir.opposite());
				}
			}

			rc.fireSingleShot(dir);
		}
    }
    
}
