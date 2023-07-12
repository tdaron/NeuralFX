package org.kaporos.neuralfx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.kaporos.neuralfx.network.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class Point {
    public int x;
    public int y;
    public Color color;

    public Point(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

}

public class HelloController {
    @FXML
    private Canvas canvas;

    @FXML
    private Button trainButton;

    @FXML
    private ColorPicker colorpicker;

    @FXML
    private Text iterationsCount;

    @FXML
    private Text currentLoss;

    private ArrayList<Point> points;

    private Network network;

    private Lock ui_lock = new ReentrantLock();



    long iterations = 0;
    double loss = 0;

    boolean isTraining = false;
    Thread trainingThread = null;
    Timer renderTimer = null;




    public HelloController() {
        points = new ArrayList<>();
        ArrayList<Layer> layers = new ArrayList<>();
        layers.add(new DenseLayer(2,15, 1));
        layers.add(new Sigmoid());
        layers.add(new DenseLayer(15,3,1));
        layers.add(new Sigmoid());
        layers.add(new DenseLayer(3,3,1));
        layers.add(new Sigmoid());
        layers.add(new DenseLayer(3,3,1));
        layers.add(new Sigmoid());
        network = new Network(layers, 0.2);
    }

    private Color pixelEval(double x, double y) {
        double[][] result = network.feed_forward(new double[][]{{x,y}});
        return Color.color(Math.max(0, result[0][0]), Math.max(0,result[0][1]), Math.max(0, result[0][2]));
    }

    private void drawClicks() {
        GraphicsContext cg = canvas.getGraphicsContext2D();
        points.forEach((point) -> {
            int radius = 15;
            int borderWidth = 1;
            int startX = point.x - radius/2;
            int startY = point.y - radius/2;
            cg.setFill(point.color);
            cg.fillOval(startX, startY, radius, radius);
            cg.setStroke(Color.WHITE);
            cg.strokeOval(startX, startY, radius+borderWidth, radius+borderWidth);
        });
    }

    private void drawColor() {
        var cg = canvas.getGraphicsContext2D();
        int width = (int) Math.round(canvas.getWidth());
        int height =(int) Math.round(canvas.getHeight());
        int squareSize = 5;

        PixelWriter writer = cg.getPixelWriter();

        for (int y = 0 ; y < height ; y+=squareSize) {
            for (int x = 0; x < width; x+=squareSize) {
                Color value = pixelEval(Double.valueOf(x)/width,Double.valueOf(y)/height);
                for (int t = 0; t < squareSize; t++) {
                    for (int u = 0; u < squareSize; u++) {
                        writer.setColor(x+t, y+u, value);

                    }
                }

            }
        }

    }


    public void redraw() {
        ui_lock.lock();
        drawColor();
        drawClicks();
        iterationsCount.setText("Current iterations: "+iterations);
        currentLoss.setText("Current Loss: "+loss);
        ui_lock.unlock();
    }

    @FXML
    protected void onCanvasClick(MouseEvent event) {
        int clicked_x = (int) Math.round(event.getX());
        int clicked_y = (int) Math.round(event.getY());
        Color color = colorpicker.getValue();
        ui_lock.lock();
        points.add(new Point(clicked_x, clicked_y, color));
        ui_lock.unlock();

        drawClicks();
    }

    public void clear(ActionEvent actionEvent) {
        int width = (int) Math.round(canvas.getWidth());
        int height =(int) Math.round(canvas.getHeight());
        GraphicsContext cg = canvas.getGraphicsContext2D();
        points = new ArrayList<>();
        cg.setFill(Color.WHITESMOKE);
        cg.fillRect(0, 0, width, height);
        redraw();
    }

    public void train() {
        iterations += 1;
        int width = (int) Math.round(canvas.getWidth());
        int height =(int) Math.round(canvas.getHeight());
        ui_lock.lock();
        points.forEach((point -> {
            var result = network.feed_forward(new double[][]{{Double.valueOf(point.x)/width, Double.valueOf(point.y)/height}});
            var error = new double[][]{{
                    2*(result[0][0] - point.color.getRed()),
                    2*(result[0][1] - point.color.getGreen()),
                    2*(result[0][2] - point.color.getBlue()),
            }};
            loss = (Math.pow(result[0][0] - point.color.getRed(), 2) + Math.pow(result[0][1] - point.color.getGreen(), 2) + Math.pow(result[0][2] - point.color.getBlue(), 2))/3;
            network.backpropagation(error);
        }));
        ui_lock.unlock();
    }

    public void train(boolean infinite) {
        if (isTraining) {
            System.out.println("Interrupted training");
            trainingThread.interrupt();
            renderTimer.cancel();
            isTraining = false;
            trainButton.setText("Train 50 iterations.");
            Platform.runLater(() -> {redraw();});
            return;
        }
        trainButton.setText("Stop training !");
        isTraining = true;
        trainingThread = new Thread(() -> {
            if (!infinite) {
                int iterations = 50;
                for (int x = 0; x < iterations; x++) {
                    train();
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
                isTraining = false;
                renderTimer.cancel();
                Platform.runLater(() -> {
                    redraw();
                    trainButton.setText("Train 50 iterations.");

                });
                System.out.println("training done");
            } else {
                while (true) {
                    train();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


        });
        trainingThread.start();

        TimerTask render_ui = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    redraw();
                });
            }
        };
        renderTimer = new Timer("UI Rendering");
        renderTimer.schedule(render_ui, 0,150);
    }


    public void startTrain(ActionEvent actionEvent) throws InterruptedException {
        train(false);
    }

    public void draw(ActionEvent actionEvent) {
        Platform.runLater(() -> {redraw();});
    }

    public void stop() {
        trainingThread.interrupt();
        renderTimer.cancel();
        System.out.println("Training interrupted !");
    }

    public void startInfiniteTrain(ActionEvent actionEvent) {
        train(true);
    }

    public void resetNetwork(ActionEvent actionEvent) {
        ui_lock.lock();
        network.reset();
        iterations = 0;
        Platform.runLater(() -> {redraw();});
        ui_lock.unlock();
    }
}