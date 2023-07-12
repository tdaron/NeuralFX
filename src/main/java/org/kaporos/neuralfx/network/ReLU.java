package org.kaporos.neuralfx.network;

import java.util.Arrays;

public class ReLU extends Layer{

    double[][] input;
    @Override
    public double[][] feed_forward(double[][] input) {
        this.input = input;
        input[0] = Arrays.stream(input[0]).map(x -> {
            if (x > 0) {
                return x;
            } else {
                return 0;
            }
        }).toArray();
        return input;
    }

    @Override
    public double[][] backward_propagation(double[][] output_error, double learning_rate) {
        for (int x = 0; x < input[0].length; x ++) {
            if (x < 0) {
                output_error[0][x] = 0;
            }
        }
        return output_error;
    }
}
