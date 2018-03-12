package rp.warehouse.pc.localisation.implementation;

import lejos.geom.Point;
import org.apache.log4j.Logger;
import rp.warehouse.pc.communication.Communication;
import rp.warehouse.pc.communication.Protocol;
import rp.warehouse.pc.data.RobotLocation;
import rp.warehouse.pc.localisation.NoIdeaException;
import rp.warehouse.pc.localisation.Ranges;
import rp.warehouse.pc.localisation.WarehouseMap;
import rp.warehouse.pc.localisation.interfaces.Localisation;

import java.util.List;
import java.util.Random;

/**
 * An implementation of the localisation interface. Used to actually calculate
 * the location.
 *
 * @author Kieran
 */
public class Localiser implements Localisation {

	// Currently assumes that all robots are facing upwards relative to the map.

	private static final Logger logger = Logger.getLogger(Localiser.class);
	private final WarehouseMap warehouseMap = new WarehouseMap();
	private final Point[] directionPoint = new Point[4];
	private final byte[] reverseRotation = new byte[] { 0, 3, 2, 1 };
	private final List<Point> blockedPoints = WarehouseMap.getBlockedPoints();
	private final byte MAX_RUNS = 10;
	private byte runCounter = 0;
	private final Random random = new Random();
	private Byte previousDirection = null;
	private final Communication comms;

	/**
	 * An implementation of the Localisation interface.
	 */
	public Localiser(Communication comms) {
		directionPoint[Ranges.UP] = new Point(0, 1);
		directionPoint[Ranges.RIGHT] = new Point(1, 0);
		directionPoint[Ranges.DOWN] = new Point(0, -1);
		directionPoint[Ranges.LEFT] = new Point(-1, 0);
		this.comms = comms;
	}

	@Override
	public RobotLocation getPosition() throws NoIdeaException {
		// Assuming they all face up initially
		// Get the readings from the sensors (using dummy values now)
		Ranges ranges = comms.getRanges();

		List<Point> possiblePoints = warehouseMap.getPoints(ranges);
		logger.debug("Possible points: " + possiblePoints);

		// Run whilst there are multiple points, or the maximum iterations has occurred.
		while (possiblePoints.size() > 1 && runCounter++ < MAX_RUNS) {
			List<Byte> directions = ranges.getAvailableDirections();
			if (runCounter > 1) {
				directions.remove(directions.indexOf(Ranges.getOpposite(previousDirection)));
			}
			logger.debug("Available directions: " + directions);
			// Choose a random direction from the list of available directions.
			final byte direction = directions.get(random.nextInt(directions.size()));
			previousDirection = direction;
			final Point move = directionPoint[direction];
			logger.debug("Chosen move: " + move);
			if (direction == Ranges.UP) {
				comms.sendMovement(Protocol.NORTH);
			} else if (direction == Ranges.RIGHT) {
				comms.sendMovement(Protocol.EAST);
			} else if (direction == Ranges.DOWN) {
				comms.sendMovement(Protocol.SOUTH);
			} else {
				comms.sendMovement(Protocol.WEST);
			}
			ranges = Ranges.rotate(comms.getRanges(), reverseRotation[direction]);
			logger.debug("Retrieved ranges: " + ranges);
			possiblePoints = filterPositions(possiblePoints, warehouseMap.getPoints(ranges), move);
			logger.debug("Filtered positions: " + possiblePoints);
		}
		// Create the location of the robot using the first possible location from the
		// list of possible locations.
		return new RobotLocation(possiblePoints.get(0), Protocol.NORTH);
	}

	/**
	 * Method to filter initial positions given new positions and a movement. Used
	 * to narrow down the possibility of location.
	 *
	 * @param initial
	 *            The initial possible positions recorded.
	 * @param next
	 *            The new possible positions recorded.
	 * @param change
	 *            The change in position from <b>initial</b> to <b>next</b>.
	 * @return The new list of possible positions of the robot.
	 */
	private List<Point> filterPositions(final List<Point> initial, final List<Point> next, final Point change) {
		// Filter the next list by removing all points that couldn't exist given the
		// previous points and the change in position.
		next.removeIf(p -> !initial.contains(p.subtract(change)) || blockedPoints.contains(p));
		return next;
	}

}