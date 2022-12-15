package task2;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ClassToFillFromProperties {

    private String stringProperty;

    @Property(name="numberProperty")
    private int myNumber;

    @Property(format="dd.MM.yyyy HH:mm")
    private Instant timeProperty;

    @Override
    public String toString() {
        return "stringProperty - " + stringProperty +
                ", myNumber - " + myNumber +
                ", timeProperty - " + timeProperty;
    }
}
