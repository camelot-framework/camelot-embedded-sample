package ru.yandex.qatools.terra.usertest;

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
}
