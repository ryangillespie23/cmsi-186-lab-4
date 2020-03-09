import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RobotSoccerSimulation extends JPanel {
    private static final long serialVersionUID = -5228718339006830546L;

    private static double WIDTH = 400;
    private static double HEIGHT = 600;

    private static final double PLAYER_RADIUS = 15;
    private static final double ENEMY_RADIUS = 20;
    private static final double PLAYER_SPEED = 2.3;
    private static final double ENEMY_SPEED = 1.8;
    private static final double FRICTION = 0.0009;

    private volatile String endMessage;

    static class Ball {
        private double x;
        private double y;
        private double radius;
        private double speed;
        private Color color;

        Ball(double x, double y, double radius, double speed, Color color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.color = color;
        }
        private static double constrain(double value, double low, double high) {
            return (Math.min(Math.max(value, low), high));
        }

        void moveToward(double targetX, double targetY) {
            var dx = targetX - this.x;
            var dy = targetY - this.y;
            var v = this.speed / Math.hypot(dx, dy);
            this.x = constrain(this.x + v * dx, this.radius, WIDTH - this.radius);
            this.y = constrain(this.y + v * dy, this.radius, HEIGHT - this.radius);
        }

        void applyFriction() {
            // Fill this in
            this.speed = this.speed - FRICTION;
            if (this.speed < 0) {
                this.speed = 0;
            }
        }

        boolean inside(Goal goal) {
            return (this.x - this.radius > goal.x - goal.w / 2 &&
                    this.x + this.radius < goal.x + goal.w / 2 &&
                    this.y - this.radius > goal.y - goal.h / 2 &&
                    this.y + this.radius < goal.y + goal.h / 2);
        }
    }

    private static Ball[] balls = new Ball[] { 
        new Ball(0.0, HEIGHT, PLAYER_RADIUS, PLAYER_SPEED, Color.BLUE),
        new Ball(WIDTH * 0.25, 40, ENEMY_RADIUS, ENEMY_SPEED, Color.RED),
        new Ball(WIDTH * 0.75, 40, ENEMY_RADIUS, ENEMY_SPEED, Color.RED),
        new Ball(WIDTH / 2, HEIGHT / 2, ENEMY_RADIUS, ENEMY_SPEED, Color.RED) 
    };

    private static class Goal {
        double x = WIDTH / 2;
        double y = 0;
        double w = 100;
        double h = 100;
    }

    private static Goal goal = new Goal();

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (var ball : balls) {
            g.setColor(ball.color);
            g.fillOval((int) (ball.x - ball.radius), (int) (ball.y - ball.radius), (int) ball.radius * 2,
                    (int) ball.radius * 2);
        }
        g.setColor(new Color(255, 255, 255, 128));
        g.fillRect((int) (goal.x - goal.w / 2), (int) (goal.y - goal.h / 2), (int) goal.w, (int) goal.h);
        if (endMessage != null) {
            g.setFont(new Font("Arial", Font.PLAIN, 50));
            g.setColor(Color.RED);
            g.drawString(endMessage, 30, (int) HEIGHT / 2);
        }
    }
    private static void adjustIfCollisions(Ball[] balls){
        for (var b1 : balls) {
            for (var b2 : balls) {
                if (b1 != b2) {
                    var dx = b2.x - b1.x;
                    var dy = b2.y - b1.y;
                    final var distance = Math.hypot(dx, dy);
                    var overlap = b1.radius + b2.radius - distance;
                    if (overlap > 0){
                        final var adjustX = (overlap / 2) * (dx / distance);
                        final var adjustY = (overlap / 2) * (dx / distance);
                        b1.x -= adjustX;
                        b1.y -= adjustY;
                        b2.x += adjustX;
                        b2.y += adjustY;
                    }
                }
            }
        }
    }
    private void endSimulationIfNecessary(){
        if (balls[0].speed <= 0)
            endMessage = "Oh no... try again";
        if (balls[0].inside(goal))
            endMessage = "GOAL";
    }

    private void runTheAnimation() {
        while (endMessage == null) {
            for (var i = 0; i < balls.length; i++){
                balls[i].applyFriction();
                balls[i].moveToward(i == 0 ? goal.x : balls[0].x, i == 0 ? goal.y : balls[0].y);
            }
            adjustIfCollisions(balls);
            endSimulationIfNecessary();
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var panel = new RobotSoccerSimulation();
            panel.setBackground(Color.GREEN.brighter());
            var frame = new JFrame("Robotic Soccer");
            frame.setSize((int) WIDTH,(int) HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
            new Thread(() -> panel.runTheAnimation()).start();
        });
    }
}
