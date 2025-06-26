package com.example.androidtranscoder.engine;

public class InvalidOutputFormatException extends RuntimeException {
    public InvalidOutputFormatException(String detailMessage) {
        super(detailMessage);
    }
}

