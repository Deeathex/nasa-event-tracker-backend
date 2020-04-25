package NASA.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Coordinates implements Serializable {
    private double latitude;
    private double longitude;
}
