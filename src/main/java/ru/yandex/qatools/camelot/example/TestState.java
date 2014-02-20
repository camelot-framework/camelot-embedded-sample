package ru.yandex.qatools.camelot.example;

import java.io.Serializable;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestState implements Serializable {
    String message;

    public TestState() {
    }

    public TestState(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
