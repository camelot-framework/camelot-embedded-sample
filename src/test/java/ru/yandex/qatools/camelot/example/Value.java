package ru.yandex.qatools.camelot.example;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class Value<T> {
    private T value;

    public Value(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
