package ru.yandex.qatools.terra.example;

import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.terra.api.ClientMessageSender;
import ru.yandex.qatools.terra.api.annotations.Aggregate;
import ru.yandex.qatools.terra.api.annotations.ClientSender;

import static ru.yandex.qatools.terra.example.SpringFacade.getContext;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate(clazz = Expressions.class, method = Expressions.BY_UUID)
@FSM(start = TestState.class)
@Transitions({@Transit(on = String.class), @Transit(stop = true, on = Float.class)})
public class TestAggregator {

    @ClientSender
    ClientMessageSender sender;

    @ClientSender(topic = "test")
    ClientMessageSender senderTopic;

    @OnTransit
    public void onNodeEvent(TestState state, String event) {
        state.message = event;
        sender.send(state);
        senderTopic.send(state);

        if (getContext() == null) {
            throw new RuntimeException("Spring context cannot be null!");
        } else {
            getContext().getBean(SpringFacade.class);
        }
    }
}
