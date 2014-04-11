package crackSim.viz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
public class Visualizer implements IODevice {

	private JLabel view;
	private BufferedImage surface;

	private BackingGrid backingGrid;

	/**
	 * Constructor that initializes all the elements defined above.
	 */
	public Visualizer(BackingGrid backingGrid) {

		surface = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		view = new JLabel(new ImageIcon(surface));

		this.backingGrid = backingGrid;
	}

	public void show() {
		// do some JFrame boilerplate
		JFrame frame = new JFrame("Crack propagation simulator"); // title
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// I'm backing this with a JPanel containing a Bufferent Image. We could probably also just use a
		// JApplet, or a Canvas, or something. Java GUIs aren't my forte.

		frame.setSize(600, 600);
		frame.setContentPane(view);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	@Override
	public void update(Grid currentGrid) {
		Graphics g = surface.getGraphics(); // Create a new instance of Graphics (GUI platform)

		int screenWidth = surface.getWidth();
		int screenHeight = surface.getHeight();
		// if we're just doing an orthogonal projection, we only need to know the maximum and minimum x and y positions
		int gridMinX = 0, gridMaxX = 0, gridMinY = 0, gridMaxY = 0;
		int buffer = 15; // magic valid to add some buffer to the edge of the vis
		boolean first = true;
		for (GridPoint gp : currentGrid.getGridPoints()) {
			if (first) {
				gridMinX = (int) Math.floor(gp.x);
				gridMaxX = (int) Math.ceil(gp.x);
				gridMinY = (int) Math.floor(gp.y);
				gridMaxY = (int) Math.ceil(gp.y);
				first = false;
				continue;
			}
			gridMinX = Math.min(gridMinX, (int) Math.floor(gp.x));
			gridMaxX = Math.max(gridMaxX, (int) Math.ceil(gp.x));
			gridMinY = Math.min(gridMinY, (int) Math.floor(gp.y));
			gridMaxY = Math.max(gridMaxY, (int) Math.ceil(gp.y));
		}
		gridMinX -= buffer;
		gridMaxX += buffer;
		gridMinY -= buffer;
		gridMaxY += buffer;
		int gridWidth = gridMaxX - gridMinX;
		int gridHeight = gridMaxY - gridMinY;

		// clear
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, screenWidth, screenHeight);

		// draw the grid using a simple orthogonal projection
		// x -> x
		// y -> y
		// z -> 0
		// This could have weird occlusion effects depending on the grid. Since the cells are in 3-space, they could possible
		// wrap around and cover each other up in awkward ways. The smart way to solve this is to use something like the Painter's
		// Algorithm. This is just a rough visualization though, so it might be simplest to just sort the polygons by the z-position of
		// their closest vertex, or something.
		List<Cell> sortedCells = new ArrayList<Cell>(backingGrid.getCells());
		Collections.sort(sortedCells, new Comparator<Cell>() {
			@Override
			public int compare(Cell c1, Cell c2) {
				// find the vertex with the largest Z-value, and sort by that
				double c1z = c1.getVertices().get(0).z;
				double c2z = c2.getVertices().get(0).z;
				for (GridPoint g : c1.getVertices()) {
					c1z = Math.max(c1z, g.z);
				}
				for (GridPoint g : c2.getVertices()) {
					c2z = Math.max(c2z, g.z);
				}
				return (int) Math.signum(c1z - c2z);
			}
		});
		for (Cell c : sortedCells) {
			List<GridPoint> vertices = c.getVertices();
			int n = vertices.size();
			int[] xPoints = new int[n];
			int[] yPoints = new int[n];
			for (int i = 0; i < n; i++) {
				xPoints[i] = (int) ((vertices.get(i).x - gridMinX) * (screenWidth - 1) / gridWidth);
				yPoints[i] = (int) ((vertices.get(i).y - gridMinY) * (screenHeight - 1) / gridHeight);
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

		view.repaint(); // repaint the simulation with updated elements
	}
}
