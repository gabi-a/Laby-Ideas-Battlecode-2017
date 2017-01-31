package onevonetests;
import java.util.*;
import battlecode.common.*;

public class BulletsAndSets {

	public static int[] intersection(int[] arrayA, int[] arrayB) {
		//System.out.println("Bytecodes before intersect(): "+Clock.getBytecodeNum());
		
		int[] intersection = new int[Math.min(arrayA.length, arrayB.length)];
		int index = 0;
		
		String stringA = Arrays.toString(arrayA);
		//System.out.println(stringA);
		for(int i = arrayB.length;i-->0;) {
			if(stringA.contains(Integer.toString(arrayB[i]))) {
				intersection[index] = arrayB[i];
				index++;
			}
		}
		return intersection;
	}
	
	static enum VennDiagramDirs {
		NORTH, CLOSE_NORTH, NORTH_WEST, NORTH_EAST,
		SOUTH, CLOSE_SOUTH, SOUTH_WEST, SOUTH_EAST,
		WEST, CLOSE_WEST,
		EAST, CLOSE_EAST
	};
	
	static MapLocation useVennDiagramsToDodgeBullets(float strideRadius, MapLocation myLocation, BulletInfo[] bullets) {
		
		final int[] bulletIDsInCircleNorth = new int[bullets.length];
		for(int i = bullets.length;i-->0;) {
			boolean hit = calcHit(bullets[i], myLocation.add(Direction.NORTH, 0.333f * strideRadius), 0.666f *  1.1f * strideRadius);
			if(hit) bulletIDsInCircleNorth[i] = bullets[i].ID;
		}
		final int[] bulletIDsInCircleSouth = new int[bullets.length];
		for(int i = bullets.length;i-->0;) {
			boolean hit = calcHit(bullets[i], myLocation.add(Direction.SOUTH, 0.333f * strideRadius), 0.666f * 1.1f * strideRadius);
			if(hit) bulletIDsInCircleSouth[i] = bullets[i].ID;
		}
		final int[] bulletIDsInCircleWest = new int[bullets.length];
		for(int i = bullets.length;i-->0;) {
			boolean hit = calcHit(bullets[i], myLocation.add(Direction.WEST, 0.333f * strideRadius), 0.666f * 1.1f * strideRadius);
			if(hit) bulletIDsInCircleWest[i] = bullets[i].ID;
		}
		final int[] bulletIDsInCircleEast = new int[bullets.length];
		for(int i = bullets.length;i-->0;) {
			boolean hit = calcHit(bullets[i], myLocation.add(Direction.EAST, 0.333f * strideRadius), 0.666f * 1.1f * strideRadius);
			if(hit) bulletIDsInCircleEast[i] = bullets[i].ID;
		}
		
		int[] bulletIDsInNorthWest = new int[bullets.length];
		int[] bulletIDsInNorthEast = new int[bullets.length];
		int[] bulletIDsInSouthWest = new int[bullets.length];
		int[] bulletIDsInSouthEast = new int[bullets.length];
		bulletIDsInNorthWest = intersection(bulletIDsInCircleNorth, bulletIDsInCircleWest);
		bulletIDsInNorthEast = intersection(bulletIDsInCircleNorth, bulletIDsInCircleEast);
		bulletIDsInSouthWest = intersection(bulletIDsInCircleSouth, bulletIDsInCircleWest);
		bulletIDsInSouthEast = intersection(bulletIDsInCircleSouth, bulletIDsInCircleEast);
		
		int[] bulletIDsInCloseNorth = new int[bullets.length];
		int[] bulletIDsInCloseSouth = new int[bullets.length];
		int[] bulletIDsInCloseWest = new int[bullets.length];
		int[] bulletIDsInCloseEast = new int[bullets.length];
		bulletIDsInCloseNorth = intersection(bulletIDsInNorthWest, bulletIDsInNorthEast);
		bulletIDsInCloseSouth = intersection(bulletIDsInSouthWest, bulletIDsInSouthEast);
		bulletIDsInCloseWest = intersection(bulletIDsInNorthWest, bulletIDsInSouthWest);
		bulletIDsInCloseEast = intersection(bulletIDsInNorthEast, bulletIDsInSouthEast);
		
		int bulletCountNorth = 0;
		int bulletCountSouth = 0;	
		int bulletCountWest = 0;	
		int bulletCountEast = 0;
		
		int bulletCountNorthWest = 0;
		int bulletCountNorthEast = 0;	
		int bulletCountSouthWest = 0;	
		int bulletCountSouthEast = 0;
		
		int bulletCountCloseNorth = 0;
		int bulletCountCloseSouth = 0;	
		int bulletCountCloseWest = 0;	
		int bulletCountCloseEast = 0;
		
		for(int i = bullets.length;i-->0;) {
			if(bulletIDsInCircleNorth[i] != 0) bulletCountNorth++;
			if(bulletIDsInCircleSouth[i] != 0) bulletCountSouth++;
			if(bulletIDsInCircleWest[i] != 0) bulletCountWest++;
			if(bulletIDsInCircleEast[i] != 0) bulletCountEast++;
			
			if(bulletIDsInNorthWest[i] != 0) bulletCountNorthWest++;
			if(bulletIDsInNorthEast[i] != 0) bulletCountNorthEast++;
			if(bulletIDsInSouthWest[i] != 0) bulletCountSouthWest++;
			if(bulletIDsInSouthEast[i] != 0) bulletCountSouthEast++;
			
			if(bulletIDsInCloseNorth[i] != 0) bulletCountCloseNorth++;
			if(bulletIDsInCloseSouth[i] != 0) bulletCountCloseSouth++;
			if(bulletIDsInCloseWest[i] != 0) bulletCountCloseWest++;
			if(bulletIDsInCloseEast[i] != 0) bulletCountCloseEast++;
		}
		bulletCountNorth = bulletCountNorth - bulletCountNorthWest - bulletCountNorthEast + bulletCountCloseNorth;
		bulletCountSouth = bulletCountSouth - bulletCountSouthWest - bulletCountSouthEast + bulletCountCloseSouth;
		bulletCountWest = bulletCountWest - bulletCountNorthWest - bulletCountSouthWest + bulletCountCloseWest;
		bulletCountEast = bulletCountEast - bulletCountNorthEast - bulletCountSouthEast + bulletCountCloseEast;
		
		int[] bulletsInDirections = {
			bulletCountNorth, bulletCountCloseNorth, bulletCountNorthWest, bulletCountNorthEast,
			bulletCountSouth, bulletCountCloseSouth, bulletCountSouthWest, bulletCountSouthEast,
			bulletCountWest, bulletCountCloseWest, 
			bulletCountEast, bulletCountCloseEast 
		};
		
		int minBullets = 1000;
		MapLocation bestMove = myLocation;
		for(int i = bulletsInDirections.length;i-->0;) {
			MapLocation moveLocation = getMoveLocation(strideRadius, myLocation, i);
			if(bulletsInDirections[i] < minBullets && RobotPlayer.rc.canMove(moveLocation)) {
				minBullets = bulletsInDirections[i];
				bestMove = moveLocation;
			}
		}
		
		return bestMove;
	}
	
	static MapLocation getMoveLocation(float strideRadius, MapLocation myLocation, int bestDirection) {
		switch(VennDiagramDirs.values()[bestDirection]) {
		case NORTH:
			return myLocation.add(Direction.NORTH, strideRadius);
		case CLOSE_NORTH:
			return myLocation.add(Direction.NORTH, strideRadius * 0.6f);
		case NORTH_WEST:
			return myLocation.add(Direction.NORTH).add(Direction.WEST);
		case NORTH_EAST:
			return myLocation.add(Direction.NORTH).add(Direction.EAST);
		case SOUTH:
			return myLocation.add(Direction.SOUTH, strideRadius);
		case CLOSE_SOUTH:
			return myLocation.add(Direction.SOUTH, strideRadius * 0.6f);
		case SOUTH_WEST:
			return myLocation.add(Direction.SOUTH).add(Direction.WEST);
		case SOUTH_EAST:
			return myLocation.add(Direction.SOUTH).add(Direction.EAST);
		case WEST:
			return myLocation.add(Direction.WEST, strideRadius);
		case CLOSE_WEST:
			return myLocation.add(Direction.WEST, strideRadius * 0.6f);
		case EAST:
			return myLocation.add(Direction.EAST, strideRadius);
		case CLOSE_EAST:
			return myLocation.add(Direction.EAST, strideRadius * 0.6f);
		}
		return null;
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
	
}
