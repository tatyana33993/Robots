package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

public class GameVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    public volatile double m_robotPositionX = 320;
    public volatile double m_robotPositionY = 220;
    public volatile double m_robotDirection = 1;

    private volatile int m_targetPositionX = 321;
    private volatile int m_targetPositionY = 320;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.001;

    private double dist = distance(m_targetPositionX, m_targetPositionY,
            m_robotPositionX, m_robotPositionY);

    public Point[] walls = new Point[]{new Point(300, 200), new Point(310, 300),
            new Point(300, 300), new Point(400, 310), new Point(400, 200), new Point(410, 310),
            new Point(200, 200), new Point(310, 210), new Point(800, 200), new Point(810, 300),
            new Point(800, 300), new Point(900, 310), new Point(900, 300), new Point(910, 400),
            new Point(900, 400), new Point(1000, 410), new Point(600, 800), new Point(700, 810),
            new Point(300, 700), new Point(310, 800)};
    public Point[] mines = new Point[]{new Point(100, 100), new Point(190, 190), new Point(145, 145)};

    public GameVisualizer()
    {
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onRedrawEvent();
            }
        }, 0, 50);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onModelUpdateEvent();
            }
        }, 0, 10);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setTargetPosition(e.getPoint());
                repaint();
            }
        });
        setDoubleBuffered(true);
    }

    protected void setTargetPosition(Point p)
    {
        for(int i = 0; i < walls.length; i+=2)
        {
            if (p.x >= walls[i].x & p.x <= walls[i+1].x & p.y >= walls[i].y & p.y <= walls[i+1].y)
            {
                return;
            }
        }
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
        dist = distance(m_targetPositionX, m_targetPositionY,
                m_robotPositionX, m_robotPositionY);
    }

    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    protected void onModelUpdateEvent()
    {
        double distance = distance(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        if (distance < 0.5)
            return;
        double velocity = maxVelocity;
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection)
        {
            angularVelocity = maxAngularVelocity;
        }
        if (angleToTarget < m_robotDirection)
        {
            angularVelocity = -maxAngularVelocity;
        }

        moveRobot(velocity, angularVelocity, 10);
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public void moveRobot(double velocity, double angularVelocity, double duration)
    {
        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);
        velocity = applyLimits(velocity, 0, maxVelocity);
        double newX = m_robotPositionX + velocity / angularVelocity *
                (Math.sin(m_robotDirection  + angularVelocity * duration) -
                        Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity *
                (Math.cos(m_robotDirection  + angularVelocity * duration) -
                        Math.cos(m_robotDirection));
        if (!Double.isFinite(newY)) {
            newY = m_robotPositionY + velocity *duration * Math.sin(m_robotDirection);
        }
        if (getWalls(newX, newY))
        {
            if (newX > m_robotPositionX)
                m_robotPositionX = m_robotPositionX - 1;
            else
                m_robotPositionX = m_robotPositionX + 1;
            if (newY > m_targetPositionY)
                m_robotPositionY = m_robotPositionY - 1;
            else
                m_robotPositionY = m_robotPositionY - 1;
            m_robotDirection = m_robotDirection + 0.09;
        }
        else
        {
            if (getMines(newX, newY))
            {
                m_robotPositionX = 10;
                m_robotPositionY = 10;
            }
            else
            {
                m_robotPositionX = newX;
                m_robotPositionY = newY;
                double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);
                m_robotDirection = newDirection;
            }
        }
    }

    public boolean getWalls(double x, double y) {
        for (int i = 0; i < walls.length; i += 2) {
            if (x > walls[i].x & x < walls[i + 1].x & y > walls[i].y & y < walls[i + 1].y)
                return true;
        }
        return false;
    }

    public boolean getMines(double x, double y) {
        for (int i = 0; i < mines.length; i++)
        {
            if (distance(x, y, mines[i].x, mines[i].y) <= 4.5)
                return true;
        }
        return false;
    }
    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        for(int i = 0; i < walls.length; i+=2)
        {
            int width = walls[i + 1].x - walls[i].x;
            int height = walls[i+1].y - walls[i].y;
            drawWall(g2d, walls[i].x, walls[i].y, width, height);
        }
        for(int i = 0; i < mines.length; i++)
        {
            drawMine(g2d, mines[i].x, mines[i].y);
        }
        drawRobot(g2d, round(m_robotPositionX), round(m_robotPositionY), m_robotDirection);
        drawTarget(g2d, m_targetPositionX, m_targetPositionY);
    }

    private static void fillRect(Graphics g, int x, int y, int width, int height)
    {
        g.fillRect(x, y, width, height);
    }

    private static void drawRect(Graphics g, int x, int y, int width, int height)
    {
        g.drawRect(x, y, width, height);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        int robotCenterX = round(m_robotPositionX);
        int robotCenterY = round(m_robotPositionY);
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX - 10, robotCenterY, 20, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX - 10, robotCenterY, 20, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX - 5, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX - 5, robotCenterY, 5, 5);
    }

    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 9, 9);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 9, 9);
    }

    private void drawWall(Graphics2D g, int x, int y, int width, int height)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.BLACK);
        fillRect(g, x, y, width, height);
        g.setColor(Color.BLACK);
        drawRect(g, x, y, width, height);
    }

    private void drawMine (Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.RED);
        fillOval(g, x, y, 9,9);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 9, 9);
    }
}