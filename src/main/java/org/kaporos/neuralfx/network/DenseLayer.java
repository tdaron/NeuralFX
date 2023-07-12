package org.kaporos.neuralfx.network;

public class DenseLayer extends Layer{

    double[][] weights;
    double[][] biases;

    double[][] input;

    int input_shape;
    int output_shape;
    int constant;

    public DenseLayer(int input_shape, int output_shape) {
        this.input_shape = input_shape;
        this.output_shape = output_shape;
        reset();
    }
    public DenseLayer(int input_shape, int output_shape, int constant) {
        this.input_shape = input_shape + 1;
        this.output_shape = output_shape;
        this.constant = constant;
        reset();
    }

    private double[][] add_constant(double[][] input) {
        if (constant > 0) {
            double[] new_input_0 = new double[input_shape];
            for (int x = 0; x < input[0].length; x++) {
                new_input_0[x] = input[0][x];
            }
            new_input_0[input_shape-1] = constant;
            input[0] = new_input_0;
        }
        return input;
    }
    private double[][] remove_constant(double [][] input) {
        if (constant > 0) {
            double[] new_input_0 = new double[input_shape-1];
            for (int x = 0; x < input[0].length - 1; x++) {
                new_input_0[x] = input[0][x];
            }
            input[0] = new_input_0;
        }
        return input;
    }
    @Override
    public double[][] feed_forward(double[][] input) {
        input = add_constant(input);
        this.input = input;
        return np.add(np.dot(input, weights), biases);
    }
    @Override
    public double[][] backward_propagation(double[][] output_error, double learning_rate) {
        var input_errors = np.dot(output_error, np.T(weights));
        var weight_errors = np.dot(np.T(input), output_error);

        weights = np.subtract(weights, np.multiply(learning_rate, weight_errors));
        biases = np.subtract(biases, np.multiply(learning_rate, output_error));
        return remove_constant(input_errors);
    }

    @Override
    public void reset() {
        weights = np.subtract(np.random(input_shape, output_shape), 0.5);
        biases  = np.subtract(np.random(1,output_shape), 0.5);
    }
}
