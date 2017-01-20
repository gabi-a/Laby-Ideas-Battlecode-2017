package smoothmoves;
import battlecode.common.*;

public class Nav {
	
	
	public static Direction awayFromBullets(MapLocation myLocation, BulletInfo[] bullets) {
		
		// Time step all the bullets forward by 1 turn
		BulletInfo[] futureBullets = new BulletInfo[bullets.length];
		for(int i = bullets.length;i-->0;) {
			BulletInfo bullet = bullets[i];
			futureBullets[i] = new BulletInfo(bullet.ID, bullet.location.add(bullet.dir,bullet.getSpeed()), bullet.dir,bullet.getSpeed(),bullet.getDamage());
		}
		
		// Apply Anti Gravity from each bullet
		MapLocation moveLocation = myLocation;
		for(int i = futureBullets.length;i-->0;) {
			moveLocation = moveLocation.add(futureBullets[i].location.directionTo(myLocation), 1/(futureBullets[i].location.distanceTo(myLocation) + 1));
		}
		Direction moveDirection = myLocation.directionTo(moveLocation);
		
		return moveDirection;
	}
	
	

}
