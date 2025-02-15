package sicxesimulator;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SymbolEntry {
    private final StringProperty name;
    private final StringProperty address;

    public SymbolEntry(String name, String address) {
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty addressProperty() {
        return address;
    }
}