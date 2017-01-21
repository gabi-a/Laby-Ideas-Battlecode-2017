package smoothmoves;
import battlecode.common.*;

public class Nav {
	
	
	public static MapLocation awayFromBulletsTreesAndBots(RobotController rc, MapLocation myLocation, BulletInfo[] bullets, TreeInfo[] trees, RobotInfo[] bots) throws GameActionException {
		
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
			if(bullets[i].location.distanceTo(myLocation) > 2f) {
				rc.setIndicatorDot(futureBullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(futureBullets[i].location.distanceTo(myLocation))));
				rc.setIndicatorLine(futureBullets[i].location, futureBullets[i].location.add(futureBullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
			else {
				rc.setIndicatorDot(bullets[i].location, 255, 50, 50);
				moveLocation = moveLocation.add(bullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(bullets[i].location.distanceTo(myLocation))));
				rc.setIndicatorLine(bullets[i].location, futureBullets[i].location.add(bullets[i].location.directionTo(myLocation)), 255, 0, 0);
			}
		}
		
		// Apply Anti Gravity from each bullet in its future position
		//for(int i = futureBullets.length;i-->0;) {
		//	rc.setIndicatorDot(futureBullets[i].location, 255, 50, 50);
		//	moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), Math.max(2f, 1f/(futureBullets[i].location.distanceTo(myLocation))));
		//	rc.setIndicatorLine(futureBullets[i].location, futureBullets[i].location.add(futureBullets[i].location.directionTo(myLocation)), 255, 0, 0);
		//}
		
		// Apply Anti Gravity for close trees
		for(int i = trees.length;i-->0;) {
			if(myLocation.distanceTo(trees[i].location) > trees[i].radius + RobotType.LUMBERJACK.bodyRadius + 2f) {
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
			moveLocation = moveLocation.add(bots[i].location.directionTo(myLocation), 1f);
		}
		
		// Apply Anti Gravity for map edges
		if (!rc.onTheMap(myLocation.add(Direction.NORTH, 2f))) moveLocation=moveLocation.add(Direction.SOUTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 2f))) moveLocation=moveLocation.add(Direction.NORTH, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.WEST, 2f))) moveLocation=moveLocation.add(Direction.EAST, bullets.length);
		if (!rc.onTheMap(myLocation.add(Direction.EAST, 2f))) moveLocation=moveLocation.add(Direction.WEST, bullets.length);
		
		rc.setIndicatorDot(moveLocation, 255, 255, 255);
		
		Direction moveDirection = tryMove(rc, myLocation.directionTo(moveLocation), 5f, 24, bullets);
		
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
				return dir.rotateRightDegrees(degreeOffset * currentCheck);
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
