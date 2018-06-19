package pcgapprentice.dungeonlevel.visualization;

import burlap.mdp.core.state.State;
import burlap.visualizer.StatePainter;
import pcgapprentice.dungeonlevel.DungeonState;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * A painter for visualizing the location of the digger agent for
 * the full DungeonState
 */
public class AgentPainter implements StatePainter {

    @Override
    public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
        g2.setColor(Color.GRAY);

        DungeonState ds = (DungeonState)s;

        float fWidth = ds.getLevelWidth();
        float fHeight = ds.getLevelHeight();

        float width = cWidth / fWidth;
        float height = cHeight / fHeight;

        int ax = ds.x;
        int ay = ds.y;

        float rx = ax*width;
        float ry = cHeight - height - ay*height;

        g2.fill(new Ellipse2D.Float(rx, ry, width, height));
    }
}
