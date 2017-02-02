package turtlebotpathing;
import battlecode.common.*;

public class Util {

	public static RobotInfo getClosestBot(RobotController rc, RobotInfo[] bots) {
		RobotInfo closestBot = null;
		RobotInfo bot;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = bots.length;i-->0;) {
			bot = bots[i];
			float dist = myLocation.distanceTo(bot.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestBot = bot;
			}
		}
		return closestBot;
	}
	
	public static TreeInfo getClosestTree(RobotController rc, TreeInfo[] trees) {
		TreeInfo closestTree = null;
		TreeInfo tree;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = trees.length;i-->0;) {
			tree = trees[i];
			float dist = myLocation.distanceTo(tree.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestTree = tree;
			}
		}
		return closestTree;
	}

}
