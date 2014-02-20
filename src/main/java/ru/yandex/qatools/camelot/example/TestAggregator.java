package ru.yandex.qatools.camelot.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.ClientSender;
import ru.yandex.qatools.camelot.api.annotations.Filter;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = {String.class, Float.class})
@Aggregate(clazz = Expressions.class, method = Expressions.BY_UUID)
@FSM(start = TestState.class)
@Transitions({
        @Transit(on = String.class),
        @Transit(stop = true, on = Float.class)
})
public class TestAggregator {

    @Autowired
    ApplicationContext context;

    @ClientSender
    ClientMessageSender sender;

    @ClientSender(topic = "test")
    ClientMessageSender senderTopic;

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
