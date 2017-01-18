package onemorego;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	static Team enemyTeam = RobotPlayer.rc.getTeam().opponent();
	static MapLocation enemyArchonLocation = RobotPlayer.rc.getInitialArchonLocations(enemyTeam)[0];
	
	static MapLocation targetLocation = enemyArchonLocation;
	//static MapLocation secondaryTargetLocation = enemyArchonLocation;
	
	static boolean exploreFlag = false;
	static Strategy strat = Strategy.OFFENSE;
	
	static final float attackRadius = 6f;
	static final float attackRotAngle = (float) (2*Math.atan(RobotType.SOLDIER.bodyRadius/attackRadius));
	
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
		
		TreeInfo[] trees = rc.senseNearbyTrees();
		RobotInfo closestEnemy = Util.getClosestEnemy(rc, enemies);
		
		switch(strat) {
		case OFFENSE:
			
			// Moving
			if(closestEnemy != null) {
				
				MapLocation attackPosition = null;
				for(int i = 12; i-->0;) {
					attackPosition = closestEnemy.location.add(closestEnemy.location.directionTo(myLocation).rotateLeftRads(attackRotAngle*i), attackRadius);
					if(rc.canSenseAllOfCircle(attackPosition, 1f) && !rc.isCircleOccupiedExceptByThisRobot(attackPosition, 1f)) {
						break;
					}
				}
				if(attackPosition != null) {
					Nav.pathTo(rc, attackPosition, bullets);
				}
				
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
				
				boolean alreadyShot = false;
				
				if(myLocation.distanceTo(closestEnemy.location) < RobotType.SOLDIER.bodyRadius + closestEnemy.type.bodyRadius + 0.3f) {
					if(rc.canFirePentadShot()) {
						rc.firePentadShot(myLocation.directionTo(closestEnemy.location));
						alreadyShot = true;
					} else if(rc.canFireTriadShot()) {
						rc.fireTriadShot(myLocation.directionTo(closestEnemy.location));
						alreadyShot = true;
					} else if(rc.canFireSingleShot()) {
						rc.fireSingleShot(myLocation.directionTo(closestEnemy.location));
						alreadyShot = true;
					}
				}
				
				if(!alreadyShot) {
					
					boolean goodToShoot = Util.goodToShoot(rc, myLocation, closestEnemy);
					
					if(goodToShoot) {
						if(rc.canFireSingleShot()) {
							rc.fireSingleShot(myLocation.directionTo(closestEnemy.location));
						}
					}
				}
			}
			
			break;
		case DEFENSE:
			
			break;
		}
	}
	
}
