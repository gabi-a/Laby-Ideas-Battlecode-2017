package seeding;
import battlecode.common.*;

public class Nav {
	
	static Direction heading = randomDirection();
	
	public static MapLocation awayFromBullets(RobotController rc, MapLocation myLocation, BulletInfo[] bullets, TreeInfo[] trees, RobotInfo[] bots) throws GameActionException {
		
		// Time step all the bullets forward by 1 turn
		BulletInfo[] futureBullets = new BulletInfo[bullets.length];
		for(int i = bullets.length;i-->0;) {
			BulletInfo bullet = bullets[i];
			futureBullets[i] = new BulletInfo(bullet.ID, bullet.location.add(bullet.dir,1f*bullet.getSpeed()), bullet.dir,bullet.getSpeed(),bullet.getDamage());
		}
		
		// The move location will move around influenced by bullets and trees
		MapLocation moveLocation = myLocation;
		
		// Apply Anti Gravity from each bullet in its current or future position
		for(int i = bullets.length;i-->0;) {
			// If we might intersect the bullet in its current position on our next move,
			// decide where to move based on its current position
			if(bullets[i].location.distanceTo(myLocation) < 3f) {
				rc.setIndicatorDot(bullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(bullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(bullets[i].location.distanceTo(myLocation))));
				rc.setIndicatorLine(bullets[i].location, bullets[i].location.add(bullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
			else {
				rc.setIndicatorDot(futureBullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), Math.max(2f, 2f/(futureBullets[i].location.distanceTo(myLocation))));
				rc.setIndicatorLine(futureBullets[i].location, futureBullets[i].location.add(futureBullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
		}
		
		// Apply Anti Gravity for close trees
		for(int i = trees.length;i-->0;) {
			if(myLocation.distanceTo(trees[i].location) > trees[i].radius + rc.getType().bodyRadius + 2f) {
				continue;
			}
			rc.setIndicatorDot(trees[i].location, 0, 50, 50);
			moveLocation = moveLocation.add(trees[i].location.directionTo(myLocation), 1f);
		}
		
		// Apply Anti Gravity for close bots
		for(int i = bots.length;i-->0;) {
			if(myLocation.distanceTo(bots[i].location) > 3f) {
				continue;
			}
			rc.setIndicatorDot(bots[i].location, 0, 50, 50);
			moveLocation = moveLocation.add(bots[i].location.directionTo(myLocation), (bots[i].type == RobotType.GARDENER || bots[i].type == RobotType.ARCHON) ? 0f : 1f);
		}
		
		// Apply Anti Gravity for map edges
		if (!rc.onTheMap(myLocation.add(Direction.NORTH, 2f))) moveLocation=moveLocation.add(Direction.SOUTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 2f))) moveLocation=moveLocation.add(Direction.NORTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.WEST, 2f))) moveLocation=moveLocation.add(Direction.EAST, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.EAST, 2f))) moveLocation=moveLocation.add(Direction.WEST, bullets.length);
		
		rc.setIndicatorDot(moveLocation, 255, 255, 255);
		Direction moveDirection = myLocation.directionTo(moveLocation);
		if(moveDirection == null) return null;
		moveDirection = tryMove(rc, moveDirection, 5f, 24, bullets);
		
		// Rescale stride 
		float moveStride = (float) (2 - 1f/Math.pow(myLocation.distanceTo(moveLocation)+0.7, 2));
		moveLocation = myLocation.add(moveDirection, moveStride);
		
		rc.setIndicatorDot(moveLocation, 0, 0, 0);
		
		//rc.setIndicatorLine(myLocation,moveLocation, 255, 20, 10);
		
		return moveLocation;
	}
	
	public static MapLocation awayFromBullets(RobotController rc, MapLocation myLocation, BulletInfo[] bullets, TreeInfo[] trees) throws GameActionException {
		
		// Time step all the bullets forward by 1 turn
		BulletInfo[] futureBullets = new BulletInfo[bullets.length];
		for(int i = bullets.length;i-->0;) {
			BulletInfo bullet = bullets[i];
			futureBullets[i] = new BulletInfo(bullet.ID, bullet.location.add(bullet.dir,1f*bullet.getSpeed()), bullet.dir,bullet.getSpeed(),bullet.getDamage());
		}
		
		// The move location will move around influenced by bullets and trees
		MapLocation moveLocation = myLocation;
		
		// Apply Anti Gravity from each bullet in its current or future position
		for(int i = bullets.length;i-->0;) {
			
			boolean goingAway = (Math.cos(myLocation.directionTo(bullets[i].location).radiansBetween(bullets[i].dir)) > 0);
			boolean couldHit = bullets[i].location.distanceTo(myLocation) < 3f;
			if(goingAway && !couldHit) {
				rc.setIndicatorDot(bullets[i].location, 0, 255, 0);
			} else {
				rc.setIndicatorDot(bullets[i].location, 255, 0, 0);
			}
			
			// If we might intersect the bullet in its current position on our next move,
			// decide where to move based on its current position
			if(couldHit) {
				//rc.setIndicatorDot(bullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(bullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(bullets[i].location.distanceTo(myLocation))));
				//rc.setIndicatorLine(bullets[i].location, bullets[i].location.add(bullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
			else if(!goingAway){
				//rc.setIndicatorDot(futureBullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), Math.max(2f, 2f/(futureBullets[i].location.distanceTo(myLocation))));
				//rc.setIndicatorLine(futureBullets[i].location, futureBullets[i].location.add(futureBullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
			
		}
		
		// Apply Anti Gravity for close trees
		for(int i = trees.length;i-->0;) {
			if(myLocation.distanceTo(trees[i].location) > trees[i].radius + rc.getType().bodyRadius + 2f) {
				continue;
			}
			rc.setIndicatorDot(trees[i].location, 0, 50, 50);
			moveLocation = moveLocation.add(trees[i].location.directionTo(myLocation), 1f);
		}
		
		// Apply Anti Gravity for map edges
		if (!rc.onTheMap(myLocation.add(Direction.NORTH, 2f))) moveLocation=moveLocation.add(Direction.SOUTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 2f))) moveLocation=moveLocation.add(Direction.NORTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.WEST, 2f))) moveLocation=moveLocation.add(Direction.EAST, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.EAST, 2f))) moveLocation=moveLocation.add(Direction.WEST, bullets.length);
		
		rc.setIndicatorDot(moveLocation, 255, 255, 255);
		Direction moveDirection = myLocation.directionTo(moveLocation);
		if(moveDirection == null) return null;
		moveDirection = tryMove(rc, moveDirection, 5f, 24, bullets);
		
		// Rescale stride 
		float moveStride = (float) (2 - 1f/Math.pow(myLocation.distanceTo(moveLocation)+0.7, 2));
		moveLocation = myLocation.add(moveDirection, moveStride);
		
		rc.setIndicatorDot(moveLocation, 0, 0, 0);
		
		//rc.setIndicatorLine(myLocation,moveLocation, 255, 20, 10);
		
		return moveLocation;
	}
	
public static MapLocation awayFromBullets(RobotController rc, MapLocation myLocation, BulletInfo[] bullets) throws GameActionException {
		
		// Time step all the bullets forward by 1 turn
		BulletInfo[] futureBullets = new BulletInfo[bullets.length];
		for(int i = bullets.length;i-->0;) {
			BulletInfo bullet = bullets[i];
			futureBullets[i] = new BulletInfo(bullet.ID, bullet.location.add(bullet.dir,1f*bullet.getSpeed()), bullet.dir,bullet.getSpeed(),bullet.getDamage());
		}
		
		// The move location will move around influenced by bullets and trees
		MapLocation moveLocation = myLocation;
		
		// Apply Anti Gravity from each bullet in its current or future position
		for(int i = bullets.length;i-->0;) {
			
			boolean goingAway = (Math.cos(myLocation.directionTo(bullets[i].location).radiansBetween(bullets[i].dir)) > 0);
			boolean couldHit = bullets[i].location.distanceTo(myLocation) < 3f;
			if(goingAway && !couldHit) {
				rc.setIndicatorDot(bullets[i].location, 0, 255, 0);
			} else {
				rc.setIndicatorDot(bullets[i].location, 255, 0, 0);
			}
			
			// If we might intersect the bullet in its current position on our next move,
			// decide where to move based on its current position
			if(couldHit) {
				//rc.setIndicatorDot(bullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(bullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(bullets[i].location.distanceTo(myLocation))));
				//rc.setIndicatorLine(bullets[i].location, bullets[i].location.add(bullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
			else if(!goingAway){
				//rc.setIndicatorDot(futureBullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), Math.max(2f, 2f/(futureBullets[i].location.distanceTo(myLocation))));
				//rc.setIndicatorLine(futureBullets[i].location, futureBullets[i].location.add(futureBullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
			
		}
		
		// Apply Anti Gravity for map edges
		if (!rc.onTheMap(myLocation.add(Direction.NORTH, 2f))) moveLocation=moveLocation.add(Direction.SOUTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 2f))) moveLocation=moveLocation.add(Direction.NORTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.WEST, 2f))) moveLocation=moveLocation.add(Direction.EAST, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.EAST, 2f))) moveLocation=moveLocation.add(Direction.WEST, bullets.length);
		
		rc.setIndicatorDot(moveLocation, 255, 255, 255);
		Direction moveDirection = myLocation.directionTo(moveLocation);
		if(moveDirection == null) return null;
		moveDirection = tryMove(rc, moveDirection, 5f, 24, bullets);
		
		// Rescale stride 
		float moveStride = (float) (2 - 1f/Math.pow(myLocation.distanceTo(moveLocation)+0.7, 2));
		moveLocation = myLocation.add(moveDirection, moveStride);
		
		rc.setIndicatorDot(moveLocation, 0, 0, 0);
		
		//rc.setIndicatorLine(myLocation,moveLocation, 255, 20, 10);
		
		return moveLocation;
	}
	
public static MapLocation awayFromBullets(RobotController rc, MapLocation myLocation, BulletInfo[] bullets, RobotInfo[] bots) throws GameActionException {
	
	// Time step all the bullets forward by 1 turn
	BulletInfo[] futureBullets = new BulletInfo[bullets.length];
	for(int i = bullets.length;i-->0;) {
		BulletInfo bullet = bullets[i];
		futureBullets[i] = new BulletInfo(bullet.ID, bullet.location.add(bullet.dir,1f*bullet.getSpeed()), bullet.dir,bullet.getSpeed(),bullet.getDamage());
	}
	
	// The move location will move around influenced by bullets and trees
	MapLocation moveLocation = myLocation;
	
	// Apply Anti Gravity from each bullet in its current or future position
	for(int i = bullets.length;i-->0;) {
		
		boolean goingAway = (Math.cos(myLocation.directionTo(bullets[i].location).radiansBetween(bullets[i].dir)) > 0);
		boolean couldHit = bullets[i].location.distanceTo(myLocation) < 3f;
		if(goingAway && !couldHit) {
			rc.setIndicatorDot(bullets[i].location, 0, 255, 0);
		} else {
			rc.setIndicatorDot(bullets[i].location, 255, 0, 0);
		}
		
		// If we might intersect the bullet in its current position on our next move,
		// decide where to move based on its current position
		if(couldHit) {
			//rc.setIndicatorDot(bullets[i].location, 255, 50, 50);
			moveLocation = moveLocation.add(bullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(bullets[i].location.distanceTo(myLocation))));
			//rc.setIndicatorLine(bullets[i].location, bullets[i].location.add(bullets[i].location.directionTo(myLocation)), 255, 0, 0);
		}
		else if(!goingAway){
			//rc.setIndicatorDot(futureBullets[i].location, 255, 50, 50);
			moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), Math.max(2f, 2f/(futureBullets[i].location.distanceTo(myLocation))));
			//rc.setIndicatorLine(futureBullets[i].location, futureBullets[i].location.add(futureBullets[i].location.directionTo(myLocation)), 255, 0, 0);
		}
		
	}
	
	// Apply Anti Gravity for close bots
	for(int i = bots.length;i-->0;) {
		if(myLocation.distanceTo(bots[i].location) > 3f) {
			continue;
		}
		rc.setIndicatorDot(bots[i].location, 0, 50, 50);
		moveLocation = moveLocation.add(bots[i].location.directionTo(myLocation), (bots[i].type == RobotType.GARDENER || bots[i].type == RobotType.ARCHON) ? 0f : 1f);
	}
	
	// Apply Anti Gravity for map edges
	if (!rc.onTheMap(myLocation.add(Direction.NORTH, 2f))) moveLocation=moveLocation.add(Direction.SOUTH, bullets.length);
	if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 2f))) moveLocation=moveLocation.add(Direction.NORTH, bullets.length);
	if (!rc.onTheMap(myLocation.add(Direction.WEST, 2f))) moveLocation=moveLocation.add(Direction.EAST, bullets.length);
	if (!rc.onTheMap(myLocation.add(Direction.EAST, 2f))) moveLocation=moveLocation.add(Direction.WEST, bullets.length);
	
	rc.setIndicatorDot(moveLocation, 255, 255, 255);
	Direction moveDirection = myLocation.directionTo(moveLocation);
	if(moveDirection == null) return null;
	moveDirection = tryMove(rc, moveDirection, 5f, 24, bullets);
	
	// Rescale stride 
	float moveStride = (float) (2 - 1f/Math.pow(myLocation.distanceTo(moveLocation)+0.7, 2));
	moveLocation = myLocation.add(moveDirection, moveStride);
	
	rc.setIndicatorDot(moveLocation, 0, 0, 0);
	
	//rc.setIndicatorLine(myLocation,moveLocation, 255, 20, 10);
	
	return moveLocation;
}

	static Direction tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide, BulletInfo[] bullets)
			throws GameActionException {

		//MapLocation myLocation = rc.getLocation();
		//MapLocation moveLocation;
		
		// First, try intended direction
		//moveLocation = myLocation.add(dir,rc.getType().strideRadius);
		if (rc.canMove(dir) /*&& isSafeLocation(rc, moveLocation, bullets)*/) {
			heading = dir;
			return dir;
		}

		// Now try a bunch of similar angles
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			//moveLocation = myLocation.add(dir.rotateLeftDegrees(degreeOffset * currentCheck),rc.getType().strideRadius);
			if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck)) /*&& Nav.isSafeLocation(rc, moveLocation, bullets)*/) {
				heading = dir.rotateLeftDegrees(degreeOffset * currentCheck);
				return heading;
			}
			// Try the offset on the right side
			//moveLocation = myLocation.add(dir.rotateRightDegrees(degreeOffset * currentCheck),rc.getType().strideRadius);
			if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck)) /*&& Nav.isSafeLocation(rc, moveLocation, bullets)*/) {
				heading = dir.rotateRightDegrees(degreeOffset * currentCheck);
				return heading;
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		//System.out.println("I'm stuck! :( I have "+
		//Clock.getBytecodesLeft()+" bytecodes left");
		return null;
	}

	public static Direction explore(RobotController rc, BulletInfo[] bullets) throws GameActionException {
    	
		Direction moveDirection;
		
		MapLocation myLocation = rc.getLocation();
		if(heading == null) {
			heading = randomDirection();
		}
    	int moveAttemptCount = 0;
    	while(moveAttemptCount < 5) {
    		moveDirection = Nav.tryMove(rc, heading, 5f, 24, bullets);
    		if(moveDirection != null && rc.onTheMap(myLocation.add(heading,rc.getType().strideRadius + rc.getType().bodyRadius ),rc.getType().bodyRadius+2f) 
					&& rc.canMove(moveDirection)) {
    				return moveDirection;
    		}
    		Direction lastHeading = heading;
    		while(lastHeading.degreesBetween(heading) < 30) {
    			heading = Nav.randomDirection();
    		}
    		moveAttemptCount++;
    	}
    	return null;
    }
	
	public static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
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
	
	static MapLocation pathTo(RobotController rc, MapLocation goal, BulletInfo[] bullets) throws GameActionException {
		RobotType[] avoid = new RobotType[0];
		return pathTo(rc, goal, avoid, rc.getType().strideRadius, bullets); 
	}
	
	static MapLocation pathTo(RobotController rc, MapLocation goal, RobotType[] avoid, BulletInfo[] bullets) throws GameActionException {
		return pathTo(rc, goal, avoid, rc.getType().strideRadius, bullets); 
	}

	static MapLocation pathTo(RobotController rc, MapLocation goal, RobotType[] avoid, float stride, BulletInfo[] bullets) throws GameActionException {
		
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
		int tries = 6;
		Direction trial;

		// Idea: if we can go closer to the goal than we ever have before, do so.
		for (int i = 0; i < 3; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				return myLocation.add(trial, stride);
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin
					&& !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				return myLocation.add(trial, stride);
			}
		}

		// If we're a lumberjack, stop thinking too hard and chop your way through
		/*
		if(rc.getType() == RobotType.LUMBERJACK && !rc.hasAttacked()){
			TreeInfo[] trees = rc.senseNearbyTrees(2f, Team.NEUTRAL);
			if(trees.length > 0) rc.chop(trees[0].getID());
			return false;
		}
		*/
		// Else, let's start following a wall.
		if (moveState == MoveState.TOWARD_LEFT) {
			moveState = moveState.LEFT;
		} else if (moveState == MoveState.TOWARD_RIGHT) {
			moveState = moveState.RIGHT;
		}

		switch (moveState) {
			case LEFT:
				for (int i = 0; i < tries; i++) {
					trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						return myLocation.add(trial, stride);
					}
				}
				break;
			case RIGHT:
				for (int i = 0; i < tries; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					if (rc.canMove(trial, stride) && !inEnemySight(rc, trial, avoid, enemyList, myLocation, stride)) {
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						myLocation.add(trial, stride);
						return myLocation.add(trial, stride);
					}
				}
				break;
			default:
				System.out.println("PATHING SHOULDN'T GET HERE!");
				break;
		}

		moveState = (moveState == moveState.LEFT) ? moveState.RIGHT : moveState.LEFT;
		return null;

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
	
	
}
