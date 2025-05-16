package org.example.aiproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javax.swing.JOptionPane;

public class Util {

    /**
     * Retrieves the GridPane from the center of the main pane.
     *
     * @param mainPane The BorderPane containing the grid.
     * @return The GridPane if found, otherwise null.
     */
    public static GridPane getGridFromMainPane(javafx.scene.layout.BorderPane mainPane) {
        if (mainPane != null && mainPane.getCenter() instanceof GridPane) {
            return (GridPane) mainPane.getCenter();
        }
        return null;
    }

    /**
     * Gets the Button at the specified row and column indices in the GridPane.
     *
     * @param grid The GridPane to search within.
     * @param x The column index.
     * @param y The row index.
     * @return The Button at the specified coordinates, or null if not found.
     */
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

    /**
     * Resets the background color of all buttons in the grid based on their
     * stored terrain type.
     *
     * @param grid The GridPane to process.
     * @param currentStartingPoint The current starting point button.
     * @param currentEndingPoint The current ending point button.
     */
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

    /**
     * Calculates the Manhattan distance between two AStarNodes.
     *
     * @param a The first AStarNode.
     * @param b The second AStarNode.
     * @return The Manhattan distance between the two nodes.
     */
    public static double manhattanDistance(GridController.AStarNode a, GridController.AStarNode b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static Perceptron trainPerceptronUsingInputData() {
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
        double[][] inputArray = inputList.toArray(new double[0][]);
        double[] labelArray = labelList.stream().mapToDouble(Double::doubleValue).toArray();

        Perceptron perceptron = new Perceptron(3, 0.05);
        perceptron.train(inputArray, labelArray, 250);
        return perceptron;
    }

}
