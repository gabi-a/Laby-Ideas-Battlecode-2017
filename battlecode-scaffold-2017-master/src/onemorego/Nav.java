package onemorego;
import battlecode.common.*;

public class Nav {
	
	static Direction heading = null;
	
	/*
	 * Tree Bug
	 * 
	 * If using right hand: If right hand is touching a tree: If obstacle ahead:
	 * Heading = Left Else: Heading = Forwards Else: Heading = Right
	 * 
	 * Else If using left hand: If left hand is touching a tree: If obstacle
	 * ahead: Heading = Right Else: Heading = Forwards Else: Heading = Left
	 * 
	 * Try move in Heading direction If can't move: Switch hand
	 */
	static enum TreeBugHands {
		LEFT, RIGHT
	};

	static TreeBugHands treeBugHand = TreeBugHands.LEFT;
	static Direction treeBugHeading = Direction.getNorth();
	static int treeBugstuckCount = 0;
	static final int treeBugRotAngle = 15;
	
	static boolean treeBug(RobotController rc, BulletInfo[] bullets) throws GameActionException {

		MapLocation myLocation = rc.getLocation();

		switch (treeBugHand) {

		case LEFT:

			// Uses 100 bytecodes
			TreeInfo[] leftTrees = rc.senseNearbyTrees(myLocation.add(treeBugHeading.rotateLeftDegrees(90), 2f), 1f,
					null);
			if (leftTrees.length > 0) {
				if (Nav.tryMove(rc, treeBugHeading, bullets)) {
					return true;
				} else {
					treeBugHeading = treeBugHeading.rotateRightDegrees(treeBugRotAngle);
				}
			} else {
				treeBugHeading = treeBugHeading.rotateLeftDegrees(treeBugRotAngle);
			}
			break;

		case RIGHT:

			// Uses 100 bytecodes
			TreeInfo[] rightTrees = rc.senseNearbyTrees(myLocation.add(treeBugHeading.rotateRightDegrees(90), 2f), 1f,
					null);
			if (rightTrees.length > 0) {
				if (Nav.tryMove(rc, treeBugHeading, bullets)) {
					return true;
				} else {
					treeBugHeading = treeBugHeading.rotateLeftDegrees(treeBugRotAngle);
				}
			} else {
				treeBugHeading = treeBugHeading.rotateRightDegrees(treeBugRotAngle);
			}

			break;

		}

		if (Nav.tryMove(rc, treeBugHeading, bullets)) {
			treeBugstuckCount--;
			return true;
		} else {
			treeBugstuckCount++;
			if(!Nav.bugExplore(rc, bullets) && treeBugstuckCount > 10) {
				treeBugHand = (treeBugHand == TreeBugHands.RIGHT) ? TreeBugHands.LEFT : TreeBugHands.RIGHT;	
			}
		}

		return false;

	}
	
	public static boolean tryMove(RobotController rc, Direction dir, BulletInfo[] bullets) throws GameActionException {
		return tryMove(rc, dir, 5, 10, bullets);
	}
	
	public static boolean tryPrecisionMove(RobotController rc, Direction dir, float stride, BulletInfo[] bullets) throws GameActionException {
		return tryPrecisionMove(rc, dir, 5, 10, stride, bullets);
	}

	static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide, BulletInfo[] bullets)
			throws GameActionException {

		MapLocation myLocation = rc.getLocation();
		MapLocation moveLocation;
		
		// First, try intended direction
		moveLocation = myLocation.add(dir,rc.getType().strideRadius);
		if (rc.canMove(dir) && isSafeLocation(rc, moveLocation, bullets)) {
			rc.move(dir);
			return true;
		}

		// Now try a bunch of similar angles
		boolean moved = false;
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			moveLocation = myLocation.add(dir.rotateLeftDegrees(degreeOffset * currentCheck),rc.getType().strideRadius);
			if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck)) && Nav.isSafeLocation(rc, moveLocation, bullets)) {
				rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
				treeBugHeading = dir;
				return true;
			}
			// Try the offset on the right side
			moveLocation = myLocation.add(dir.rotateRightDegrees(degreeOffset * currentCheck),rc.getType().strideRadius);
			if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck)) && Nav.isSafeLocation(rc, moveLocation, bullets)) {
				rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck));
				treeBugHeading = dir;
				return true;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		// System.out.println("I'm stuck! :( I have "+
		// Clock.getBytecodesLeft()+" bytecodes left");
		return false;
	}
	
	static boolean tryPrecisionMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide, float stride, BulletInfo[] bullets)
			throws GameActionException {

		if(rc.hasMoved()) return false;
		
		MapLocation myLocation = rc.getLocation();
		MapLocation moveLocation;
		
		// First, try intended direction
		moveLocation = myLocation.add(dir,rc.getType().strideRadius);
		if (rc.canMove(dir,stride) && isSafeLocation(rc, moveLocation, bullets)) {
			rc.move(dir, stride);
			return true;
		}

		// Now try a bunch of similar angles
		boolean moved = false;
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			moveLocation = myLocation.add(dir.rotateLeftDegrees(degreeOffset * currentCheck),stride);
			if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck),stride) && Nav.isSafeLocation(rc, moveLocation, bullets)) {
				rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck),stride);
				treeBugHeading = dir;
				return true;
			}
			// Try the offset on the right side
			moveLocation = myLocation.add(dir.rotateRightDegrees(degreeOffset * currentCheck),stride);
			if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck),stride) && Nav.isSafeLocation(rc, moveLocation, bullets)) {
				rc.move(dir.rotateRightDegrees(degreeOffset * currentCheck),stride);
				treeBugHeading = dir;
				return true;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		// System.out.println("I'm stuck! :( I have "+
		// Clock.getBytecodesLeft()+" bytecodes left");
		return false;
	}
	
	public static boolean bugExplore(RobotController rc, BulletInfo bullets[]) throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		int moveAttemptCount = 0;
		while (moveAttemptCount < 30) {
			if (rc.onTheMap(myLocation.add(treeBugHeading, rc.getType().strideRadius + rc.getType().bodyRadius),
					rc.getType().bodyRadius + 2f) && Nav.tryMove(rc, treeBugHeading, bullets)) {
				return true;
			}
			treeBugHeading = Nav.randomDirection();
			moveAttemptCount++;
		}
		return false;
	}
	
	public static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
	
	public static boolean explore(RobotController rc, BulletInfo[] bullets) throws GameActionException {
    	MapLocation myLocation = rc.getLocation();
		if(heading == null) {
			heading = randomDirection();
		}
    	int moveAttemptCount = 0;
    	while(moveAttemptCount < 5) {
    		if(rc.onTheMap(myLocation.add(heading,rc.getType().strideRadius + rc.getType().bodyRadius ),rc.getType().bodyRadius+2f) 
					&& Nav.tryMove(rc, heading, bullets)) {
    				return true;
    		}
    		heading = Nav.randomDirection();
    		moveAttemptCount++;
    	}
    	return false;
    }
	
	
	/*
	 * 
	 *  Magic pathing
	 * 
	 * 
	 * 
	 */
	
	static MoveState moveState = MoveState.TOWARD_LEFT;
	static float dMin = 10000f;
	static MapLocation goalCache = new MapLocation(-1, -1);

	static Team myTeam = RobotPlayer.rc.getTeam();
	
	public static enum MoveState {
		LEFT,
		RIGHT,
		TOWARD_LEFT,
		TOWARD_RIGHT
	}
	
	static boolean pathTo(RobotController rc, MapLocation goal, BulletInfo[] bullets) throws GameActionException {
		RobotType[] avoid = new RobotType[0];
		return pathTo(rc, goal, avoid, rc.getType().strideRadius, bullets); 
	}
	
	static boolean pathTo(RobotController rc, MapLocation goal, RobotType[] avoid, BulletInfo[] bullets) throws GameActionException {
		return pathTo(rc, goal, avoid, rc.getType().strideRadius, bullets); 
	}

	static boolean pathTo(RobotController rc, MapLocation goal, RobotType[] avoid, float stride, BulletInfo[] bullets) throws GameActionException {
		
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemyList = rc.senseNearbyRobots(rc.getType().sensorRadius, myTeam.opponent());
		
		// If this is the first time going here, clear our pathing memory
		if (goal.distanceTo(goalCache) > 5f) {
			goalCache = goal;
			dMin = 10000f;
			moveState = MoveState.TOWARD_LEFT;
		}
		
		goalCache = goal;

		float degreeOffset = 30f;
		Direction trial;

		// Idea: if we can go closer to the goal than we ever have before, do so.
		for (int i = 0; i < 3; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride) && Nav.isSafeLocation(rc, goal, bullets)) {
				rc.move(trial, stride);
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				return true;
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride) && Nav.isSafeLocation(rc, goal, bullets)) {
				rc.move(trial, stride);
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				return true;
			}
		}

		// If we're a lumberjack, stop thinking too hard and chop your way through
		if(rc.getType() == RobotType.LUMBERJACK){
			TreeInfo[] trees = rc.senseNearbyTrees(3, Team.NEUTRAL);
			if(trees.length > 0) rc.chop(trees[0].getID());
		}

		// Else, let's start following a wall.
		if (moveState == MoveState.TOWARD_LEFT) {
			moveState = moveState.LEFT;
		} else if (moveState == MoveState.TOWARD_RIGHT) {
			moveState = moveState.RIGHT;
		}

		switch (moveState) {
			case LEFT:
				for (int i = 0; i < 6; i++) {
					trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride) && Nav.isSafeLocation(rc, goal, bullets)) {
						rc.move(trial, stride);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return true;
					}
				}
				break;
			case RIGHT:
				for (int i = 0; i < 6; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride) && Nav.isSafeLocation(rc, goal, bullets)) {
						rc.move(trial, stride);
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return true;
					}
				}
				break;
			default:
				System.out.println("PATHING SHOULDN'T GET HERE!");
				break;
		}

		moveState = (moveState == moveState.LEFT) ? moveState.RIGHT : moveState.LEFT;
		return false;

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
	
public static MapLocation[] getSafeMoveLocations(RobotController rc, BulletInfo[] bullets) throws GameActionException {
		
		MapLocation myLocation = rc.getLocation();
		
		MapLocation[] possibleLocations = new MapLocation[]{
				myLocation,
				myLocation.add(0f, 2f), 
				myLocation.add(1.0472f, 2f),
				myLocation.add(2.0944f, 2f),
				myLocation.add(3.1416f, 2f),
				myLocation.add(4.1888f, 2f),
				myLocation.add(5.2360f, 2f),
		};
		
		MapLocation[] safeLocations = new MapLocation[possibleLocations.length];
		
		for(int i = possibleLocations.length;i-->0;) {
			if(isSafeLocation(rc, possibleLocations[i], bullets)) {
				//rc.setIndicatorDot(possibleLocations[i], 0, 50, 0);
				safeLocations[i] = possibleLocations[i];
			}
		}
		
		return safeLocations;
		
	}
	
	public static boolean isSafeLocation(RobotController rc, MapLocation location, BulletInfo[] bullets) throws GameActionException {

		if(!rc.canSenseLocation(location)) {
			location = rc.getLocation().add(rc.getLocation().directionTo(location), rc.getType().strideRadius);
		}
		
		if(!rc.onTheMap(location)) {
			return false;
		}
		
		for(int i = bullets.length; i-->0;) {
			if(willCollideWithMe(rc, bullets[i], location)) {
				return false;
			}
			if(bullets[i].location.distanceTo(location) < 2f) {
				return false;
			}
		}
		
		return true;
	}
	
	static boolean willCollideWithMe(RobotController rc, BulletInfo bullet, MapLocation loc) {

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }
		
		float distToRobot = bulletLocation.distanceTo(loc);

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
	
}
