package org.sample.asteroids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AsteroidsApplication extends Application {

    public static int WIDTH = 600;
    public static int HEIGHT = 400;

    public static void main(String[] args) {
        launch(AsteroidsApplication.class);
    }

    public static int partsCompleted() {
        return 4;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH, HEIGHT);
        Text text = new Text(10, 20, "Points: 0");
        pane.getChildren().add(text);

        AtomicInteger points = new AtomicInteger();

        // Creating ship
        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        ArrayList<Asteroid> asteroids = new ArrayList<>();
        ArrayList<Projectile> projectiles = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            asteroids.add(asteroid);
        }

        pane.getChildren().add(ship.getCharacter());
        asteroids.forEach(asteroid -> {
            pane.getChildren().add(asteroid.getCharacter());

        });

        Scene scene = new Scene(pane);

        HashMap<KeyCode, Boolean> pressedKeys = new HashMap<>();

        scene.setOnKeyPressed((e) -> {
            pressedKeys.put(e.getCode(), true);
        });

        scene.setOnKeyReleased((e) -> {
            pressedKeys.put(e.getCode(), false);
        });

        new AnimationTimer() {
            long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Rotate
                if (pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
                    ship.turnLeft();
                }

                if (pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
                    ship.turnRight();
                }

                // Moving
                if (pressedKeys.getOrDefault(KeyCode.UP, false)) {
                    ship.accelerate();
                }

                // Shooter
                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 3) {
                    if (now - lastUpdate > 500_000_000L) {
                        lastUpdate = now;

                        Projectile projectile = new Projectile((int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY());
                        projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                        projectiles.add(projectile);

                        projectile.accelerate();
                        projectile.setMovement(projectile.getMovement().normalize().multiply(3));

                        pane.getChildren().add(projectile.getCharacter());
                    }
                }

                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                        stop();
                    }
                });

                // Watch collision between asteroid and projectile
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if (asteroid.collide(projectile))  {
                            asteroid.setAlive(false);
                            projectile.setAlive(false);
                        }

                        if (!asteroid.isAlive()) {
                            text.setText("Points: " + points.addAndGet(1000));
                        }
                    });
                });

                // Remove asteroids and projectile if not alive
                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList()));

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList()));

                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                // Add asteroids with a probability of 0.5% each time the AnimationTimer is called
                if (Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);

                    if (!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }
            }

        }.start();

        stage.setTitle("Asteroids");
        stage.setScene(scene);
        stage.show();
    }
}

