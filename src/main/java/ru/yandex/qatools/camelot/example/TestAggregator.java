package ru.yandex.qatools.camelot.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = {String.class, Float.class})
@Aggregate
@FSM(start = TestState.class)
@Transitions({
        @Transit(on = String.class),
        @Transit(stop = true, on = Float.class)
})
public class TestAggregator {
    @InjectHeader(UUID)
    String uuid;

    @Autowired
    ApplicationContext context;

    @ClientSender
    ClientMessageSender sender;

    @ClientSender(topic = "test")
    ClientMessageSender senderTopic;

    @AggregationKey
    public String byUuid(String event) {
        return uuid;
    }

    @OnTransit
    public void onNodeEvent(TestState state, String event) {
        state.message = event;
        sender.send(state);
        senderTopic.send(event);

        if (context == null) {
            throw new RuntimeException("Spring context cannot be null!");
        }
    }
}
