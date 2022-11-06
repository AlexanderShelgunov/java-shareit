package ru.practicum.shareit.exception;

public class ServerException extends RuntimeException {

    public ServerException(String message) {
        super(message);

    }

    public String getDetailMessage() {
        return getMessage();
    }
}
