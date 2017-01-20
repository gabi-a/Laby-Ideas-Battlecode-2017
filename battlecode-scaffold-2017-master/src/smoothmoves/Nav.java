package smoothmoves;
import battlecode.common.*;

public class Nav {
	
	
	public static Direction awayFromBulletsAndTrees(RobotController rc, MapLocation myLocation, BulletInfo[] bullets, TreeInfo[] trees) throws GameActionException {
		
		// Time step all the bullets forward by 1 turn
		BulletInfo[] futureBullets = new BulletInfo[bullets.length];
		for(int i = bullets.length;i-->0;) {
			BulletInfo bullet = bullets[i];
			futureBullets[i] = new BulletInfo(bullet.ID, bullet.location.add(bullet.dir,bullet.getSpeed()), bullet.dir,bullet.getSpeed(),bullet.getDamage());
		}
		
		// The move location will move around influenced by bullets and trees
		MapLocation moveLocation = myLocation;
		
		// Apply Anti Gravity from each bullet
		for(int i = futureBullets.length;i-->0;) {
			rc.setIndicatorDot(futureBullets[i].location, 0, 50, 50);
			moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), 1f/(futureBullets[i].location.distanceTo(myLocation)));
		}
		
		// Apply Anti Gravity from each tree that isn't too close to the last tree
		/*
		TreeInfo previousTree = null;
		for(int i = trees.length;i-->0;) {
			if(previousTree != null && previousTree.location.distanceTo(trees[i].location) < trees[i].radius + previousTree.radius + 1f) {
				continue;
			}
			rc.setIndicatorDot(trees[i].location, 0, 50, 50);
			moveLocation = moveLocation.add(trees[i].location.directionTo(myLocation), 1f/(trees[i].location.distanceTo(myLocation)));
			previousTree = trees[i];
		}
		 */
		Direction moveDirection = myLocation.directionTo(moveLocation);
		
		rc.setIndicatorLine(myLocation,moveLocation, 255, 20, 10);
		
		return tryMove(rc, moveDirection, 5f, 12, bullets);
	}
	
	static Direction tryMove(RobotController rc, Direction dir, float degreeOffset, int checksPerSide, BulletInfo[] bullets)
			throws GameActionException {

		//MapLocation myLocation = rc.getLocation();
		//MapLocation moveLocation;
		
		// First, try intended direction
		//moveLocation = myLocation.add(dir,rc.getType().strideRadius);
		if (rc.canMove(dir) /*&& isSafeLocation(rc, moveLocation, bullets)*/) {
			return dir;
		}

		// Now try a bunch of similar angles
		int currentCheck = 1;

		while (currentCheck <= checksPerSide) {
			// Try the offset of the left side
			//moveLocation = myLocation.add(dir.rotateLeftDegrees(degreeOffset * currentCheck),rc.getType().strideRadius);
			if (rc.canMove(dir.rotateLeftDegrees(degreeOffset * currentCheck)) /*&& Nav.isSafeLocation(rc, moveLocation, bullets)*/) {
				return dir.rotateLeftDegrees(degreeOffset * currentCheck);
			}
			// Try the offset on the right side
			//moveLocation = myLocation.add(dir.rotateRightDegrees(degreeOffset * currentCheck),rc.getType().strideRadius);
			if (rc.canMove(dir.rotateRightDegrees(degreeOffset * currentCheck)) /*&& Nav.isSafeLocation(rc, moveLocation, bullets)*/) {
				return dir.rotateLeftDegrees(degreeOffset * currentCheck);
			}
			// No move performed, try slightly further
			currentCheck++;
		}

		// A move never happened, so return false.
		System.out.println("I'm stuck! :( I have "+
		Clock.getBytecodesLeft()+" bytecodes left");
		return null;
	}

}
