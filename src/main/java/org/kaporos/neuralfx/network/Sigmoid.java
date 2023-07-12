package org.kaporos.neuralfx.network;

public class Sigmoid extends Layer{
    double[][] input;
    @Override
    public double[][] feed_forward(double[][] input) {
        this.input = input;
        return np.sigmoid(input);
    }

    @Override
    public double[][] backward_propagation(double[][] output_error, double learning_rate) {
        return np.multiply(np.sigmoid_prime(input), output_error);
    }
}
