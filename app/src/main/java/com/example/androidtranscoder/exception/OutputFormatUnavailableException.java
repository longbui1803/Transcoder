package com.example.androidtranscoder.exception;

public class OutputFormatUnavailableException extends RuntimeException {
    public OutputFormatUnavailableException(String detailMessage) {
        super(detailMessage);
    }
}
