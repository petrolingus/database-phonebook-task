package me.petrolingus.phonebook.infrastructure.converter;

import javafx.util.Pair;
import javafx.util.StringConverter;

public class ChoiceBoxStringConverter extends StringConverter<Pair<Integer, String>> {

    @Override
    public String toString(Pair<Integer, String> object) {
        return object != null ? object.getValue() : "";
    }

    @Override
    public Pair<Integer, String> fromString(String string) {
        return null;
    }
}
