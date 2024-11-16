package me.eldodebug.soar.management.mods.impl.waveycapes.sim;

import java.util.ArrayList;
import java.util.List;

import me.eldodebug.soar.management.mods.impl.WaveyCapesMod;
import me.eldodebug.soar.utils.MathUtils;
import me.eldodebug.soar.utils.vector.Vector2;

public class StickSimulation {

    public List<Point> points = new ArrayList<>();
    public List<Stick> sticks = new ArrayList<>();
    public float gravity = 20f;
    public int numIterations = 30;
    private float maxBend = 5;

    public void simulate() {
    	
        gravity = WaveyCapesMod.getInstance().getGravitySetting().getValueFloat();

        float deltaTime = 50f / 1000f;
        
        Vector2 down = new Vector2(0, gravity * deltaTime);
        Vector2 tmp = new Vector2(0, 0);
        for (Point p : points) {
            if (!p.locked) {
                tmp.copy(p.position);
                p.position.subtract(down);
                p.prevPosition.copy(tmp);
            }
        }

        Point basePoint = points.get(0);
        for (Point p : points) {
            if (p != basePoint && p.position.x - basePoint.position.x > 0) {
                p.position.x = basePoint.position.x - 0.1f;
            }
        }

        for (int i = points.size() - 2; i >= 1; i--) {
            double angle = getAngle(points.get(i).position, points.get(i - 1).position, points.get(i + 1).position);
            angle *= 57.2958;
            if (angle > 360) {
                angle -= 360;
            }
            if (angle < -360) {
                angle += 360;
            }
            double abs = Math.abs(angle);
            if (abs < 180 - maxBend) {
                Vector2 replacement = getReplacement(points.get(i).position, points.get(i - 1).position, angle,
                        180 - maxBend + 1);
                points.get(i + 1).position = replacement;
            }
            if (abs > 180 + maxBend) {
                Vector2 replacement = getReplacement(points.get(i).position, points.get(i - 1).position, angle,
                        180 + maxBend - 1);
                points.get(i + 1).position = replacement;
            }
        }

        for (int i = 0; i < numIterations; i++) {
            for (int x = sticks.size() - 1; x >= 0; x--) {
                Stick stick = sticks.get(x);
                Vector2 stickCentre = stick.pointA.position.clone().add(stick.pointB.position).div(2);
                Vector2 stickDir = stick.pointA.position.clone().subtract(stick.pointB.position).normalize();
                if (!stick.pointA.locked) {
                    stick.pointA.position = stickCentre.clone().add(stickDir.clone().mul(stick.length / 2));
                }
                if (!stick.pointB.locked) {
                    stick.pointB.position = stickCentre.clone().subtract(stickDir.clone().mul(stick.length / 2));
                }
            }
        }
        
        for (int x = 0; x < sticks.size(); x++) {
            Stick stick = sticks.get(x);
            Vector2 stickDir = stick.pointA.position.clone().subtract(stick.pointB.position).normalize();
            if (!stick.pointB.locked) {
                stick.pointB.position = stick.pointA.position.clone().subtract(stickDir.mul(stick.length));
            }
        }
    }

    private Vector2 getReplacement(Vector2 middle, Vector2 prev, double angle, double target) {
        double theta = target / 57.2958;
        float x = prev.x - middle.x;
        float y = prev.y - middle.y;
        if (angle < 0) {
            theta *= -1;
        }
        double cs = Math.cos(theta);
        double sn = Math.sin(theta);
        return new Vector2((float) ((x * cs) - (y * sn) + middle.x), (float) ((x * sn) + (y * cs) + middle.y));
    }

    private double getAngle(Vector2 middle, Vector2 prev, Vector2 next) {
        return Math.atan2(next.y - middle.y, next.x - middle.x) - Math.atan2(prev.y - middle.y, prev.x - middle.x);
    }

    public static class Point {
    	
        public Vector2 position = new Vector2(0, 0);
        public Vector2 prevPosition = new Vector2(0, 0);
        public boolean locked;
        
        public float getLerpX(float delta) {
            return MathUtils.lerp(delta, prevPosition.x, position.x);
        }
        
        public float getLerpY(float delta) {
            return MathUtils.lerp(delta, prevPosition.y, position.y);
        }
    }

    public static class Stick {
        public Point pointA, pointB;
        public float length;

        public Stick(Point pointA, Point pointB, float length) {
            this.pointA = pointA;
            this.pointB = pointB;
            this.length = length;
        }

    }
}