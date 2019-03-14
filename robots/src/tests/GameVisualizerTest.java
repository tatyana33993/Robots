import gui.GameVisualizer;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameVisualizerTest {
    @Test
    void testGetWalls() {
        GameVisualizer g = new GameVisualizer();
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < g.walls.length; i += 2) {
            Random rnd = new Random();
            int x = g.walls[i].x + rnd.nextInt((int)g.walls[i+1].x - (int)g.walls[i].x);
            int y = g.walls[i].y + rnd.nextInt((int)g.walls[i+1].y - (int)g.walls[i].y);
            points.add(new Point(x, y));
        }
        for (Point p: points){
            boolean result = g.getWalls(p.x, p.y);
            assertTrue(result);
        }
    }

    @Test
    void testGetMines() {
        GameVisualizer g = new GameVisualizer();
        ArrayList<Point> points = new ArrayList<Point>();
        for (int i = 0; i < g.mines.length; i += 1){
            boolean result = g.getMines(g.mines[i].x, g.mines[i].y);
            assertTrue(result);
            Random rnd = new Random();
            int x = rnd.nextInt(900);
            int y = rnd.nextInt(900);
            if (x != g.mines[i].x & y != g.mines[i].y){
                points.add(new Point(x, y));
            }
        }
        for (Point p: points){
            boolean result = g.getMines(p.x, p.y);
            assertFalse(result);
        }
    }
}