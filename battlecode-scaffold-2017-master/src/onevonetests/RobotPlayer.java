package onevonetests;
import battlecode.common.*;
import onevonetests.Nav;
import turtlebot.*;

public class RobotPlayer {

 	static RobotController rc;
 	static int gardenersCount = 0;
 	static int botsBuilt = 0;
 	static TreeInfo target;
 	
 	public static void run(RobotController rc) throws GameActionException {
 		RobotPlayer.rc = rc;
 		while(true) {
 			//int[] arrayA = {1,5,2,7,4,56};
 		    //int[] arrayB = {56,2,6,1,65,4,5};
 		    //int[] intersect = BulletsAndSets.intersection(arrayA, arrayB);
 		    //for(int e : intersect) System.out.println(e);
 		   
 			try {
 				switch (rc.getType()) {
			    case ARCHON:
			        runArchon();
			        break;
			    case GARDENER:
			        runGardener();
			        break;
			    case SOLDIER:
			        runSoldier();
			        break;
			    case LUMBERJACK:
			        runLumberjack();
			        break;
				}
				Clock.yield();
			}	catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
    }

	private static void runLumberjack() throws GameActionException {
		// TODO Auto-generated method stub
		
		BotLumberjack.turn(rc);
		
	}

	private static void runSoldier() throws GameActionException {
		// TODO Auto-generated method stub
		
		switch(rc.getTeam()) {
		
		case A:{
			BulletInfo[] bullets = rc.senseNearbyBullets();
			RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
			//if(bullets.length > 0) {
			//	MapLocation myLocation = rc.getLocation();
			//	MapLocation moveLocation = Nav.dontGetHit(rc, myLocation, bullets, enemies.length == 0 ? null : enemies[0].location);
			//	if(moveLocation != myLocation && rc.canMove(myLocation.directionTo(moveLocation))) rc.move(myLocation.directionTo(moveLocation));
			//}
			if(enemies.length > 0) {
			//	if(!rc.hasMoved() && rc.canMove(rc.getLocation().directionTo(enemies[0].location))) rc.move(rc.getLocation().directionTo(enemies[0].location));
				if(rc.canFireTriadShot()) rc.fireTriadShot(rc.getLocation().directionTo(enemies[0].location));
			}
			break;}
			//No break so that it fires
		case B:{
			BulletInfo[] bullets = rc.senseNearbyBullets();
			RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
			if(bullets.length > 0) {
				MapLocation myLocation = rc.getLocation();
	 			
				MapLocation moveLocation = BulletsAndSets.useVennDiagramsToDodgeBullets(rc.getType().bodyRadius, myLocation, bullets);
				Direction moveDirection = myLocation.directionTo(moveLocation);
				float moveStride = Math.min(rc.getType().strideRadius, myLocation.distanceTo(moveLocation));
				if(moveLocation != myLocation && rc.canMove(moveDirection, moveStride)) rc.move(moveDirection, moveStride);
			}
			//if(enemies.length > 0) {
			//	if(!rc.hasMoved() && rc.canMove(rc.getLocation().directionTo(enemies[0].location))) rc.move(rc.getLocation().directionTo(enemies[0].location));
			//	if(rc.canFireTriadShot()) rc.fireTriadShot(rc.getLocation().directionTo(enemies[0].location));
			//}
			break;}
		}
		
	}

	private static void runGardener() throws GameActionException {
		
	}

	private static void runArchon() throws GameActionException {
		
	}
	
	public static MapLocation awayFromBullets(RobotController rc, MapLocation myLocation, BulletInfo[] bullets) throws GameActionException {

		int numBullets = Math.min(12, bullets.length);
		BulletInfo[] bulletsToAvoid = new BulletInfo[numBullets];
		for(int i = 0; i < numBullets; i++) {
			bulletsToAvoid[i] = bullets[i];
		}
		
		System.out.println("Soldier is going to try avoid bullets");
		float bulletX, bulletY;
		float leastIntersections = 1000f;
		Direction leastRay = Direction.getNorth();
		for (float rayAng = 6.2831853f; (rayAng -= Math.PI/6f) > 0;) {
			Direction rayDir = new Direction(rayAng);
			if ( !rc.canMove(myLocation.add(rayDir, 2f)) || rc.senseNearbyBullets(myLocation.add(rayDir, 2f), 2f).length != 0 ) continue;
			float rayX = rayDir.getDeltaX(1);
			float rayY = rayDir.getDeltaY(1);
			float intersections = 0;
			for (int i = bulletsToAvoid.length; i --> 0;) {
				System.out.format("i: %d rayAng: %f bytecodes: %d\n", i, rayAng, Clock.getBytecodeNum());
				bulletX = bulletsToAvoid[i].dir.getDeltaX(1f);
				bulletY = bulletsToAvoid[i].dir.getDeltaY(1f);
				Direction relDir = myLocation.directionTo(bulletsToAvoid[i].location);
				float relX = relDir.getDeltaX(1);
				float relY = relDir.getDeltaY(1);
				
				// You are not expected to understand this.
				if (Math.pow(bulletX - rayX + relX, 2) + Math.pow(bulletY - rayY + relY, 2) < 1) {
					intersections += 1f/(myLocation.add(rayDir, 2f).distanceTo(bulletsToAvoid[i].location));
					System.out.println((myLocation.add(rayDir, 2f).distanceTo(bulletsToAvoid[i].location)));
					//rc.setIndicatorLine(myLocation.add(rayDir, 2f), bullets[i].location, 50, 10, 10);
				}
			}
			rc.setIndicatorLine(myLocation, myLocation.add(rayDir, 2f), (int) (100/intersections), (int) (100/intersections),(int) (100/intersections));
			if (intersections < leastIntersections) {
				leastRay = rayDir;
				leastIntersections = intersections;
			}
		}
		return myLocation.add(leastRay, rc.getType().strideRadius);
		
	}
	
}
