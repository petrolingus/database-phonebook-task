package me.petrolingus.phonebook;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import me.petrolingus.phonebook.infrastructure.converter.ChoiceBoxStringConverter;
import me.petrolingus.phonebook.mapper.NodeMapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// TODO: 06.05.2021 fix duplicated code
@SuppressWarnings("DuplicatedCode")
public class Controller {

    public TableView<ObservableList<String>> tableView;

    public ToggleGroup tableSwitchGroup;

    private Connection connection;


    public void initialize() throws SQLException {

        String url = "jdbc:sqlserver://localhost:1433;databaseName=TestDB";
        String user = "SA";
        String password = "H#MTlWikoDOfbB1#";
        connection = DriverManager.getConnection(url, user, password);

        onTableSwitch();
    }

    public void onTableSwitch() throws SQLException {

        Toggle selectedToggle = tableSwitchGroup.getSelectedToggle();
        ObservableList<Toggle> toggles = tableSwitchGroup.getToggles();

        if (toggles.get(0).equals(selectedToggle)) {
            generateTable("select * from person");
        } else if (toggles.get(1).equals(selectedToggle)) {
            generateTable("select pn.id as id, phone, type, name from phone_number pn join provider p on pn.provider = p.id;");
        } else if (toggles.get(2).equals(selectedToggle)) {
            generateTable("select * from provider;");
        } else {
            generateTable("select person_id, phone_number_id, last_name, first_name, middle_name, phone, type, name from phone_contact" +
                    "\tjoin person on person.id = person_id" +
                    "\tjoin phone_number on phone_number.id = phone_number_id" +
                    "\tjoin provider on provider.id = phone_number.provider;");
        }
    }

    public void onAddButton() throws IOException {

        Toggle selectedToggle = tableSwitchGroup.getSelectedToggle();
        ObservableList<Toggle> toggles = tableSwitchGroup.getToggles();

        if (toggles.get(0).equals(selectedToggle)) {
            addPerson();
        } else if (toggles.get(1).equals(selectedToggle)) {
            addPhoneNumber();
        } else if (toggles.get(2).equals(selectedToggle)) {
            addProvider();
        } else {
            addPhonebook();
        }
    }

    public void onChangeButton() throws IOException {

    }

    public void onDeleteButton() {

        Toggle selectedToggle = tableSwitchGroup.getSelectedToggle();
        ObservableList<Toggle> toggles = tableSwitchGroup.getToggles();
        ObservableList<String> selectedItem = tableView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {

            try (Statement statement = connection.createStatement()) {

                if (toggles.get(0).equals(selectedToggle)) {
                    statement.execute("delete from person where person.id=" + selectedItem.get(0));
                } else if (toggles.get(1).equals(selectedToggle)) {
                    statement.execute("delete from phone_number where phone_number.id=" + selectedItem.get(0));
                } else if (toggles.get(2).equals(selectedToggle)) {
                    statement.execute("delete from provider where id=" + selectedItem.get(0));
                } else {
                    statement.execute("delete from phone_contact " +
                            "where person_id=" + selectedItem.get(0) +
                            "and phone_number_id=" + selectedItem.get(1)
                    );
                }
                onTableSwitch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void addPerson() throws IOException {

        String headerText = "Creating a new person and push it to the table.";
        Dialog<List<String>> dialog = createDialogue("/person.fxml", "Add Person Dialog", headerText, "Add");

        dialog.showAndWait().ifPresent(fields -> {
            String values = fields.stream().collect(Collectors.joining(", ", "(", ")"));
            try (Statement statement = connection.createStatement()) {
                statement.execute("insert into person values " + values);
                onTableSwitch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void addPhoneNumber() throws IOException {

        String headerText = "Creating a new phone and push it to the table.";
        Dialog<List<String>> dialog = createDialogue("/phone.fxml", "Add Phone Dialog", headerText, "Add");

        ChoiceBox<Pair<Integer, String>> choiceBoxPerson = createChoiceBox("select id, name from provider;", 2);
        ((VBox) ((VBox) dialog.getDialogPane().getChildren().get(3)).getChildren().get(2)).getChildren().add(choiceBoxPerson);

        dialog.showAndWait().ifPresent(fields -> {
            fields.forEach(System.out::println);
            String values = fields.stream().collect(Collectors.joining(", ", "(", ")"));
            try (Statement statement = connection.createStatement()) {
                statement.execute("insert into phone_number values " + values);
                onTableSwitch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void addProvider() throws IOException {

        String headerText = "Creating a new provider and push it to the table.";
        Dialog<List<String>> dialog = createDialogue("/provider.fxml", "Add Provider Dialog", headerText, "Add");

        dialog.showAndWait().ifPresent(fields -> {
            String values = fields.stream().collect(Collectors.joining(", ", "(", ")"));
            try (Statement statement = connection.createStatement()) {
                statement.execute("insert into provider values " + values);
                onTableSwitch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void addPhonebook() throws IOException {

        String headerText = "Creating a new phonebook data and push it to the table.";
        Dialog<List<String>> dialog = createDialogue("/phonebook.fxml", "Add Phonebook Dialog", headerText, "Add");

        ChoiceBox<Pair<Integer, String>> choiceBoxPerson = createChoiceBox("select * from person;", 2, 3, 4, 5);
        ((VBox) ((VBox) dialog.getDialogPane().getChildren().get(3)).getChildren().get(0)).getChildren().add(choiceBoxPerson);

        String phoneNumberQuery = "select pn.id as id, phone, type, name from phone_number pn join provider p on pn.provider = p.id;";
        ChoiceBox<Pair<Integer, String>> choiceBoxPhoneNumber = createChoiceBox(phoneNumberQuery, 2, 3, 4);
        ((VBox) ((VBox) dialog.getDialogPane().getChildren().get(3)).getChildren().get(1)).getChildren().add(choiceBoxPhoneNumber);

        dialog.showAndWait().ifPresent(fields -> {
            fields.forEach(System.out::println);
            String values = fields.stream().collect(Collectors.joining(", ", "(", ")"));
            try (Statement statement = connection.createStatement()) {
                statement.execute("insert into phone_contact values " + values);
                onTableSwitch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    private Dialog<List<String>> createDialogue(String name, String title, String headerText, String buttonText) throws IOException {

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        ButtonType loginButtonType = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        Parent content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(name)));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return content.getChildrenUnmodifiable()
                        .stream()
                        .filter(node -> node instanceof VBox)
                        .map(node -> ((VBox) node).getChildren().get(1))
                        .map(NodeMapper::map)
                        .collect(Collectors.toList());
            }
            return null;
        });

        return dialog;
    }

    private void generateTable(String sql) throws SQLException {

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        tableView.getColumns().clear();
        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            String dbColumnName = resultSet.getMetaData().getColumnName(i + 1);
            int finalI = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(dbColumnName);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(finalI)));
            tableView.getColumns().add(column);
        }

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        while (resultSet.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                row.add(resultSet.getString(i));
            }
            data.add(row);
        }

        tableView.getItems().clear();
        tableView.setItems(data);
    }

    private ChoiceBox<Pair<Integer, String>> createChoiceBox(String query, int... ids) {

        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            List<Pair<Integer, String>> providerRows = new ArrayList<>();
            while (resultSet.next()) {
                int key = resultSet.getInt(1);
                List<String> cols = new ArrayList<>();
                for (int i : ids) {
                    cols.add(resultSet.getString(i));
                }
                String value = String.join(" ", cols);
                providerRows.add(new Pair<>(key, value));
            }
            ChoiceBox<Pair<Integer, String>> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(providerRows));
            choiceBox.setMaxWidth(Double.MAX_VALUE);
            choiceBox.setConverter(new ChoiceBoxStringConverter());
            choiceBox.getSelectionModel().select(0);
            return choiceBox;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
