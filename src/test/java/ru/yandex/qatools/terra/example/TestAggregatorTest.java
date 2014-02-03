package ru.yandex.qatools.terra.example;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.clay.annotations.Body;
import ru.yandex.qatools.clay.annotations.Headers;
import ru.yandex.qatools.terra.api.ClientMessageSender;
import ru.yandex.qatools.terra.api.EndpointListener;
import ru.yandex.qatools.terra.test.*;

import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.TimeoutWaiter.timeoutHasExpired;
import static ru.yandex.qatools.terra.api.Constants.Headers.UUID;
import static ru.yandex.qatools.terra.test.Matchers.beNotNullByKey;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(TerraTestRunner.class)
@UseCustomContext("classpath:/META-INF/spring/embedded-terra.xml")
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
    public void init(){
        helper.send("test", UUID, "uuid");
    }

    @Test
    public void testRoute() throws Exception {
        verify(prcMock, timeout(3000)).onNodeEvent(eq("test"));
        verify(aggMock, timeout(3000)).onNodeEvent(any(TestState.class), eq("testprocessed"));
        verify(sender, timeout(3000)).send(any(TestState.class));
        verify(senderTopic, timeout(3000)).send(eq("testprocessed"));

        // waiting until state is saved into hazelcast instance
        assertThat(aggStates, should(beNotNullByKey(aggStates, "uuid"))
                .whileWaitingUntil(timeoutHasExpired(SECONDS.toMillis(5))));

        TestState state = aggStates.get(TestState.class, "uuid");
        assertNotNull(state);
        assertEquals("testprocessed", state.message);
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
