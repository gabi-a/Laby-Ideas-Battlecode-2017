package onemorego;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	static Team enemyTeam = RobotPlayer.rc.getTeam().opponent();
	static MapLocation enemyArchonLocation = RobotPlayer.rc.getInitialArchonLocations(enemyTeam)[0];
	static MapLocation targetLocation = enemyArchonLocation;
	
	static boolean exploreFlag = false;
	static Strategy strat = Strategy.OFFENSE;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		Util.reportDeath(rc);
	
		/* Offense:
		 * Try and get to AttackGroup B location, if any enemies are seen along the way
		 * start targeting them until they are dead 
		 * 
		 * Defense:
		 * Hang around the gardeners?
		 */

		MapLocation commsTarget = Comms.readAttackLocation(rc, AttackGroup.B);
		if(commsTarget != null) {
			if(commsTarget != targetLocation) {
				exploreFlag = false;
			}
			targetLocation = commsTarget;
		}
		
		BulletInfo[] bullets = rc.senseNearbyBullets(6f);
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemyTeam);
		
		MapLocation myLocation = rc.getLocation();
		MapLocation[] safeMoveLocations = Nav.getSafeMoveLocations(rc, bullets);
		
		TreeInfo[] trees = rc.senseNearbyTrees();
		RobotInfo closestEnemy = Util.getClosestEnemy(rc, enemies);
		
		switch(strat) {
		case OFFENSE:
			
			// Moving
			if(closestEnemy != null) {
				Nav.pathTo(rc, closestEnemy.location.add(closestEnemy.location.directionTo(myLocation), 2f), bullets);
			}
			else {
				if(!exploreFlag) {
					if(!Nav.pathTo(rc, targetLocation, bullets)) {
						Nav.explore(rc, bullets);
					}
					if(myLocation.distanceTo(targetLocation) < 5f) exploreFlag = true;
				} else {
					Nav.explore(rc, bullets);
				}
			}
			
			// Shooting
			if(closestEnemy != null) {
				
				MapLocation halfwayLocation = Util.halfwayLocation(myLocation, closestEnemy.location);
				float halfwayDistance = myLocation.distanceTo(closestEnemy.location);
				RobotInfo[] botsBetweenUs = rc.senseNearbyRobots(halfwayLocation, halfwayDistance, rc.getTeam());
				TreeInfo[] treesBetweenUs = rc.senseNearbyTrees(halfwayLocation, halfwayDistance, null);
				boolean goodToShoot = true;
				for(int i = botsBetweenUs.length; i-->0;) {
					if(Util.doesLineIntersectWithCircle(myLocation, closestEnemy.location, botsBetweenUs[i].location, botsBetweenUs[i].getRadius())) {
						goodToShoot = false;
						break;
					}
				}
				for(int i = treesBetweenUs.length; i-->0;) {
					if(Util.doesLineIntersectWithCircle(myLocation, closestEnemy.location, treesBetweenUs[i].location, treesBetweenUs[i].getRadius())) {
						goodToShoot = false;
						break;
					}
				}
				if(goodToShoot) {
					if(rc.canFireSingleShot()) {
						rc.fireSingleShot(myLocation.directionTo(closestEnemy.location));
					}
				}
				
			}
			
			break;
		case DEFENSE:
			
			break;
		}
	}
	
}
