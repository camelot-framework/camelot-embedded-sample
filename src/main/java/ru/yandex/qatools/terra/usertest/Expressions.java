package ru.yandex.qatools.terra.usertest;

import ru.yandex.qatools.clay.annotations.Header;

import static ru.yandex.qatools.terra.api.Constants.Headers.UUID;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class Expressions {
    public static final String BY_UUID = "byUuid";

    public String byUuid(@Header(UUID) String correlationKey) {
        return correlationKey;
    }
}
