package SprintBot;

import battlecode.common.*;

public class Nav {

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

	static boolean treeBug(RobotController rc) throws GameActionException {

		MapLocation myLocation = rc.getLocation();

		switch (treeBugHand) {

		case LEFT:

			// Uses 100 bytecodes
			TreeInfo[] leftTrees = rc.senseNearbyTrees(myLocation.add(treeBugHeading.rotateLeftDegrees(90), 2f), 1f,
					null);

			if (leftTrees.length > 0) {
				if (Nav.tryMove(rc, treeBugHeading)) {
					return true;
				} else {
					treeBugHeading = treeBugHeading.rotateRightDegrees(90);
				}
			} else {
				treeBugHeading = treeBugHeading.rotateLeftDegrees(90);
			}
			break;

		case RIGHT:

			// Uses 100 bytecodes
			TreeInfo[] rightTrees = rc.senseNearbyTrees(myLocation.add(treeBugHeading.rotateRightDegrees(90), 2f), 1f,
					null);

			if (rightTrees.length > 0) {
				if (Nav.tryMove(rc, treeBugHeading)) {
					return true;
				} else {
					treeBugHeading = treeBugHeading.rotateLeftDegrees(90);
				}
			} else {
				treeBugHeading = treeBugHeading.rotateRightDegrees(90);
			}

			break;

		}

		if (Nav.tryMove(rc, treeBugHeading)) {
			return true;
		} else {
			if(!Nav.explore(rc)) {
				treeBugHand = (treeBugHand == TreeBugHands.RIGHT) ? TreeBugHands.LEFT : TreeBugHands.RIGHT;
			}
		}

		return false;

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

	public static boolean explore(RobotController rc) throws GameActionException {
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

	public static Direction randomDirection() {
		return new Direction((float) Math.random() * 2 * (float) Math.PI);
	}

}
