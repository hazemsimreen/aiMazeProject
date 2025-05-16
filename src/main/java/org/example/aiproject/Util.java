package org.example.aiproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javax.swing.JOptionPane;

public class Util {

    public static GridPane getGridFromMainPane(javafx.scene.layout.BorderPane mainPane) {
        if (mainPane != null && mainPane.getCenter() instanceof GridPane) {
            return (GridPane) mainPane.getCenter();
        }
        return null;
    }

    public static Button getButtonAt(GridPane grid, int x, int y) {
        for (Node node : grid.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);
            if (colIndex != null && rowIndex != null && colIndex == x && rowIndex == y) {
                return (Button) node;
            }
        }
        return null;
    }

    public static void resetGridColors(GridPane grid, Button currentStartingPoint, Button currentEndingPoint) {
        for (Node node : grid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                String terrainType = (String) button.getProperties().get("terrainType");
                String baseStyle = (String) button.getProperties().get("baseStyle");

                // Don't reset special points or obstacles/water
                if (button.equals(currentStartingPoint) || button.equals(currentEndingPoint)) {
                    continue;
                }

                if ("Grass".equals(terrainType)) {
                    button.setStyle(baseStyle + " -fx-background-color: mediumseagreen;");
                } else if ("Water".equals(terrainType)) {
                    button.setStyle(baseStyle + " -fx-background-color: aqua;");
                } else if ("Obstacle".equals(terrainType)) {
                    button.setStyle(baseStyle + " -fx-background-color: dimgray;");
                }
            }
        }
    }

    public static double manhattanDistance(GridController.AStarNode a, GridController.AStarNode b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static Map<String, Object> trainPerceptronUsingInputData() {
        String fileName = "data.txt";
        List<double[]> inputList = new ArrayList<>();
        List<Double> labelList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length == 4) {
                    double[] inputs = new double[3];
                    for (int i = 0; i < 3; i++) {
                        inputs[i] = Double.parseDouble(tokens[i]);
                    }
                    double label = Double.parseDouble(tokens[3]);
                    inputList.add(inputs);
                    labelList.add(label);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error reading training data file: " + e.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Combine inputs and labels for shuffling
        List<DataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
            dataPoints.add(new DataPoint(inputList.get(i), labelList.get(i)));
        }

        // Shuffle the data
        Collections.shuffle(dataPoints);

        // Split into training (80%) and testing (20%)
        int splitIndex = (int) (dataPoints.size() * 0.8);
        List<DataPoint> trainingData = dataPoints.subList(0, splitIndex);
        List<DataPoint> testingData = dataPoints.subList(splitIndex, dataPoints.size());

        // Convert training data to arrays
        double[][] trainInputs = new double[trainingData.size()][];
        double[] trainLabels = new double[trainingData.size()];
        for (int i = 0; i < trainingData.size(); i++) {
            trainInputs[i] = trainingData.get(i).inputs;
            trainLabels[i] = trainingData.get(i).label;
        }

        // Train the perceptron
        Perceptron perceptron = new Perceptron(3, 0.05);
        perceptron.train(trainInputs, trainLabels, 2500);

        // Evaluate on test data
        int correct = 0;
        for (DataPoint testPoint : testingData) {
            int prediction = perceptron.predict(testPoint.inputs);
            if (prediction == (int) testPoint.label) {
                correct++;
            }
        }

        double accuracy = (double) correct / testingData.size() * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("perceptron", perceptron);
        result.put("accuracy", accuracy);

        return result;
    }

// Helper class to keep inputs and labels together
    private static class DataPoint {

        double[] inputs;
        double label;

        DataPoint(double[] inputs, double label) {
            this.inputs = inputs;
            this.label = label;
        }
    }

}
