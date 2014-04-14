package crackSim.viz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import crackSim.core.BackingGrid;
import crackSim.core.BackingGrid.Cell;
import crackSim.core.BackingGrid.GridPoint;
import crackSim.core.Grid;
import crackSim.scheduling.IODevice;

/**
 * This creates the GUI which visually represents the simulation. This is shamelessly copies from the previous project in the hopes
 * that some of the code is portable.
 * 
 * @author Daniel Keyes
 */
public class Visualizer extends JPanel implements IODevice {

	// private JPanel topView;
	// private JLabel bottomView;
	// private BufferedImage topViewSurface;
	// private BufferedImage bottomViewSurface;

	private BackingGrid backingGrid;
	private Comparator<Cell> cellSorter;
	private Camera camera;

	// simple camera class for handing viewing information
	// the identity is at 0,0,0 and points down the z (maybe?) axis
	public static class Camera {
		public static Camera TOP = new Camera(0, 0, 0, 0, 0, 0);
		public static Camera BOTTOM = new Camera(0, 0, 0, 0, 180, 0);
		// camera parameters:
		private double vx, vy, vz; // camera position
		private double thx, thy, thz; // camera rotation--ri is rotation around axis i
		// notably this *technically* is only 4 degrees of freedom
		// latitude and longitude of the camera direction, and x,y offset of the image
		// (but don't tell anyone!)
		public Camera(double cx, double cy, double cz, double thx, double thy, double thz) {
			this.vx = cx;
			this.vy = cy;
			this.vz = cz;
			this.thx = thx;
			this.thy = thy;
			this.thz = thz;
		}

		private double getProjectedX(GridPoint p) {
			double x = p.x - vx;
			double y = p.y - vy;
			double z = p.z - vz;
			double cx = Math.cos(Math.toRadians(thx));
			double sx = Math.sin(Math.toRadians(thx));
			double cy = Math.cos(Math.toRadians(thy));
			double sy = Math.sin(Math.toRadians(thy));
			double cz = Math.cos(Math.toRadians(thz));
			double sz = Math.sin(Math.toRadians(thz));
			return cy*(sz*y + cz*x) - sy*z;
		}

		private double getProjectedY(GridPoint p) {
			double x = p.x - vx;
			double y = p.y - vy;
			double z = p.z - vz;
			double cx = Math.cos(Math.toRadians(thx));
			double sx = Math.sin(Math.toRadians(thx));
			double cy = Math.cos(Math.toRadians(thy));
			double sy = Math.sin(Math.toRadians(thy));
			double cz = Math.cos(Math.toRadians(thz));
			double sz = Math.sin(Math.toRadians(thz));
			return sx*(cy*z + sy*(sz*y+cz*x	)) + cz*(cz*y + sz*x);
		}

		private double getProjectedZ(GridPoint p) {
			double x = p.x - vx;
			double y = p.y - vy;
			double z = p.z - vz;
			double cx = Math.cos(Math.toRadians(thx));
			double sx = Math.sin(Math.toRadians(thx));
			double cy = Math.cos(Math.toRadians(thy));
			double sy = Math.sin(Math.toRadians(thy));
			double cz = Math.cos(Math.toRadians(thz));
			double sz = Math.sin(Math.toRadians(thz));
			return cx*(cy*z + sy*(sz*y+cz*x	)) - sx*(cz*y + sz*x);
		}
	}

	/**
	 * Constructor that initializes all the elements defined above.
	 */
	public Visualizer(BackingGrid backingGrid, final Camera viewingDirection) {
		this.camera = viewingDirection;
		
		cellSorter = new Comparator<Cell>() {
			@Override
			public int compare(Cell c1, Cell c2) {
				// find the vertex with the largest Z-value, and sort by that
				double c1max = viewingDirection.getProjectedZ(c1.getVertices().get(0));
				double c2max = viewingDirection.getProjectedZ(c2.getVertices().get(0));
				for (GridPoint g : c1.getVertices()) {
					c1max = Math.max(c1max, viewingDirection.getProjectedZ(g));
				}
				for (GridPoint g : c2.getVertices()) {
					c2max = Math.max(c2max, viewingDirection.getProjectedZ(g));
				}
				return (int) Math.signum(c1max - c2max);
			}
		};

		// display top and bottom views
		// topViewSurface = new BufferedImage(400, 533, BufferedImage.TYPE_INT_RGB);
		// topView = new JPanel();

		// bottomViewSurface = new BufferedImage(400, 533, BufferedImage.TYPE_INT_RGB);
		// bottomView = new JLabel(new ImageIcon(bottomViewSurface));

		this.backingGrid = backingGrid;
	}

	// public void show() {
	// // do some JFrame boilerplate
	// JFrame frame = new JFrame("Crack propagation simulator"); // title
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// // I'm backing this with a JPanel containing a Bufferent Image. We could probably also just use a
	// // JApplet, or a Canvas, or something. Java GUIs aren't my forte.
	//
	// this.setPreferredSize(new Dimension(400,600));
	// frame.add(this, BorderLayout.WEST);
	// frame.setSize(800, 600);
	// frame.setLocationByPlatform(true);
	// frame.setVisible(true);
	// }

	@Override
	public void update(Grid currentGrid) {

		int screenWidth = getWidth();
		int screenHeight = getHeight();
		// find bounds to show for x and y
		int gridMinX = 0, gridMaxX = 0, gridMinY = 0, gridMaxY = 0;
		int buffer = 15; // magic value to add some buffer to the edge of the vis
		boolean first = true;
		for (GridPoint gp : currentGrid.getGridPoints()) {
			if (first) {
				gridMinX = (int) Math.floor(camera.getProjectedX(gp));
				gridMaxX = (int) Math.ceil(camera.getProjectedX(gp));
				gridMinY = (int) Math.floor(camera.getProjectedY(gp));
				gridMaxY = (int) Math.ceil(camera.getProjectedY(gp));
				first = false;
				continue;
			}
			gridMinX = Math.min(gridMinX, (int) Math.floor(camera.getProjectedX(gp)));
			gridMaxX = Math.max(gridMaxX, (int) Math.ceil(camera.getProjectedX(gp)));
			gridMinY = Math.min(gridMinY, (int) Math.floor(camera.getProjectedY(gp)));
			gridMaxY = Math.max(gridMaxY, (int) Math.ceil(camera.getProjectedY(gp)));
		}
		gridMinX -= buffer;
		gridMaxX += buffer;
		gridMinY -= buffer;
		gridMaxY += buffer;
		int gridWidth = gridMaxX - gridMinX;
		int gridHeight = gridMaxY - gridMinY;

		// draw the grid using a simple orthogonal projection
		// x -> x
		// y -> y
		// z -> 0
		// This could have weird occlusion effects depending on the grid. Since the cells are in 3-space, they could possible
		// wrap around and cover each other up in awkward ways. The smart way to solve this is to use something like the Painter's
		// Algorithm. This is just a rough visualization though, so it might be simplest to just sort the polygons by the z-position of
		// their closest vertex, or something.
		List<Cell> sortedCells = new ArrayList<Cell>(backingGrid.getCells());
		Collections.sort(sortedCells, cellSorter);
		// save some state variables for the next repaint
		this.sortedCells = sortedCells;
		this.currentGrid = currentGrid;
		this.gridMinX = gridMinX;
		this.gridMinY = gridMinY;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		repaint();
	}

	private List<Cell> sortedCells;
	private Grid currentGrid;
	private double gridMinX;
	private double gridMinY;
	private double screenWidth;
	private double screenHeight;
	private double gridWidth;
	private double gridHeight;

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintView(g);
	}

	private void paintView(Graphics g) {

		if (sortedCells == null)
			return;

		// Graphics g = view.getGraphics(); // Create a new instance of Graphics (GUI platform)

		if (g == null)
			return;

		// clear
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) screenWidth, (int) screenHeight);

		for (Cell c : sortedCells) {
			List<GridPoint> vertices = c.getVertices();
			int n = vertices.size();
			int[] xPoints = new int[n];
			int[] yPoints = new int[n];
			for (int i = 0; i < n; i++) {
//				xPoints[i] = (int) ((vertices.get(i).x - gridMinX) * (screenWidth - 1) / gridWidth);
//				yPoints[i] = (int) ((vertices.get(i).y - gridMinY) * (screenHeight - 1) / gridHeight);
				xPoints[i] = (int) ((camera.getProjectedX(vertices.get(i))-gridMinX)*(screenWidth - 1) / gridWidth);
				yPoints[i] = (int) ((camera.getProjectedY(vertices.get(i))-gridMinY)*(screenWidth - 1) / gridHeight);
			}

			// fill the polygon with blue (alive) or red (dead)
			if (currentGrid.getDamaged().contains(c)) {
				g.setColor(Color.RED);
			} else if (!Collections.disjoint(currentGrid.getAdjacent(c), currentGrid.getDamaged())) {
				g.setColor(Color.YELLOW);
			} else if (currentGrid.getAlive().contains(c)) {
				g.setColor(Color.BLUE);
			}
			g.fillPolygon(xPoints, yPoints, n);

			// color the edge of the polygon
			g.setColor(Color.BLACK);
			g.drawPolygon(xPoints, yPoints, n);
		}

		// repaint the simulation with updated elements
		// super.repaint();
	}
}
