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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javax.swing.JOptionPane;

public class TieController {

    @FXML
    private TextField columnsCount;

    @FXML
    private Button create;

    @FXML
    private TextField rowsCount;

    private Button currentStartingPoint = null;
    private Button currentEndingPoint = null;

    Perceptron perceptron;

    public TieController() {
        trainPerceptronUsingInputData();
    }

    @FXML
    public void createGrid(ActionEvent actionEvent) {
        try {
            int rows = Integer.parseInt(rowsCount.getText());
            int columns = Integer.parseInt(columnsCount.getText());

            // Create a BorderPane to hold the grid and labels
            BorderPane mainPane = new BorderPane();
            mainPane.setPrefSize(500, 550); // Increased height to accommodate buttons

            GridPane grid = new GridPane();
            grid.setPrefSize(500, 500);
            grid.setGridLinesVisible(true);

            // Calculate tile size based on grid dimensions
            double tileWidth = 500.0 / columns;
            double tileHeight = 500.0 / rows;

            // Calculate font size based on tile size (smaller dimension)
            double fontSize = Math.min(tileWidth, tileHeight) * 0.2;

            // Set up column constraints
            for (int col = 0; col < columns; col++) {
                ColumnConstraints colConst = new ColumnConstraints();
                colConst.setPercentWidth(100.0 / columns);
                grid.getColumnConstraints().add(colConst);
            }

            // Set up row constraints
            for (int row = 0; row < rows; row++) {
                RowConstraints rowConst = new RowConstraints();
                rowConst.setPercentHeight(100.0 / rows);
                grid.getRowConstraints().add(rowConst);
            }

            // Create buttons for the grid
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    Button button = new Button();
                    button.setMinSize(0, 0);
                    button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                    grid.add(button, col, row);

                    GridPane.setHgrow(button, Priority.ALWAYS);
                    GridPane.setVgrow(button, Priority.ALWAYS);

                    button.setOnAction(e -> showDiallogBox(button));
                }
            }

            // Create top and bottom coordinate labels (for x-axis)
            HBox topLabels = new HBox();
            topLabels.setPrefHeight(fontSize * 2); // Give some space for the labels
            HBox bottomLabels = new HBox();
            bottomLabels.setPrefHeight(fontSize * 2);

            for (int col = 0; col < columns; col++) {
                Label topLabel = createCoordinateLabel(String.valueOf(col), fontSize);
                Label bottomLabel = createCoordinateLabel(String.valueOf(col), fontSize);

                // Set flexible width to match tile width
                topLabel.setPrefWidth(tileWidth);
                bottomLabel.setPrefWidth(tileWidth);

                topLabels.getChildren().add(topLabel);
                bottomLabels.getChildren().add(bottomLabel);
            }

            // Create left and right coordinate labels (for y-axis)
            VBox leftLabels = new VBox();
            leftLabels.setPrefWidth(fontSize * 2);
            VBox rightLabels = new VBox();
            rightLabels.setPrefWidth(fontSize * 2);

            //extra padding for rightLabels
            rightLabels.setPadding(new Insets(5, 5, 5, 10));

            for (int row = 0; row < rows; row++) {
                Label leftLabel = createCoordinateLabel(String.valueOf(row), fontSize);
                Label rightLabel = createCoordinateLabel(String.valueOf(row), fontSize);

                // Set flexible height to match tile height
                leftLabel.setPrefHeight(tileHeight);
                rightLabel.setPrefHeight(tileHeight);

                leftLabels.getChildren().add(leftLabel);
                rightLabels.getChildren().add(rightLabel);
            }

            // Create control buttons at the bottom
            HBox controlButtons = new HBox(20); // 20px spacing between buttons
            controlButtons.setPadding(new Insets(10));
            controlButtons.setAlignment(Pos.CENTER);

            Button randomizeButton = new Button("Randomize Tiles");
            randomizeButton.setOnAction(this::randomizeTiles);

            Button runSearchButton = new Button("Run Search");
            runSearchButton.setOnAction(this::runSearch);

            controlButtons.getChildren().addAll(randomizeButton, runSearchButton);

            // Add all components to the main pane
            mainPane.setCenter(grid);
            mainPane.setTop(topLabels);
            mainPane.setBottom(new VBox(bottomLabels, controlButtons)); // Combine labels and buttons
            mainPane.setLeft(leftLabels);
            mainPane.setRight(rightLabels);

            Scene gridScene = new Scene(mainPane, 500, 550); // Increased height
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(gridScene);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please enter valid numbers for rows and columns.");
            alert.showAndWait();
        }
    }

    @FXML
    private void randomizeTiles(ActionEvent event) {
        // Get the current scene and find the GridPane in it
        Scene currentScene = ((Node) event.getSource()).getScene();
        GridPane grid = null;

        // Search for the GridPane in the scene's root node
        if (currentScene.getRoot() instanceof BorderPane) {
            BorderPane root = (BorderPane) currentScene.getRoot();
            grid = (GridPane) root.getCenter();
        }

        if (grid == null) {
            System.out.println("Grid not found!");
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

        // First collect all grass tiles
        List<Button> grassTiles = new ArrayList<>();

        // Get all buttons from the grid and randomize their properties
        for (Node node : grid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;

                // Randomly select terrain type (Grass, Water, or Obstacle)
                String[] terrainTypes = {"Grass", "Water", "Obstacle"};
                String randomTerrain = terrainTypes[(int) (Math.random() * terrainTypes.length)];

                // Random elevation between 0 and 10
                int randomElevation = (int) (Math.random() * 11);

                // Apply the random terrain style
                switch (randomTerrain) {
                    case "Grass":
                        button.setStyle("-fx-background-color: mediumseagreen;");
                        grassTiles.add(button); // Add to grass tiles list
                        break;
                    case "Water":
                        button.setStyle("-fx-background-color: aqua;");
                        break;
                    case "Obstacle":
                        button.setStyle("-fx-background-color: dimgray;");
                        break;
                }

                // Store the terrain type and elevation in the button's properties
                button.getProperties().put("terrainType", randomTerrain);
                button.getProperties().put("elevation", randomElevation);

                button.setText(String.valueOf(randomElevation));
            }
        }

        // Set random starting and ending points (must be on grass and different tiles)
        if (grassTiles.size() >= 2) {
            // Random starting point
            int startIndex = (int) (Math.random() * grassTiles.size());
            Button startButton = grassTiles.get(startIndex);
            setStartingPoint(startButton);

            // Remove starting point from available grass tiles
            grassTiles.remove(startIndex);

            // Random ending point (from remaining grass tiles)
            int endIndex = (int) (Math.random() * grassTiles.size());
            Button endButton = grassTiles.get(endIndex);
            setEndingPoint(endButton);
        } else if (grassTiles.size() == 1) {
            // Only one grass tile - set it as starting point
            setStartingPoint(grassTiles.get(0));
            // Show warning about missing ending point using JOptionPane
            JOptionPane.showMessageDialog(null,
                    "Could only set starting point. Add more grass tiles to set an ending point.",
                    "Only one grass tile available",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            // No grass tiles available
            JOptionPane.showMessageDialog(null,
                    "Could not set starting or ending points. Add some grass tiles first.",
                    "No grass tiles available",
                    JOptionPane.WARNING_MESSAGE);
        }

        System.out.println("All tiles have been randomized!");
    }

    @FXML
    private void runSearch(ActionEvent event) {
        // TODO: Implement search logic
        throw new UnsupportedOperationException("runSearch not implemented yet");
    }

    private Label createCoordinateLabel(String text, double fontSize) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: " + fontSize + "px; -fx-alignment: center;");
        return label;
    }

    @FXML
    public void showDiallogBox(Button clickedButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("combo.fxml"));
            Parent root = loader.load();

            ComboBoxController comboController = loader.getController();
            comboController.setTargetButton(clickedButton);
            comboController.setMainController(this);  // Pass the reference to TieController

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root, 490, 305));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStartingPoint(Button button) {
        // Clear previous starting point if exists
        if (currentStartingPoint != null) {
            currentStartingPoint.getProperties().put("isStartingPoint", false);
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
        }
    }

    public void setEndingPoint(Button button) {
        // Clear previous ending point if exists
        if (currentEndingPoint != null) {
            currentEndingPoint.getProperties().put("isEndingPoint", false);
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
        }
    }

    private void trainPerceptronUsingInputData() {
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
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        double[][] inputArray = inputList.toArray(new double[0][]);
        double[] labelArray = labelList.stream().mapToDouble(Double::doubleValue).toArray();

        perceptron = new Perceptron(3);
        perceptron.train(inputArray, labelArray, 20);
    }

}
