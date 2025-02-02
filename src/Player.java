import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

public class Player extends TemplateObject
{
    double x;
    double y;
    double radius;
    double vx;
    double vy;
    public Color cl;

    ArrowButton left, right;

    public Player(double x, double y, double radius)
    {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.vx = 2;
        this.vy = 5;
        cl = Color.WHITE;
    }

    @Override
    public void draw()
    {
        StdDraw.setPenColor(cl);

        /*
            Draw a deformed circle based on current velocity
         */
        double vxNorm = Math.abs(vx / 5.5);
        double vyNorm = Math.abs( -vy / Game.getInstance().getMinVY() * 2.25);

        if (vxNorm < 1) vxNorm = 1;
        if (vyNorm < 1) vyNorm = 1;

        StdDraw.filledEllipse(x, y, radius * vxNorm, radius * vyNorm);
    }

    @Override
    public void update()
    {
        x = x + vx;
        y = y + vy;

        vy = Math.max(vy - Game.getInstance().getGravity(), Game.getInstance().getMinVY());

        /*
            Changing direction has more effect on velocity up to a certain limit.
         */
        double strength = Game.getInstance().getVX();
        if (right.isPressed())
        {
            if (vx > Game.getInstance().getMaxVX()) return; // For cases when speed is more than max after hitting a launch pad
            if (vx < 0) strength = strength * 2; // Pressing right but current direction is left
            vx = Math.min(Game.getInstance().getMaxVX(), vx + strength);
        }
        if (left.isPressed())
        {
            if (vx < -Game.getInstance().getMaxVX()) return; // For cases when speed is more than max after hitting a launch pad
            if (vx > 0) strength = strength * 2; // Pressing left but current direction is right
            vx = Math.max(-Game.getInstance().getMaxVX(), vx - strength);
        }
    }

    @Override
    public void reset()
    {
        //
    }

    public boolean collide(Collidable cld)
    {
        if (cld.getClass() == Obstacle.class) return collide((Obstacle) cld);
        if (cld.getClass() == LaunchPad.class) return collide((LaunchPad) cld);
        return false;
    }

    public boolean collide(Obstacle o)
    {
        if (isIn(o, radius))
        {
            bounce();
            o.collide(this);
            Game.getInstance().hitObstacle(this, o);
            return true;
        }
        return false;
    }

    public boolean collide(LaunchPad l)
    {
        if (between(x, y, l.x1, l.y1, l.x2, l.y2))
        {
            l.collide(this);
            return true;
        }
        return false;
    }

    public void bounce()
    {
        /*
            Hit obstacle, flip y velocity between a defined limit
         */
        vy = -vy;
        vy = Math.max(vy, Game.getInstance().getHitVY());
        vy = Math.min(vy, Game.getInstance().getMaxVY());
    }

    public void launch(double vx_, double vy_)
    {
        vy = vy_;
        vx = vx_;

        for (int i = 0; i < 2; i++) update();
    }

    private boolean isIn(Obstacle o, double pEPS)
    {
        return x + this.radius + pEPS > o.x - o.halfWidth && x - radius - pEPS < o.x + o.halfWidth &&
                y + this.radius > o.y - o.halfHeight && y - radius < o.y + o.halfHeight;
    }

    private boolean between(double x, double y, double x1, double y1, double x2, double y2)
    {
        double dtToX1 = (x - x1) * (x - x1) + (y - y1) * (y - y1);
        dtToX1 = Math.sqrt(dtToX1);

        double dtToX2 = (x - x2) * (x - x2) + (y - y2) * (y - y2);
        dtToX2 = Math.sqrt(dtToX2);

        double dtOverall = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
        dtOverall = Math.sqrt(dtOverall);

        double dtEPS = 10;

        return dtToX1 + dtToX2 - dtOverall < dtEPS;
    }

    public boolean checkBelow()
    {
        /*
            If the player drops below screen - reset score and launch him back
         */

        int maxY = Game.getInstance().getMaxY();
        if (y < -maxY * 0.2)
        {
            vy = maxY / 40.0 + 2;
            return true;
        }
        return false;
    }

    public void checkAbove()
    {
        int maxY = Game.getInstance().getMaxY();

        /*
            If the player is above the screen
         */
        if (y > maxY * 1.1)
        {
            StdDraw.setPenColor(cl);
            double currentRad = y - maxY;
            double offset = Game.getInstance().getMaxX() * 0.01;

            /*
                Kinda close, draw a triangle
             */
            if (y < maxY * 1.2)
            {
                double[] xs = new double[]{x, x - offset * 2, x + offset * 2};
                double[] ys = new double[]{maxY * 1.04, maxY - offset, maxY - offset};

                StdDraw.filledPolygon(xs, ys);
            }

            /*
                Far above the screen, draw an arrow
             */
            else
            {
                double penR = Game.getInstance().getPenR();

                /*
                    Make the pen radius slimmer as the player is farthest from the border
                 */
                currentRad = 1 / currentRad;
                currentRad = Math.min(penR * 5, currentRad);
                currentRad = Math.max(penR, currentRad);
                StdDraw.setPenRadius(currentRad);

                StdDraw.line(x, maxY * 1.04, x + offset , maxY - offset / 2);
                StdDraw.line(x, maxY * 1.04, x - offset, maxY - offset / 2);

                StdDraw.setPenRadius(penR);
            }

        }
    }

    public void checkSides()
    {
        int maxX = Game.getInstance().getMaxX();
        double maxVX = Game.getInstance().getMaxVX();
        double maxVY = Game.getInstance().getMaxVY();
        /*
            If the player hits a wall, bounce him back at max X speed and give a little upwards boost
         */
        if (x + radius < -maxX * 0.05) {vx = maxVX; vy = Math.min(vy + 3, maxVY);}
        if (x - radius > maxX * 1.05) {vx = -maxVX; vy = Math.min(vy + 3, maxVY);}
    }
}
