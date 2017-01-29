package smoothmoves;
import battlecode.common.*;

public class Nav {
	
	static final int maxBulletsToAvoid = 10;
	
	static Direction heading = randomDirection();
	
	public static MapLocation awayFromBullets(RobotController rc, MapLocation myLocation, BulletInfo[] bullets) throws GameActionException {

		//BulletInfo[] bulletsCouldHit = bullets.clone();
		//System.out.format("Start of dodge bytecodes: %d\n", Clock.getBytecodeNum());
		int numBullets = Math.min(20, bullets.length);
		BulletInfo[] bulletsToAvoid = new BulletInfo[numBullets];
		for(int i = 0; i < numBullets; i++) {
			//System.out.format("Angle: %f Bullet location:"+bullets[i].location+"\n", Math.abs(bullets[i].dir.degreesBetween(bullets[i].location.directionTo(myLocation))));
			if (Math.abs(bullets[i].dir.degreesBetween(bullets[i].location.directionTo(myLocation))) < 70 || bullets[i].location.distanceTo(myLocation) < 4f) {
				bulletsToAvoid[i] = bullets[i];
			}
		}
		float bulletX, bulletY;
		float leastIntersections = 1000f;
		Direction leastRay = Direction.getNorth();
		int bulletsNeededToDodge = 0;
		//outer:
		for (float rayAng = 6.2831853f; (rayAng -= Math.PI/3f) > 0;) {
			Direction rayDir = new Direction(rayAng);
			if ( !rc.canMove(myLocation.add(rayDir, 2f)) || rc.senseNearbyBullets(myLocation.add(rayDir, 2f), 2f).length != 0 ) continue;
			float rayX = rayDir.getDeltaX(1);
			float rayY = rayDir.getDeltaY(1);
			float intersections = 0;
			for (int i = bulletsToAvoid.length; i --> 0;) {
				if (bulletsToAvoid[i] == null) continue;
				if(Clock.getBytecodeNum() > 9000) System.out.format("Bytecodes: %d, bullets: %d\n",Clock.getBytecodeNum(),bulletsToAvoid.length);
				bulletX = bulletsToAvoid[i].dir.getDeltaX(1f);
				bulletY = bulletsToAvoid[i].dir.getDeltaY(1f);
				Direction relDir = myLocation.directionTo(bulletsToAvoid[i].location);
				float relX = relDir.getDeltaX(1);
				float relY = relDir.getDeltaY(1);
				
				// You are not expected to understand this.
				double test = Math.pow(bulletX - rayX + relX, 2) + Math.pow(bulletY - rayY + relY, 2);
				if (test < 1d && test > 0.3d) {
					intersections += 1f/(myLocation.add(rayDir, 2f).distanceTo(bulletsToAvoid[i].location));
					//System.out.println((myLocation.add(rayDir, 2f).distanceTo(bulletsToAvoid[i].location)));
					if(myLocation.distanceTo(bulletsToAvoid[i].location) < 4f) bulletsNeededToDodge++;
					//rc.setIndicatorLine(myLocation.add(rayDir, 2f), bullets[i].location, 50, 10, 10);
					rc.setIndicatorDot(bulletsToAvoid[i].location, 100, 200, 0);
				} else {
					rc.setIndicatorDot(bulletsToAvoid[i].location, 0, 100, 200);
				}
				if(Clock.getBytecodeNum() > 9000) System.out.format("Bytecodes: %d, bullets: %d\n",Clock.getBytecodeNum(),bulletsToAvoid.length);
				//if(Clock.getBytecodesLeft() < 4000) break outer;
			}
			//rc.setIndicatorLine(myLocation, myLocation.add(rayDir, 2f), (int) (100/intersections), (int) (100/intersections),(int) (100/intersections));
			if (intersections < leastIntersections) {
				leastRay = rayDir;
				leastIntersections = intersections;
			}
		}
		if(bulletsNeededToDodge == 0) return null;
		return myLocation.add(Nav.tryMove(rc, leastRay, 5f, 24, bullets), rc.getType().strideRadius);
		
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
	
	static MapLocation pathTo(RobotController rc, MapLocation goal) throws GameActionException {
		return pathTo(rc, goal, rc.getType().strideRadius); 
	}
	
	static MapLocation pathTo(RobotController rc, MapLocation goal, float stride) throws GameActionException {
				
		MapLocation myLocation = rc.getLocation();
		
		// If this is the first time going here, clear our pathing memory
		if (goal.distanceTo(goalCache) > 5f) {
			goalCache = goal;
			dMin = 10000f;
			moveState = MoveState.TOWARD_LEFT;
		}
		
		goalCache = goal;

		float degreeOffset = 31f;
		int tries = 6;
		Direction trial;
		
		if(myLocation.distanceTo(goal) <= 2.1f) {
			rc.setIndicatorDot(myLocation, 255, 255, 255);
			return myLocation;
		}

		// Idea: if we can go closer to the goal than we ever have before, do so.
		for (int i = 0; i < 3; i++) {
			trial = new Direction(myLocation, goal).rotateLeftDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin) {
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				heading = trial;
				return myLocation.add(trial, stride);
			}
			trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
			if (rc.canMove(trial, stride) && myLocation.add(trial, stride).distanceTo(goal) < dMin) {
				dMin = myLocation.add(trial, stride).distanceTo(goal);
				moveState = chooseMoveState();
				heading = trial;
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
					//rc.setIndicatorLine(myLocation, myLocation.add(trial), 255, 0, 0);
					if (rc.canMove(trial, stride)) {
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						heading = trial;
						return myLocation.add(trial, stride);
					}
				}
				break;
			case RIGHT:
				for (int i = 0; i < tries; i++) {
					trial = new Direction(myLocation, goal).rotateRightDegrees(degreeOffset * i);
					//rc.setIndicatorLine(myLocation, myLocation.add(trial), 0, 255, 0);
					if (rc.canMove(trial, stride)) {
						dMin = Math.min(dMin, myLocation.add(trial, stride).distanceTo(goal));
						heading = trial;
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
	
}

/*	
// Get an array containing only the bullets that might hit us
int numBulletsCouldHit = 0;
for(int i = 0; i < bullets.length; i++) {
	boolean goingAway = (Math.cos(myLocation.directionTo(bullets[i].location).radiansBetween(bullets[i].dir)) > 0);
	boolean couldHit = bullets[i].location.distanceTo(myLocation) < 3f;
	if(goingAway && !couldHit) {
		rc.setIndicatorDot(bullets[i].location, 0, 200, 100);
		bulletsCouldHit[i] = null;
		continue;
	}
	numBulletsCouldHit++;
}
*/

