package pcgapprentice.dungeonlevel.visualization;

import burlap.mdp.core.state.State;
import burlap.visualizer.StatePainter;
import pcgapprentice.dungeonlevel.DungeonState;
import static pcgapprentice.dungeonlevel.DungeonDomainGenerator.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A painter for visualizing the level of a full DungeonState
 */
public class LevelPainter implements StatePainter {

    @Override
    public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
        g2.setColor(Color.BLACK);

        DungeonState ds = (DungeonState)s;
        int[][] level = ds.level;

        float fWidth = ds.getLevelWidth();
        float fHeight = ds.getLevelHeight();

        // Determine the width of a cell on the canvas to fit the level
        float width = cWidth / fWidth;
        float height = cHeight / fHeight;

        for(int x = 0; x < level.length; x++) {
            for(int y = 0; y < level[0].length; y++) {
                float rx = x * width;
                float ry = cHeight - height - y * height;

                switch(level[x][y]) {
                    case CELL_BLOCK:
                        g2.setColor(Color.BLACK);
                        break;
                    case CELL_OPEN:
                        g2.setColor(Color.WHITE);
                        break;
                    case CELL_TREASURE:
                        g2.setColor(Color.YELLOW);
                        break;
                    case CELL_ENEMY:
                        g2.setColor(Color.RED);
                        break;
                    case CELL_EXIT:
                        g2.setColor(Color.GREEN);
                        break;
                    case CELL_KEY:
                        g2.setColor(Color.BLUE);
                        break;
                    case CELL_DOOR:
                        g2.setColor(Color.MAGENTA);
                        break;
                    default:
                        g2.setColor(Color.BLACK);
                        break;
                }

                g2.fill(new Rectangle2D.Float(rx, ry, width, height));
            }
        }
    }
}
