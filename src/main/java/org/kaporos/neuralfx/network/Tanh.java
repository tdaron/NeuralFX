package org.kaporos.neuralfx.network;

public class Tanh extends Layer{
    double[][] input;
    @Override
    public double[][] feed_forward(double[][] input) {
        this.input = input;
        return np.tanh(input);
    }

    @Override
    public double[][] backward_propagation(double[][] output_error, double learning_rate) {
        return np.multiply(np.tanh_prime(input), output_error);
    }
}
