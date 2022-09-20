package ru.practicum.shareit.exception;

public class Throwable extends RuntimeException {

    public Throwable(String message) {
        super(message);

    }

    public String getDetailMessage() {
        return getMessage();
    }
}
