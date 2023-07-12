package org.kaporos.neuralfx.network;


public abstract class Layer {

    public double[][] feed_forward(double[][] input) {
        return input;
    }
    public double[][] backward_propagation(double[][] output_error, double learning_rate) {
        return output_error;
    }

    public void reset() {

    }

}

