package org.example.aiproject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

import javax.swing.JOptionPane;

public class InputGridController {

    @FXML
    private TextField columnsCount;

    @FXML
    private Button create;

    @FXML
    private TextField rowsCount;

    @FXML
    private void createGrid(ActionEvent event) {
        try {
            int rows = Integer.parseInt(rowsCount.getText().trim());
            int columns = Integer.parseInt(columnsCount.getText().trim());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/aiproject/grid.fxml"));
            Parent root = loader.load();

            // Pass values to GridController
            GridController gridController = loader.getController();
            gridController.setRowsCount(rows);
            gridController.setColumnsCount(columns);

            // Show the new scene with size 500x740
            Scene scene = new Scene(root, 560, 740);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid integer values for rows and columns.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to load grid.fxml.\n" + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
