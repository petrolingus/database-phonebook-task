package me.petrolingus.phonebook.infrastructure.mapper;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.Pair;

public class NodeMapper {

    public static String map(Node node) {

        if (node instanceof TextField) {
            return  "N'" + ((TextField) node).getText() + "'";
        } else if (node instanceof ChoiceBox) {
            //noinspection unchecked
            ChoiceBox<Pair<Integer, String>> choiceBox = (ChoiceBox<Pair<Integer, String>>) node;
            return  "N'" + ((choiceBox).getSelectionModel().getSelectedItem().getKey()) + "'";
        }
        return null;
    }
}
