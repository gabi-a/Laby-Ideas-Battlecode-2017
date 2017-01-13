package experimentalbot;
import battlecode.common.*;

public class Nav {

	public static enum MoveState {
		LEFT,
		RIGHT,
		TOWARD_LEFT,
		TOWARD_RIGHT
	}
	
	// NAVIGATION VARIABLES
	static MoveState moveState = MoveState.TOWARD_LEFT;
	static float dMin = 10000f;
	static MapLocation goalCache = new MapLocation(-1, -1);

	static Team myTeam = RobotPlayer.rc.getTeam();
    static Direction heading = Nav.randomDirection();
    
	/**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        return tryMove(rc, dir,5,10);
    }
    
    public static boolean tryPrecisionMove(RobotController rc, Direction dir, float stride) throws GameActionException {
        return tryPrecisionMove(rc, dir,5,10, stride);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
			// Try the offset of the left side
		   if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
			   rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
			   return true;
		   }
		   // Try the offset on the right side
		   if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
			   rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
			   return true;
		   }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        //System.out.println("I'm stuck! :( I have "+ Clock.getBytecodesLeft()+" bytecodes left");
        return false;
    }
    
    static boolean tryPrecisionMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide, float stride) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir,stride)) {
            rc.move(dir,stride);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck),stride)) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck),stride);
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck),stride)) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck),stride);
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        //System.out.println("I'm stuck! :( I have "+ Clock.getBytecodesLeft()+" bytecodes left");
        return false;
    }
    
    public static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
    
    public static boolean explore(RobotController rc) throws GameActionException {
    	MapLocation myLocation = rc.getLocation();
    	int moveAttemptCount = 0;
    	while(moveAttemptCount < 30) {
    		if(rc.onTheMap(myLocation.add(heading,rc.getType().strideRadius + rc.getType().bodyRadius ),rc.getType().bodyRadius+2f) 
					&& Nav.tryMove(rc, heading)) {
    				return true;
    		}
    		heading = Nav.randomDirection();
    		moveAttemptCount++;
    	}
    	return false;
    }
    
    public static boolean avoidBullets(RobotController rc, MapLocation myLocation) throws GameActionException {
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		BulletInfo bullet;
		MapLocation goalLoc = myLocation;
		if(bullets.length == 0) {
			return false;
		}
		for(int i = bullets.length;i-->0;) {
			bullet = bullets[i];
			if(willCollideWithMe(rc, bullet, myLocation)) {
				goalLoc = goalLoc.add(bullet.dir.rotateLeftDegrees(90));
			}
		}
		if(goalLoc == myLocation) {
			return false;
		}
		return Nav.tryMove(rc, myLocation.directionTo(goalLoc));
		
	}
	
	public static boolean avoidLumberjacks(RobotController rc, MapLocation myLocation) throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		if(enemies.length == 0) {
			return false;
		}
		Float[] enemyAngles = new Float[enemies.length];
		float s = 0;
		float c = 0;
		int eaPointer = 0;
		float closestDistance = 2.5f;
		for (RobotInfo enemy : enemies) {
			if(enemy.type == RobotType.LUMBERJACK && enemy.location.distanceTo(myLocation) <= 2.5f) {
				enemyAngles[eaPointer] = new Direction(myLocation, enemy.location).radians;
				float distanceToEnemy = enemy.location.distanceTo(myLocation);
				if(distanceToEnemy < closestDistance) {
					closestDistance = distanceToEnemy;
				}
				eaPointer++;
			}
		}
		if (enemyAngles[0] == null) {
			return false;
		}
		// Find best escape route
		for (Float angle : enemyAngles) {
			if( angle != null ) {
				s += Math.sin(angle);
				c += Math.cos(angle);
			}
		}
		s /= eaPointer;
		c /= eaPointer;
		if (c < 0) {
			return tryPrecisionMove(rc, new Direction((float) Math.atan2(s, c)), 2f, 5, 2.5f - closestDistance);			
		}
		else {
			return tryPrecisionMove(rc, (new Direction((float) Math.atan2(s, c)).opposite()), 2f, 5, 2.5f - closestDistance);
		}
		
	}
	
	
	static boolean willCollideWithMe(RobotController rc, BulletInfo bullet, MapLocation loc) {

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float distToRobot = bulletLocation.distanceTo(loc);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
	public static boolean simpleRunAway(RobotController rc, MapLocation myLocation, RobotInfo[] nearbyEnemies, TreeInfo[] nearbyTrees) throws GameActionException {
		MapLocation goalLoc = myLocation;
		for(int i = nearbyEnemies.length;i-->0;) {
			goalLoc = goalLoc.add(myLocation.directionTo(nearbyEnemies[i].getLocation()).opposite());
		}
		for(int i = nearbyTrees.length;i-->0;) {
			goalLoc = goalLoc.add(myLocation.directionTo(nearbyTrees[i].getLocation()).opposite());
		}
		return Nav.tryMove(rc, myLocation.directionTo(goalLoc));
	}
	
	
	private static void pathTo(RobotController rc, MapLocation goal) throws GameActionException {
		RobotType[] avoid = new RobotType[0];
		pathTo(rc, goal, avoid); 
	}

	static void pathTo(RobotController rc, MapLocation goal, RobotType[] avoid) throws GameActionException {

		int roundNum = rc.getRoundNum();
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemyList = rc.senseNearbyRobots(rc.getType().sensorRadius, myTeam.opponent());
		
		// If this is the first time going here, clear our pathing memory
		if (goal != goalCache) {
			goalCache = goal;
			dMin = 10000f;
			moveState = MoveState.TOWARD_LEFT;
		}

		float degreeOffset = 15f;
		Direction trial;
		float stride = rc.getType().strideRadius;

		// Idea: if we can go closer to the goal than we ever have before, do so.
		for (int i = 0; i < 7; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if (rc.canMove(trial) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation)) {
				rc.move(trial);
				dMin = myLocation.add(trial, rc.getType().strideRadius).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation)) {
				rc.move(trial);
				dMin = myLocation.add(trial, rc.getType().strideRadius).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
		}

		// Else, let's start following a wall.
		if (moveState == MoveState.TOWARD_LEFT) {
			moveState = moveState.LEFT;
		} else if (moveState == MoveState.TOWARD_RIGHT) {
			moveState = moveState.RIGHT;
		}

		switch (moveState) {
			case LEFT:
				rc.setIndicatorDot(myLocation, 255, 0, 0);
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
					if (rc.canMove(trial) && !inEnemySight(rc, trial, avoid, enemyList, myLocation)) {
						rc.move(trial);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return;
					}
				}
				break;
			case RIGHT:
				rc.setIndicatorDot(myLocation, 0, 255, 0);
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if (rc.canMove(trial) && !inEnemySight(rc, trial, avoid, enemyList, myLocation)) {
						rc.move(trial);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return;
					}
				}
				break;
			default:
				System.out.println("PATHING SHOULDN'T GET HERE!");
				break;
		}

		moveState = (moveState == moveState.LEFT) ? moveState.RIGHT : moveState.LEFT;
		return;

	}

	private static MoveState chooseMoveState() {
		if (moveState == MoveState.LEFT || moveState == MoveState.TOWARD_LEFT) {
			return MoveState.TOWARD_LEFT;
		} else {
			return MoveState.TOWARD_RIGHT;
		}
	}
	
	private static boolean inEnemySight(RobotController rc, Direction trial, RobotType[] avoid, RobotInfo[] enemyList, MapLocation myLocation) {
		if (avoid.length == 0) {
			return false;
		}
		for(RobotInfo enemy : enemyList) {
			if(java.util.Arrays.asList(avoid).contains(enemy.type)
					&& enemy.location.distanceTo(myLocation.add(trial, rc.getType().strideRadius)) <= enemy.type.sensorRadius) {
				return true;
			}
		}
		return false;
	}
}
