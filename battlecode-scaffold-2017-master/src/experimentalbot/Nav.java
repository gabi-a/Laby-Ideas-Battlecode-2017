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
	
	//SCOUT ATTACK MOVE VARIABLE
	static TreeInfo treeCache;
    
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
	
	
	static void pathTo(RobotController rc, MapLocation goal) throws GameActionException {
		RobotType[] avoid = new RobotType[0];
		pathTo(rc, goal, avoid, rc.getType().strideRadius); 
	}
	
	static void pathTo(RobotController rc, MapLocation goal, RobotType[] avoid) throws GameActionException {
		pathTo(rc, goal, avoid, rc.getType().strideRadius); 
	}

	static void pathTo(RobotController rc, MapLocation goal, RobotType[] avoid, float stride) throws GameActionException {

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

		// Idea: if we can go closer to the goal than we ever have before, do so.
		for (int i = 0; i < 7; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
				rc.move(trial, stride);
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				return;
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
				rc.move(trial, stride);
				dMin = myLocation.add(trial, stride).distanceTo(goal);
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
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
						rc.move(trial, stride);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return;
					}
				}
				break;
			case RIGHT:
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
						rc.move(trial, stride);
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
	
	private static boolean inEnemySight(RobotController rc, Direction trial, RobotType[] avoid, RobotInfo[] enemyList, MapLocation myLocation, float stride) {
		if (avoid.length == 0) {
			return false;
		}
		boolean scoutFlag = false;
		if (rc.getType() == RobotType.SCOUT) {
			scoutFlag = true;
		}
		for(RobotInfo enemy : enemyList) {
			if(java.util.Arrays.asList(avoid).contains(enemy.type)
					&& enemy.location.distanceTo(myLocation.add(trial, stride)) <= enemy.type.sensorRadius) {
				return true;
			}
		}
		return false;
	}
	
	public static void scoutAttackMove(RobotController rc, MapLocation myLocation, RobotInfo enemy) throws GameActionException {
		if(inEnemySight(rc, new Direction(myLocation, enemy.location), 
				new RobotType[]{RobotType.LUMBERJACK}, rc.senseNearbyRobots(rc.getType().sensorRadius, myTeam.opponent()), myLocation, 2.5f ) 
				|| !scoutAttack(rc, myLocation, enemy)) {
			treeCache = null;
			pathTo(rc, enemy.location, new RobotType[]{RobotType.LUMBERJACK}, 1.5f);
		}
	}
	
	private static boolean scoutAttack(RobotController rc, MapLocation myLocation, RobotInfo enemy) throws GameActionException {
		// This will only be called by scouts, so to save bytecodes we define this value
		float scoutSightRadius = 10f;
		rc.setIndicatorDot(myLocation, 255, 255, 255);
		// Chase the enemy if they are escaping
		if(myLocation.distanceTo(enemy.location) <= scoutSightRadius - enemy.getType().strideRadius) {
			float bodyRadius = rc.getType().bodyRadius;
			TreeInfo[] treeList = rc.senseNearbyTrees(bodyRadius);
			if(treeList.length == 0) {
				return false;
			}
			float closestTreeDist = 10000f;
			TreeInfo closestTree = null;
			// Find the tree that minimises d(myLoc, tree) + d(tree,enemyLoc)
			if (treeCache == null) {
				for(TreeInfo tree : treeList) {
					if(tree.radius < 1) {
						continue;
					}
					float d = myLocation.distanceTo(tree.location) + tree.location.distanceTo(enemy.location);			
					if(d < closestTreeDist) {
						closestTreeDist = d;
						closestTree = tree;
						treeCache = tree;
					}
				}
			}
			else {
				closestTree = treeCache;
			}
			if(closestTree == null) {
				return false;
			}
			if(rc.canShake(closestTree.ID)) {
				rc.shake(closestTree.ID);
			}
			// Head to the edge of that tree closest to the enemy location
			MapLocation targetPosition = closestTree.location.add(new Direction(closestTree.location, enemy.location), closestTree.radius - 1f);
			Direction heading =  new Direction(myLocation, targetPosition);
			float stride = Math.min(myLocation.distanceTo(targetPosition), 1.5f);
			if(rc.canMove(heading, stride)) {
				rc.move(heading, stride);
				return true;
			}
		}
		return false;
	}
}
