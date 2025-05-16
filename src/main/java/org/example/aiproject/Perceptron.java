package org.example.aiproject;

import java.util.Random;

public class Perceptron {

    private double[] weights;
    private double threshold;
    private double learningRate;

    public Perceptron(int inputSize, double learningRate) {
        weights = new double[inputSize];
        this.learningRate = learningRate;
        Random rand = new Random();
        for (int i = 0; i < inputSize; i++) {
            weights[i] = rand.nextDouble() - 0.5;
        }
        threshold = rand.nextDouble() - 0.5;
    }

    public int predict(double[] inputs) {
        double sum = threshold;
        for (int i = 0; i < inputs.length; i++) {
            sum += weights[i] * inputs[i];
        }
        return sum >= 0 ? 1 : 0;
    }

    public void train(double[][] inputSamples, double[] labels, int epochs) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            int errors = 0;
            for (int i = 0; i < inputSamples.length; i++) {
                int prediction = predict(inputSamples[i]);
                double error = labels[i] - prediction;
                if (error != 0) {
                    errors++;
                }

                for (int j = 0; j < weights.length; j++) {
                    weights[j] += learningRate * error * inputSamples[i][j];
                }
                threshold += learningRate * error;
            }
            if (errors == 0) {
                break; // Early stopping
            }

        }
    }
}
