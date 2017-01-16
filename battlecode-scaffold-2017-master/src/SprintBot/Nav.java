package SprintBot;

import battlecode.common.*;

public class Nav {
	
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
	static int treeBugRotAngle = 45;
	
	static boolean treeBug(RobotController rc) throws GameActionException {

		MapLocation myLocation = rc.getLocation();
		rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(treeBugHeading,3), 255, 0, 0);
		TreeInfo[] trees = rc.senseNearbyTrees();
		if(trees.length > 0) {
			for(float tries = 1;(tries+=0.4f) < 4f;) {
				treeBugHeading = myLocation.directionTo(trees[0].location).rotateLeftDegrees(treeBugRotAngle*tries);
				rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(treeBugHeading,3), 0, 0, 255);
				if(Nav.tryMove(rc, treeBugHeading)) break;
				//if(rc.canMove(treeBugHeading)) {rc.move(treeBugHeading);break;}
				
			}
			if(!rc.hasMoved()) {
				treeBugRotAngle = -treeBugRotAngle;
				for(float tries = 1;(tries+=0.4f) < 4f;) {
					treeBugHeading = myLocation.directionTo(trees[0].location).rotateLeftDegrees(treeBugRotAngle*tries);
					rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(treeBugHeading,3), 0, 0, 255);
					if(Nav.tryMove(rc, treeBugHeading)) break;
					//if(rc.canMove(treeBugHeading)) {rc.move(treeBugHeading);break;}
				}
			}
		}
		if(!rc.hasMoved()) Nav.explore(rc);
		return true;
		/*
		if(treeBugstuckCount < 10) {
			TreeInfo[] myTreesAhead = rc.senseNearbyTrees(myLocation.add(treeBugHeading), 2f, rc.getTeam());
			if(myTreesAhead.length > 0) {
				TreeInfo treeAhead = myTreesAhead[0];
				if(treeAhead.health < 0.8f * treeAhead.maxHealth && Nav.tryMove(rc, myLocation.directionTo(treeAhead.location))) {
					return true;
				}
			}
		}
		*/
		/*
		switch (treeBugHand) {

		case LEFT:

			// Uses 100 bytecodes
			TreeInfo[] leftTrees = rc.senseNearbyTrees(myLocation.add(treeBugHeading.rotateLeftDegrees(90), 2f), 1f,
					null);
			rc.setIndicatorLine(rc.getLocation(), myLocation.add(treeBugHeading.rotateLeftDegrees(90), 5f), 0, 0, 255);

			if (leftTrees.length > 0) {
				if (Nav.tryMove(rc, treeBugHeading)) {
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
			rc.setIndicatorLine(rc.getLocation(), myLocation.add(treeBugHeading.rotateRightDegrees(90), 5f), 0, 0, 255);

			if (rightTrees.length > 0) {
				if (Nav.tryMove(rc, treeBugHeading)) {
					return true;
				} else {
					treeBugHeading = treeBugHeading.rotateLeftDegrees(treeBugRotAngle);
				}
			} else {
				treeBugHeading = treeBugHeading.rotateRightDegrees(treeBugRotAngle);
			}

			break;
		}
		*/
		/*
		if (Nav.tryMove(rc, treeBugHeading)) {
			treeBugstuckCount--;
			return true;
		} else {
			treeBugstuckCount++;
			if(!Nav.bugExplore(rc) && treeBugstuckCount > 10) {
				treeBugHand = (treeBugHand == TreeBugHands.RIGHT) ? TreeBugHands.LEFT : TreeBugHands.RIGHT;	
			}
		}
		*/

		//return false;

	}

	public static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
		return tryMove(rc, dir, 5, 10);
	}

	static boolean tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide)
			throws GameActionException {

		// First, try intended direction
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}

		// Now try a bunch of similar angles
		boolean moved = false;
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck))) {
				rc.move(dir.rotateLeftDegrees(degreeOffset * currentCheck));
				treeBugHeading = dir;
				return true;
			}
			// Try the offset on the right side
			if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck))) {
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

	public static boolean bugExplore(RobotController rc) throws GameActionException {
		MapLocation myLocation = rc.getLocation();
		int moveAttemptCount = 0;
		while (moveAttemptCount < 30) {
			if (rc.onTheMap(myLocation.add(treeBugHeading, rc.getType().strideRadius + rc.getType().bodyRadius),
					rc.getType().bodyRadius + 2f) && Nav.tryMove(rc, treeBugHeading)) {
				return true;
			}
			treeBugHeading = Nav.randomDirection();
			moveAttemptCount++;
		}
		return false;
	}

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

    
	public static boolean tryPrecisionMove(RobotController rc, Direction dir, float stride) throws GameActionException {
        return tryPrecisionMove(rc, dir,5,10, stride);
    }
	
	static boolean tryPrecisionMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide, float stride) throws GameActionException {
		MapLocation myLocation = rc.getLocation();
        // First, try intended direction
        if (rc.canMove(dir,stride) && rc.senseNearbyBullets(myLocation.add(dir,stride), rc.getType().bodyRadius).length == 0) {
            rc.move(dir,stride);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck),stride) && rc.senseNearbyBullets(myLocation.add(dir,stride), rc.getType().bodyRadius).length == 0) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck),stride);
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck),stride) && rc.senseNearbyBullets(myLocation.add(dir,stride), rc.getType().bodyRadius).length == 0) {
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
    
    static boolean pathTo(RobotController rc, MapLocation goal) throws GameActionException {
		RobotType[] avoid = new RobotType[0];
		return pathTo(rc, goal, avoid, rc.getType().strideRadius); 
	}
	
	static boolean pathTo(RobotController rc, MapLocation goal, RobotType[] avoid) throws GameActionException {
		return pathTo(rc, goal, avoid, rc.getType().strideRadius); 
	}

	static boolean pathTo(RobotController rc, MapLocation goal, RobotType[] avoid, float stride) throws GameActionException {

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
				return true;
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
				rc.move(trial, stride);
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				return true;
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
						return true;
					}
				}
				break;
			case RIGHT:
				for (int i = 0; i < 12; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
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
		return Nav.tryPrecisionMove(rc, myLocation.directionTo(goalLoc), rc.getType().strideRadius);
		
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
	
	public static boolean explore(RobotController rc) throws GameActionException {
    	MapLocation myLocation = rc.getLocation();
    	int moveAttemptCount = 0;
    	while(moveAttemptCount < 30) {
    		if(rc.onTheMap(myLocation.add(heading,rc.getType().strideRadius + rc.getType().bodyRadius ),rc.getType().bodyRadius+2f) 
					&& Nav.tryPrecisionMove(rc, heading, rc.getType().strideRadius)) {
    				return true;
    		}
    		heading = Nav.randomDirection();
    		moveAttemptCount++;
    	}
    	return false;
    }
	
}
