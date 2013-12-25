package ru.yandex.qatools.terra.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.terra.api.ClientMessageSender;
import ru.yandex.qatools.terra.test.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.terra.api.Constants.Headers.UUID;

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

    @Test
    public void testRoute() throws Exception {
        helper.send("test", UUID, "uuid");
        verify(prcMock, timeout(3000)).onNodeEvent(eq("test"));
        verify(aggMock, timeout(3000)).onNodeEvent(any(TestState.class), eq("testprocessed"));
        verify(sender, timeout(3000)).send(any(TestState.class));
        verify(senderTopic, timeout(3000)).send(eq("testprocessed"));
        TestState state = aggStates.get(TestState.class, "uuid");
        assertNotNull(state);
        assertEquals("testprocessed", state.message);
    }
}
