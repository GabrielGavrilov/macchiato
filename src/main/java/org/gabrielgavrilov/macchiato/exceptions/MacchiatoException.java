package org.gabrielgavrilov.macchiato.exceptions;

import java.sql.SQLException;

public class MacchiatoException extends RuntimeException {
    public MacchiatoException(String message) {
        super(message);
    }
}
