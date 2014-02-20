package ru.yandex.qatools.camelot.example;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.EndpointListener;
import ru.yandex.qatools.camelot.test.*;
import ru.yandex.qatools.clay.annotations.Body;
import ru.yandex.qatools.clay.annotations.Headers;

import java.util.Map;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.TimeoutWaiter.timeoutHasExpired;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@UseCustomContext("classpath:/META-INF/spring/camelot-embedded.xml")
public class TestAggregatorTest {

    @PluginMock(id = "test-plugin")
    TestAggregator aggMock;

    @PluginMock(id = "test-processor")
    TestProcessor prcMock;

    @Helper
    TestHelper helper;

    @AggregatorState(id = "test-plugin")
    AggregatorStateStorage aggStates;

    @ClientSenderMock(id = "test-plugin")
    ClientMessageSender sender;

    @ClientSenderMock(id = "test-plugin", topic = "test")
    ClientMessageSender senderTopic;

    @PluginOutputListener(id = "test-processor")
    EndpointListener procListener;

    @Before
    public void init() {
        helper.send("test", UUID, "uuid");
    }

    @Test
    public void testRoute() throws Exception {
        verify(prcMock, timeout(3000)).onNodeEvent(eq("test"));
        verify(aggMock, timeout(3000)).onNodeEvent(any(TestState.class), eq("testprocessed"));
        verify(sender, timeout(3000)).send(any(TestState.class));
        verify(senderTopic, timeout(3000)).send(eq("testprocessed"));

        assertThat("testprocessed must be in state", aggStates.get(TestState.class, "uuid"), should(having(
                on(TestState.class).getMessage(), equalTo("testprocessed")
        )).whileWaitingUntil(timeoutHasExpired(3000)));
    }


    @Test
    public void testEndpointListener() throws Exception {
        final Value<Boolean> success = new Value<>(false);
        procListener.listen(5, SECONDS, new EndpointListener.Processor<String>() {
            @Override
            public boolean onMessage(@Body String event, @Headers Map<String, Object> headers) {
                success.set(success.get() || event.equals("testprocessed"));
                return success.get();
            }
        });
        assertTrue(success.get());
    }
}
