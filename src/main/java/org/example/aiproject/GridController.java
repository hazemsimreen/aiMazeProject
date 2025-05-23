package org.example.aiproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import javafx.application.Platform;

import javafx.scene.paint.Color;
import javax.swing.JOptionPane;

public class GridController {

    //for stopping the search when we press randomize tiles
    private volatile boolean stopSearch = false;

    private Button currentStartingPoint = null;
    private Button currentEndingPoint = null;

    Perceptron perceptron;

    @FXML
    BorderPane mainPane;

    private int rowsCount;
    private int columnsCount;

    String baseStyle;

    @FXML
    Label accuracyLabel;

    @FXML
    public void initialize() {
        perceptron = (Perceptron) Util.trainPerceptronUsingInputData().get("perceptron");
        Double accuracy = (Double) Util.trainPerceptronUsingInputData().get("accuracy");
        accuracyLabel.setText("The accuracy of our training was " + String.valueOf(accuracy) + "%");
    }

    @FXML
    public void createGrid() {
        try {
            int rows = rowsCount;
            int columns = columnsCount;

            if (mainPane == null) {
                System.err.println("Error: mainPane is null");
                return;
            }

            mainPane.setPrefSize(560, 550); // Set preferred size for main pane

            GridPane grid = new GridPane();
            grid.setPrefSize(500, 500);
            grid.setGridLinesVisible(true);

            double tileWidth = 500.0 / columns;
            double tileHeight = 500.0 / rows;
            // More dynamic font size calculation based on both width and height
            double fontSize = Math.min(tileWidth, tileHeight) * 0.4; // Increased multiplier for better visibility
            fontSize = Math.max(8, Math.min(fontSize, 24)); // Set min and max bounds for font size

            grid.getColumnConstraints().clear();
            grid.getRowConstraints().clear();

            for (int col = 0; col < columns; col++) {
                ColumnConstraints colConst = new ColumnConstraints();
                colConst.setPercentWidth(100.0 / columns);
                grid.getColumnConstraints().add(colConst);
            }

            for (int row = 0; row < rows; row++) {
                RowConstraints rowConst = new RowConstraints();
                rowConst.setPercentHeight(100.0 / rows);
                grid.getRowConstraints().add(rowConst);
            }

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    Button button = new Button();
                    button.setMinSize(0, 0);
                    button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    // Store the base style with font size separately
                    baseStyle = String.format("-fx-font-size: %.2fpx;", fontSize);
                    button.getProperties().put("baseStyle", baseStyle);
                    button.setStyle(baseStyle);

                    grid.add(button, col, row);
                    GridPane.setHgrow(button, Priority.ALWAYS);
                    GridPane.setVgrow(button, Priority.ALWAYS);
                    button.setOnAction(e -> showDiallogBox(button));
                }
            }

            mainPane.setCenter(grid);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please enter valid numbers for rows and columns.");
            alert.showAndWait();
        }
    }

    @FXML
    private void randomizeTiles(ActionEvent event) {
        //if a search is running, stop it
        stopCurrentSearch();

        // Get the grid from mainPane's center
        GridPane grid = Util.getGridFromMainPane(mainPane);

        if (grid == null) {
            JOptionPane.showMessageDialog(null,
                    "Grid not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Clear current starting and ending points
        if (currentStartingPoint != null) {
            currentStartingPoint.getProperties().put("isStartingPoint", false);
            currentStartingPoint.setBorder(null);
            currentStartingPoint = null;
        }
        if (currentEndingPoint != null) {
            currentEndingPoint.getProperties().put("isEndingPoint", false);
            currentEndingPoint.setBorder(null);
            currentEndingPoint = null;
        }

        // First collect all safe grass tiles with elevation > 0
        List<Button> eligibleSafeTiles = new ArrayList<>();

        // Get all buttons from the grid and randomize their properties
        for (Node node : grid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;

                // Randomly select terrain type (Grass, Water, or Obstacle)
                double randomValue = Math.random();
                String randomTerrain;

                // 70% grass, 15% water, 15% obstacle
                if (randomValue < 0.7) {
                    randomTerrain = "Grass";
                } else if (randomValue < 0.85) {
                    randomTerrain = "Water";
                } else {
                    randomTerrain = "Obstacle";
                }

                // Random elevation between 0 and 10
                int randomElevation = (int) (Math.random() * 11);

                // Apply the random terrain style
                switch (randomTerrain) {
                    case "Grass":
                        button.setStyle(baseStyle + " -fx-background-color: mediumseagreen;");
                        break;
                    case "Water":
                        button.setStyle(baseStyle + " -fx-background-color: aqua;");
                        break;
                    case "Obstacle":
                        button.setStyle(baseStyle + " -fx-background-color: dimgray;");
                        break;
                }

                // Store the terrain type and elevation in the button's properties
                button.getProperties().put("terrainType", randomTerrain);
                button.getProperties().put("elevation", randomElevation);

                button.setText(String.valueOf(randomElevation));

                // Check if tile is safe and grass with elevation > 0
                int x = GridPane.getColumnIndex(button);
                int y = GridPane.getRowIndex(button);
                if ("Grass".equals(randomTerrain) && randomElevation > 0 && isTileSafe(grid, x, y)) {
                    eligibleSafeTiles.add(button);
                }
            }
        }

        // Set random starting and ending points (must be on safe grass with elevation > 0 and different tiles)
        if (eligibleSafeTiles.size() >= 2) {
            // Keep trying to find valid points until we get safe ones
            boolean validPointsSet = false;
            int attempts = 0;
            final int MAX_ATTEMPTS = 10; // Prevent infinite loops

            while (!validPointsSet && attempts < MAX_ATTEMPTS) {
                attempts++;

                // Random starting point
                int startIndex = (int) (Math.random() * eligibleSafeTiles.size());
                Button startButton = eligibleSafeTiles.get(startIndex);

                // Remove starting point from available tiles
                List<Button> remainingTiles = new ArrayList<>(eligibleSafeTiles);
                remainingTiles.remove(startIndex);

                if (!remainingTiles.isEmpty()) {
                    // Random ending point (from remaining tiles)
                    int endIndex = (int) (Math.random() * remainingTiles.size());
                    Button endButton = remainingTiles.get(endIndex);

                    // Verify both points are safe (should be true since we pre-filtered)
                    if (isTileSafe(grid, GridPane.getColumnIndex(startButton), GridPane.getRowIndex(startButton))
                            && isTileSafe(grid, GridPane.getColumnIndex(endButton), GridPane.getRowIndex(endButton))) {

                        setStartingPoint(startButton);
                        setEndingPoint(endButton);
                        validPointsSet = true;
                    }
                }
            }

            if (!validPointsSet) {
                JOptionPane.showMessageDialog(null,
                        "Failed to find safe starting and ending points after multiple attempts.",
                        "No Safe Points Found",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else if (eligibleSafeTiles.size() == 1) {
            // Only one eligible safe tile - set it as starting point if safe
            Button startButton = eligibleSafeTiles.get(0);
            if (isTileSafe(grid, GridPane.getColumnIndex(startButton), GridPane.getRowIndex(startButton))) {
                setStartingPoint(startButton);
                JOptionPane.showMessageDialog(null,
                        "Could only set starting point. Add more safe grass tiles with elevation > 0 to set an ending point.",
                        "Only one eligible safe tile available",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Could not set starting or ending points. Add some safe grass tiles with elevation > 0 first.",
                    "No eligible safe tiles available",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    @FXML
    private void runSearch(ActionEvent event) {
        // Get the grid from mainPane's center
        GridPane grid = Util.getGridFromMainPane(mainPane);

        if (grid == null) {
            JOptionPane.showMessageDialog(null,
                    "Grid not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if we have both start and end points
        if (currentStartingPoint == null || currentEndingPoint == null) {
            JOptionPane.showMessageDialog(null,
                    "Please set both starting and ending points before running search.",
                    "Missing Points",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get coordinates of start and end points
        final int startX = GridPane.getColumnIndex(currentStartingPoint);
        final int startY = GridPane.getRowIndex(currentStartingPoint);
        final int endX = GridPane.getColumnIndex(currentEndingPoint);
        final int endY = GridPane.getRowIndex(currentEndingPoint);
        final GridPane finalGrid = grid;

        // Reset all tile colors (except obstacles, water, and special points)
        Util.resetGridColors(grid, currentStartingPoint, currentEndingPoint);

        // Create a new thread for the search to allow for animation
        new Thread(() -> {
            List<AStarNode> testedPath = new ArrayList<>();
            List<AStarNode> finalPath = aStarSearch(finalGrid, startX, startY, endX, endY, testedPath);

            // After search completes, show the final path with delay
            Platform.runLater(() -> {
                if (finalPath != null && !finalPath.isEmpty()) {
                    // Animate the final path one tile at a time
                    animateFinalPath(finalGrid, finalPath, 0);

                } //Make sure it won't run when we stop search (since we also return null)
                else if (!stopSearch) {

                    JOptionPane.showMessageDialog(null,
                            "No safe path found from start to end!",
                            "No Path Found",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }).start();
    }

    private List<AStarNode> aStarSearch(GridPane grid, int startX, int startY, int endX, int endY, List<AStarNode> testedPath) {

        //flag a new search operation
        stopSearch = false;

        // Create open and closed lists
        PriorityQueue<AStarNode> openList = new PriorityQueue<>();
        List<AStarNode> closedList = new ArrayList<>();

        // Create start and end nodes
        AStarNode startNode = new AStarNode(startX, startY);
        AStarNode endNode = new AStarNode(endX, endY);

        // Add the start node to open list
        openList.add(startNode);

        //mark starting node as visited (via filling it)
        Platform.runLater(() -> {
            Button button = Util.getButtonAt(grid, startNode.x, startNode.y);
            if (button != null) {
                button.setStyle(baseStyle + " -fx-background-color: red;");
            }
        });

        while (!openList.isEmpty()) {

            if (stopSearch) {
                return null;
            }

            // Get the node with the lowest f cost
            AStarNode currentNode = openList.poll();
            testedPath.add(currentNode);

            // Update the UI to show the tested node
            Platform.runLater(() -> {
                Button button = Util.getButtonAt(grid, currentNode.x, currentNode.y);
                if (button != null && !button.equals(currentStartingPoint) && !button.equals(currentEndingPoint)) {
                    button.setStyle(baseStyle + " -fx-background-color: lightcoral;");
                }
            });

            // Add a small delay for visualization
            try {
                Thread.sleep(300); // 300ms delay between node expansions
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }

            // Check if we've reached the end
            if (currentNode.equals(endNode)) {
                Button button = Util.getButtonAt(grid, currentNode.x, currentNode.y);
                if (button != null) {
                    button.setStyle(baseStyle + " -fx-background-color: orange;");
                }
                return reconstructPath(currentNode);
            }

            // Generate neighbors
            List<AStarNode> neighbors = getNeighbors(grid, currentNode);

            for (AStarNode neighbor : neighbors) {
                // Skip if neighbor is in closed list or not walkable
                if (closedList.contains(neighbor) || !isTileSafe(grid, neighbor.x, neighbor.y)) {
                    continue;
                }

                // Calculate tentative g score
                double tentativeGScore = currentNode.g + 1; // Assuming each step costs 1

                // Check if this path to neighbor is better
                if (!openList.contains(neighbor) || tentativeGScore < neighbor.g) {
                    neighbor.parent = currentNode;
                    neighbor.g = tentativeGScore;
                    neighbor.h = Util.manhattanDistance(neighbor, endNode);
                    neighbor.f = neighbor.g + neighbor.h;

                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }

            // Add current node to closed list
            closedList.add(currentNode);
        }

        // No path found
        return null;
    }

    private void animateFinalPath(GridPane grid, List<AStarNode> path, int index) {
        if (index >= path.size()) {
            return;
        }

        AStarNode node = path.get(index);
        Button button = Util.getButtonAt(grid, node.x, node.y);
        if (button != null && !button.equals(currentStartingPoint) && !button.equals(currentEndingPoint)) {
            button.setStyle(baseStyle + " -fx-background-color: lime;");
        }

        // Schedule the next animation step after a delay
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    animateFinalPath(grid, path, index + 1);
                });
            }
        },
                300 // 300ms delay between path segments
        );
    }

    private List<AStarNode> getNeighbors(GridPane grid, AStarNode node) {
        List<AStarNode> neighbors = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // 4-directional movement

        for (int[] dir : directions) {
            int newX = node.x + dir[0];
            int newY = node.y + dir[1];

            // Check if within grid bounds
            if (newX >= 0 && newX < grid.getColumnCount() && newY >= 0 && newY < grid.getRowCount()) {
                neighbors.add(new AStarNode(newX, newY));
            }
        }

        return neighbors;
    }

    private boolean isTileSafe(GridPane grid, int x, int y) {
        Button button = Util.getButtonAt(grid, x, y);
        if (button == null) {
            return false;
        }

        // Get tile properties
        String terrainType = (String) button.getProperties().get("terrainType");
        int elevation = (int) button.getProperties().get("elevation");

        // Calculate Manhattan distance to nearest obstacle
        int distanceToObstacle = calculateDistanceToNearestObstacle(grid, x, y);

        // Prepare features for perceptron
        double[] features = new double[3];
        features[0] = "Grass".equals(terrainType) ? 0 : 1; // Grass=0, Water=1
        features[1] = elevation;
        features[2] = distanceToObstacle;

        // Use perceptron to classify
        return perceptron.predict(features) == 1;
    }

    private int calculateDistanceToNearestObstacle(GridPane grid, int x, int y) {
        int minDistance = Integer.MAX_VALUE;

        for (Node node : grid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if ("Obstacle".equals(button.getProperties().get("terrainType"))) {
                    int obstacleX = GridPane.getColumnIndex(button);
                    int obstacleY = GridPane.getRowIndex(button);
                    int distance = (int) Util.manhattanDistance(new AStarNode(x, y), new AStarNode(obstacleX, obstacleY));
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
            }
        }

        return minDistance == Integer.MAX_VALUE ? 10 : minDistance; // Default to 10 if no obstacles
    }

    private List<AStarNode> reconstructPath(AStarNode endNode) {
        List<AStarNode> path = new ArrayList<>();
        AStarNode current = endNode;

        while (current != null) {
            path.add(0, current); // Add to beginning to reverse the order
            current = current.parent;
        }

        return path;
    }

// Renamed Node class to AStarNode
    public static class AStarNode implements Comparable<AStarNode> {

        int x, y;
        double f = 0, g = 0, h = 0;
        AStarNode parent = null;

        public AStarNode(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            AStarNode node = (AStarNode) obj;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.f, other.f);
        }
    }

    @FXML
    public void showDiallogBox(Button clickedButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("combo.fxml"));
            Parent root = loader.load();

            ComboBoxController comboController = loader.getController();
            comboController.setTargetButton(clickedButton);
            comboController.setMainController(this);  // Pass the reference to InputGridController

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root, 490, 305));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading dialog: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setStartingPoint(Button button) {
        // Check if tile is safe
        GridPane grid = Util.getGridFromMainPane(mainPane);
        if (grid != null && button != null) {
            int x = GridPane.getColumnIndex(button);
            int y = GridPane.getRowIndex(button);
            if (!isTileSafe(grid, x, y)) {
                JOptionPane.showMessageDialog(null,
                        "Cannot set starting point on an unsafe tile!",
                        "Invalid Tile",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Clear previous starting point if exists
        if (currentStartingPoint != null) {
            currentStartingPoint.getProperties().put("isStartingPoint", false);
            currentStartingPoint.setText(currentStartingPoint.getProperties().get("elevation").toString());
            currentStartingPoint.setBorder(null);
        }

        // Set new starting point
        currentStartingPoint = button;
        if (button != null) {
            button.getProperties().put("isStartingPoint", true);
            button.setBorder(new Border(new BorderStroke(
                    Color.RED,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(0),
                    new BorderWidths(3))));
            button.setText("S");
        }
    }

    public void setEndingPoint(Button button) {

        // Check if tile is safe
        GridPane grid = Util.getGridFromMainPane(mainPane);
        if (grid != null && button != null) {
            int x = GridPane.getColumnIndex(button);
            int y = GridPane.getRowIndex(button);
            if (!isTileSafe(grid, x, y)) {
                JOptionPane.showMessageDialog(null,
                        "Cannot set ending point on an unsafe tile!",
                        "Invalid Tile",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Clear previous ending point if exists
        if (currentEndingPoint != null) {
            currentEndingPoint.getProperties().put("isEndingPoint", false);
            currentEndingPoint.setText(currentEndingPoint.getProperties().get("elevation").toString());
            currentEndingPoint.setBorder(null);
        }

        // Set new ending point
        currentEndingPoint = button;
        if (button != null) {
            button.getProperties().put("isEndingPoint", true);
            button.setBorder(new Border(new BorderStroke(
                    Color.ORANGE,
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(0),
                    new BorderWidths(3))));
            button.setText("E");

        }
    }

    public void setRowsCount(int rowsCount) {
        this.rowsCount = rowsCount;
    }

    public void setColumnsCount(int columnsCount) {
        this.columnsCount = columnsCount;

        //run here, since its set after rows
        createGrid();
    }

    private void stopCurrentSearch() {
        stopSearch = true;
        // Reset the flag after a short delay to allow new searches
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
            @Override
            public void run() {
                stopSearch = false;
            }
        },
                600
        );
    }

}
