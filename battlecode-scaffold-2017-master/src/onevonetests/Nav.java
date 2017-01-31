package onevonetests;
import battlecode.common.*;

public class Nav {

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
			tryPrecisionMove(rc, new Direction((float) Math.atan2(s, c)), 2f, 5, 2.5f - closestDistance);
			return true;
		}
		else {
			tryPrecisionMove(rc, (new Direction((float) Math.atan2(s, c)).opposite()), 2f, 5, 2.5f - closestDistance);
			return true;
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
	
	static MapLocation dontGetHit(RobotController rc, MapLocation myLocation, BulletInfo[] bullets, MapLocation closestEnemyLocation) {

		int innerCircleLocs = 8;
		int outerCircleLocs = 16;
		
		// Assumption: There is always a move where no bullets will hit
		
		MapLocation[] safeMoves = new MapLocation[innerCircleLocs + outerCircleLocs + 1];
		
		// First check if staying still is the best option (unlikely!)
		boolean hit = false;
		for(int i = bullets.length; i-->0;) {
			hit = calcHit(bullets[i], myLocation, rc.getType().bodyRadius);
			if(hit) break;
		}
		if(!hit) safeMoves[innerCircleLocs + outerCircleLocs] = myLocation;
		
		
		// Idea: Try an inner circle of possible moves, then an outer circle
		int locationAttempt = 0;
		
		// Try moves in the inner circle
		for(float rads = (float) (2*Math.PI); (rads -= 2*Math.PI / innerCircleLocs) > 0;) {
			Direction dir = new Direction(rads);
			MapLocation possibleMoveLocation = myLocation.add(dir, rc.getType().strideRadius);
			
			
			int hits = 0;
			for(int i = bullets.length; i-->0;) {
				hit = calcHit(bullets[i], possibleMoveLocation, rc.getType().bodyRadius / 2f);
				if(hit) hits++;
			}
			
			if(hits != 0) rc.setIndicatorDot(possibleMoveLocation, 255, 0, 0);
			if(hits == 0) rc.setIndicatorDot(possibleMoveLocation, 137, 172, 229);
			//rc.setIndicatorDot(possibleMoveLocation, 50 * hits, 172, 229);
			
			if(hits == 0) safeMoves[locationAttempt] = possibleMoveLocation;
			locationAttempt++;
		}
		
		// Now try moves in the outer circle
		for(float rads = (float) (2*Math.PI); (rads -= 2*Math.PI / outerCircleLocs) > 0;) {
			Direction dir = new Direction(rads);
			MapLocation possibleMoveLocation = myLocation.add(dir, rc.getType().strideRadius);
			
			int hits = 0;
			for(int i = bullets.length; i-->0;) {
				hit = calcHit(bullets[i], possibleMoveLocation, rc.getType().bodyRadius);
				if(hit) hits++; 
			}
			
			if(hits != 0) rc.setIndicatorDot(possibleMoveLocation, 255, 0, 0);
			if(hits == 0) rc.setIndicatorDot(possibleMoveLocation, 137, 172, 229);
			//rc.setIndicatorDot(possibleMoveLocation, 150, 172, 50 * hits);
			//System.out.println("Loc:"+possibleMoveLocation+" Hits:"+hits);
			
			if(hits == 0) safeMoves[locationAttempt] = possibleMoveLocation;
			locationAttempt++;
		}
		
		// Now find the best of the safe moves
		// If no enemy, just pick the first valid one
		if(closestEnemyLocation == null) {
			for(int i = safeMoves.length; i --> 0;) {
				if(safeMoves[i] != null && rc.canMove(safeMoves[i])) {
					//System.out.println("Returning safe location:"+safeMoves[i]);
					return safeMoves[i];
				}
			}
		}
		
		// Otherwise find the furthest from the enemy
		MapLocation bestLocation = myLocation;
		float maxDist = 0;
		for(int i = safeMoves.length; i --> 0;) {
			if(safeMoves[i] != null && rc.canMove(safeMoves[i])) {
				//rc.setIndicatorDot(safeMoves[i], 137, 172, 229);
				float dist = safeMoves[i].distanceTo(closestEnemyLocation);
				if(dist > maxDist) {
					maxDist = dist;
					bestLocation = safeMoves[i];
				}
			}
		}
		//System.out.println("Returning best location:"+bestLocation);
		return bestLocation;
		
	}
	
	static boolean calcHit(BulletInfo bullet, MapLocation targetCenter, float targetRadius) {
		
		if( Math.abs(bullet.dir.degreesBetween(bullet.location.directionTo(targetCenter))) % 360 > 30 ) return false; 
		
		MapLocation bulletFutureLocation = bullet.location.add(bullet.dir,bullet.speed);
		float hitDist = calcHitDist(bullet.location, bulletFutureLocation, targetCenter, targetRadius);
		return(hitDist >= 0);
	}
	
	static float calcHitDist(MapLocation bulletStart, MapLocation bulletFinish,
            MapLocation targetCenter, float targetRadius) {
		final float minDist = 0;
        final float maxDist = bulletStart.distanceTo(bulletFinish);
        final float distToTarget = bulletStart.distanceTo(targetCenter);
        final Direction toFinish = bulletStart.directionTo(bulletFinish);
        final Direction toTarget = bulletStart.directionTo(targetCenter);

        // If toTarget is null, then bullet is on top of center of unit, distance is zero
        if(toTarget == null) {
            return 0;
        }

        if(toFinish == null) {
            // This should never happen
            throw new RuntimeException("bulletStart and bulletFinish are the same.");
        }

        float radiansBetween = toFinish.radiansBetween(toTarget);

        //Check if the target intersects with the line made between the bullet points
        float perpDist = (float)Math.abs(distToTarget * Math.sin(radiansBetween));
        if(perpDist > targetRadius){
            return -1;
        }

        //Calculate hitDist
        float halfChordDist = (float)Math.sqrt(targetRadius * targetRadius - perpDist * perpDist);
        float hitDist = distToTarget * (float)Math.cos(radiansBetween);
        if(hitDist < 0){
            hitDist += halfChordDist;
            hitDist = hitDist >= 0 ? 0 : hitDist;
        }else{
            hitDist -= halfChordDist;
            hitDist = hitDist < 0 ? 0 : hitDist;
        }

        //Check invalid hitDists
        if(hitDist < minDist || hitDist > maxDist){
            return -1;
        }
        return hitDist;
	}
	
	static MapLocation reallyDontGetHit(RobotController rc, MapLocation myLocation, BulletInfo[] bullets) {

		final int innerCircleLocs = 8;
		final int outerCircleLocs = 8;
		
		MapLocation maxHitDistLoc = myLocation;
		float maxHitDist = 0;
		
		float minHitDist = 100;
		for(int i = bullets.length; i-->0;) {
			float hitDist = calcHitDist(bullets[i].location,bullets[i].location.add(bullets[i].dir,10*bullets[i].speed), myLocation, rc.getType().bodyRadius);
			if(hitDist != -1) {
				if(hitDist < minHitDist) {
					minHitDist = hitDist;
				}
			}
		}
		maxHitDist = minHitDist;
		System.out.println("\nMax: "+maxHitDist+" Min: "+minHitDist);
		
		for(float rads = (float) (2*Math.PI); (rads -= 2*Math.PI / innerCircleLocs) >= 0;) {
			Direction dir = new Direction(rads);
			MapLocation possibleMoveLoc = myLocation.add(dir, rc.getType().strideRadius / 2f);
			
			minHitDist = 100;
			for(int i = bullets.length; i-->0;) {
				float hitDist = calcHitDist(bullets[i].location,bullets[i].location.add(bullets[i].dir,10*bullets[i].speed), possibleMoveLoc, rc.getType().bodyRadius);
				if(hitDist != -1) {
					rc.setIndicatorDot(possibleMoveLoc, (int)(100 * hitDist), (int)(100 * hitDist), (int)(100 * hitDist));
					if(hitDist < minHitDist) {
						minHitDist = hitDist;
					}
				}
			}
			if(minHitDist > maxHitDist) {
				maxHitDist = minHitDist;
				maxHitDistLoc = possibleMoveLoc;
			}
		}
		System.out.println("Max: "+maxHitDist+" Min: "+minHitDist);
		for(float rads = (float) (2*Math.PI); (rads -= 2*Math.PI / outerCircleLocs) >= 0;) {
			Direction dir = new Direction(rads);
			MapLocation possibleMoveLoc = myLocation.add(dir, rc.getType().strideRadius);
			
			minHitDist = 100;
			for(int i = bullets.length; i-->0;) {
				float hitDist = calcHitDist(bullets[i].location,bullets[i].location.add(bullets[i].dir,10*bullets[i].speed), possibleMoveLoc, rc.getType().bodyRadius);
				if(hitDist != -1) {
					rc.setIndicatorDot(possibleMoveLoc, (int)(100 * hitDist), (int)(100 * hitDist), (int)(100 * hitDist));
					if(hitDist < minHitDist) {
						minHitDist = hitDist;
					}
				}
			}
			if(minHitDist > maxHitDist) {
				maxHitDist = minHitDist;
				maxHitDistLoc = possibleMoveLoc;
			}
		}
		System.out.println(maxHitDist);
		return maxHitDistLoc;
	}
	
	static MapLocation dodgeBulletsAndStuff(RobotController rc, MapLocation myLocation, BulletInfo[] bullets) {
		
		final int innerCircleLocs = 3;
		final int outerCircleLocs = 8;
		
		
		int hits = 0;
		for(int i = bullets.length; i-->0;) {
			boolean hit = calcHit(bullets[i], myLocation, rc.getType().bodyRadius);
			if(hit) hits++;
		}
		if(hits == 0) return myLocation;
		
		MapLocation minHitsLoc = myLocation;
		int minHits = hits;
		
		for(float rads = (float) (2*Math.PI); (rads -= 2*Math.PI / innerCircleLocs) >= 0;) {
			Direction dir = new Direction(rads);
			MapLocation possibleMoveLoc = myLocation.add(dir, rc.getType().strideRadius / 2f);
			hits = 0;
			for(int i = bullets.length; i-->0;) {
				boolean hit = calcHit(bullets[i], possibleMoveLoc, rc.getType().bodyRadius);
				if(hit) hits++;
			}
			if(hits == 0) return possibleMoveLoc;
			if(hits < minHits) {minHits = hits; minHitsLoc = possibleMoveLoc;}
		}
		
		for(float rads = (float) (2*Math.PI); (rads -= 2*Math.PI / outerCircleLocs) >= 0;) {
			Direction dir = new Direction(rads);
			MapLocation possibleMoveLoc = myLocation.add(dir, rc.getType().strideRadius);
			hits = 0;
			for(int i = bullets.length; i-->0;) {
				boolean hit = calcHit(bullets[i], possibleMoveLoc, rc.getType().bodyRadius);
				if(hit) hits++;
			}
			if(hits == 0) return possibleMoveLoc;
			if(hits < minHits) {minHits = hits; minHitsLoc = possibleMoveLoc;}
		}
		
		if(minHitsLoc == myLocation) {
			MapLocation goalLoc = myLocation;
			for(int i = bullets.length; i-->0;) {
				goalLoc = goalLoc.add(bullets[i].location.directionTo(myLocation));
			}
			System.out.println("Can't dodge bullets properly");
			return goalLoc;
		}
		System.out.println("Min hits: "+minHits);
		return minHitsLoc;
		
	}
	
	static MapLocation finalDodgeAttempt(float strideRadius, float bodyRadius, MapLocation myLocation, BulletInfo[] bullets) {
		
		// Step 1: Find the circle with the least bullets in it
		int minBulletsInTestLocation = 1000;
		MapLocation bestTestLocation = myLocation;
		for(float testRads = 2f * (float) Math.PI; (testRads -= 2f * (float) Math.PI / 6f) >= 0;) {
			Direction testDir = new Direction(testRads);
			MapLocation centerTestLocation = myLocation.add(testDir, strideRadius/2f);
			int bulletsInTestLocation = 0;
			RobotPlayer.rc.setIndicatorDot(myLocation.add(testDir, 10*strideRadius/2f), 255, 0, 0);
			for(int i = bullets.length;i-->0;) {
				if(bullets[i].location.add(bullets[i].dir,4 * bullets[i].speed).distanceTo(centerTestLocation) < 1f) {
					bulletsInTestLocation++;
				}
			}
			if(bulletsInTestLocation < minBulletsInTestLocation) {
				minBulletsInTestLocation = bulletsInTestLocation;
				bestTestLocation = centerTestLocation;
			}
		}

		RobotPlayer.rc.setIndicatorDot(myLocation.add(myLocation.directionTo(bestTestLocation), 10*strideRadius/2f), 0, 0, 0);
		
		// Step 2: Within the test circle find a better location
		MapLocation bestFinalTestLocation = bestTestLocation;
		for(float testRads = 2f * (float) Math.PI; (testRads -= 2f * (float) Math.PI / 6f) >= 0;) {
			Direction testDir = new Direction(testRads);
			MapLocation centerTestLocation = bestTestLocation.add(testDir, strideRadius/4f);
			int bulletsInTestLocation = 0;
			RobotPlayer.rc.setIndicatorDot(myLocation.add(myLocation.directionTo(bestTestLocation), 10*strideRadius/2f).add(testDir, 5 * strideRadius/4f), 0, 255, 0);
			for(int i = bullets.length;i-->0;) {
				if(calcHit(bullets[i], myLocation, bodyRadius)) {
					bulletsInTestLocation++;
				}
			}
			if(bulletsInTestLocation < minBulletsInTestLocation) {
				minBulletsInTestLocation = bulletsInTestLocation;
				bestFinalTestLocation = centerTestLocation;
			}
		}
		RobotPlayer.rc.setIndicatorDot(myLocation.add(myLocation.directionTo(bestTestLocation), 10*strideRadius/2f).add(bestTestLocation.directionTo(bestFinalTestLocation), 5 * strideRadius/4f), 0, 0, 0);
		return bestFinalTestLocation;
	}
}
