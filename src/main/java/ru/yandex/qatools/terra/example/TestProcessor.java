package ru.yandex.qatools.terra.example;

import ru.yandex.qatools.clay.annotations.Body;
import ru.yandex.qatools.clay.annotations.FallbackProcessor;
import ru.yandex.qatools.clay.annotations.Processor;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestProcessor {

    @Processor(bodyType = String.class)
    public String onNodeEvent(@Body String event) {
        return event + "processed";
    }

    @FallbackProcessor
    public Object fallback(@Body Object event) {
        return event;
    }
}
