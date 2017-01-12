package turtlebot;

import battlecode.common.*;

public class BotScout {
    
        public static MapLocation moveTarget = null;
        public static boolean startupFlag = true;
        public static MapLocation homeMemoryLocation = null;
        public static boolean returning = false;
        public static RobotInfo enemyTarget = null;

        public static void turn(RobotController rc) throws GameActionException {
		MapLocation myLocation = rc.getLocation();
                
                RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                enemyTarget = null;

		for(int i = 0; i < enemies.length; i++){
			if(enemies[i].getType() == RobotType.GARDENER){
				//Comms.writeStack(rc, Comms.ENEMY_ARCHON_START, Comms.ENEMY_ARCHON_END, enemies[i].getLocation());
				enemyTarget = enemies[i];
			} else {
				//Comms.writeStack(rc, Comms.ENEMY_START, Comms.ENEMY_END, enemies[i].getLocation());
			}
		}
                
                if(startupFlag) {
                   System.out.format("I'm here!\n");
                   homeMemoryLocation = myLocation;
                   System.out.format("My home location: (%f, %f)\n", myLocation.x, myLocation.y);
                   startupFlag = false;
                }
                
                if( moveTarget == null) {
                    moveTarget = Comms.popStack(rc, Comms.ARCHON_SCOUT_DELEGATION_START, Comms.ARCHON_SCOUT_DELEGATION_END);
                    if (moveTarget == null) {
                        System.out.format("Failed to get comms, while %b returning\n", returning);
                    }
                    else {
                        System.out.format("My assignment: (%f, %f)\n", moveTarget.x, moveTarget.y);
                    }
                }
                
		if(enemyTarget == null) {
                        if (moveTarget == null && returning == false) {
                            Nav.explore(rc);
                        }
                        else {
                            if (!returning) {
                                Direction moveDirection = new Direction(myLocation, moveTarget); 
                                boolean successful = Nav.tryMove(rc, moveDirection);
                                if(!successful && !rc.onTheMap(myLocation.add(moveDirection, 3f))) {
                                    returning = true;
                                    System.out.format("I'm returning from: (%f, %f)\n", myLocation.x, myLocation.y);
                                }
                            }
                            else {
                                Direction moveDirection = new Direction(myLocation, homeMemoryLocation); 
                                Nav.tryMove(rc, moveDirection);
                                if(myLocation.distanceTo(homeMemoryLocation) < 3f) {
                                    returning = false;
                                    moveTarget = null;
                                    broadcastUnassigned(rc);
                                    System.out.format("I'm unassigned now...\n");
                                }
                            }
                        }
		}
                else {
			Direction dir = rc.getLocation().directionTo(enemyTarget.location);
			float dist = enemyTarget.location.distanceTo(rc.getLocation());
			if(!Nav.avoidBullets(rc, myLocation)) {
                            if (dist >= 1) {
                                Nav.tryMove(rc, dir);
                            }
			}
                        if(rc.canFireSingleShot()) {
                            rc.fireSingleShot(dir);
                        }
		}
        }

        public static void broadcastUnassigned(RobotController rc) throws GameActionException {
            Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, rc.getLocation());
        }

}
