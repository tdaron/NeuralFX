package org.kaporos.neuralfx.network;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Network {
    ArrayList<Layer> layers;
    double learning_rate;
    public Network(ArrayList<Layer> layers, double learning_rate) {
        this.layers = layers;
        this.learning_rate = learning_rate;
    }

    public double[][] feed_forward(double[][] input) {
        AtomicReference<double[][]> output = new AtomicReference<>(input);
        layers.forEach((layer) -> {
            output.set(layer.feed_forward(output.get()));
        });
        return output.get();
    }



    public void backpropagation(double[][] error) {
        for (int counter = layers.size()- 1; counter >= 0; counter -= 1) {
            error = layers.get(counter).backward_propagation(error, learning_rate);
        }
    }

    public void reset() {
        layers.forEach((layer) -> {
            layer.reset();
        });
    }


}
