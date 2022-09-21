package ru.practicum.shareit.exception;

public class handelArgumentNotValidException extends RuntimeException {
    public handelArgumentNotValidException(String message) {
        super(message);

    }

    public String getDetailMessage() {
        return getMessage();
    }
}
