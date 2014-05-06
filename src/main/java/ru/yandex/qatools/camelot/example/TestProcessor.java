package ru.yandex.qatools.camelot.example;

import ru.yandex.qatools.camelot.api.annotations.Processor;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestProcessor {

    @Processor
    public String onNodeEvent(String event) {
        return event + "processed";
    }

    @Processor
    public Object fallback(Object event) {
        return event;
    }
}
