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

public class TieController {

    @FXML
    private TextField columnsCount;

    @FXML
    private Button create;

    @FXML
    private TextField rowsCount;

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
            mainPane.setPrefSize(500, 500);

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

            // Add all components to the main pane
            mainPane.setCenter(grid);
            mainPane.setTop(topLabels);
            mainPane.setBottom(bottomLabels);
            mainPane.setLeft(leftLabels);
            mainPane.setRight(rightLabels);

            Scene gridScene = new Scene(mainPane, 500, 500);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(gridScene);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please enter valid numbers for rows and columns.");
            alert.showAndWait();
        }
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

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Manage");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root, 358, 254));
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
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
