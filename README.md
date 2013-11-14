# Terra embedded

Пример того, как можно использовать встроенную терру.

### 1. Создать новый Maven-проект. Можно воспользоваться архетипом terra-plugin:

```bash
mvn archetype:create -DarchetypeArtifactId=terra-plugin \
    -DarchetypeGroupId=ru.yandex.qatools \
    -DarchetypeVersion=1.1-SNAPSHOT \
    -DartifactId=test-plugin \
    -DgroupId=ru.yandex.qatools
```
### 2. Подключить terra-api:

```xml
        <dependency>
            <groupId>ru.yandex.qatools</groupId>
            <artifactId>terra-api</artifactId>
            <version>1.1</version>
        </dependency>
```

### 3. Спроектировать и реализовать свою доменную модель (в виде набора классов-событий и классов-состояний)

### 4. Реализовать аггрегатор:

```java
   @Aggregate(clazz = Strategy.class, method = "byUUID")
   @FSM(start = TestsCountByDayState.class)
   @Transitions({@Transit(from = TestsCountByDayState.class, on = TestEvent.class)})
   public class TestsCountByDayAggregator {
       @MainInput
       EventProducer main;

       @Input
       EventProducer self;

       @Output
       EventProducer out;

       @ClientSender
       ClientMessageSender client;

       @PluginStorage
       Storage pluginStorage;

       @Plugins
       PluginsInterop plugins;

       @Plugin("another-plugin-id")
       PluginInterop anotherPlugin;

       @OnTransit
       public void transit(TestsCountByDayState state, TestEvent event) {
           state.setCountByDay(state.getCountByDay() + 1);
           // ...
       }

       @OnTimer(cron = "0 */5 * * * ?")
       public void every5Minutes(TestsCountByDayState state) {
           //...
       }

       @OnClientMessage
       public void onClientMessage(TestsCountByDayState state, String message){
           //...
       }
   }

```

Здесь:

* `@Aggregate` - базовые настройки аггрегации
* `@FSM` и `@Transitions` - см. описание реализации стейт-машины в [yatomata](https://github.yandex-team.ru/qafw/yatomata)
* `@Output` - свойство типа EventProducer, позволяющее отправить любое сообщение в выходную очередь плагина
* `@Input` - свойство типа EventProducer, позволяющее отправить любое сообщение во входную очередь плагина (самому себе)
* `@MainInput` - свойство типа EventProducer, позволяющее отправить любое сообщение в главную входную очередь
* `@ClientSender` - свойство типа ClientMessageSender, позволяющее отправить сообщение клиентской части (через веб-сокет) плагина.
* `@PluginStorage` - свойство типа Storage, позволяющее плагину хранить произвольный набор данных (например, настройки).
* `@Plugin` - свойство типа PluginInterop, позволяющее плагину взаимодействовать с другим плагином (указывается ID другого плагина)
* `@Plugins` - свойство типа PluginsInterop, позволяющее плагину взаимодействовать с другими подключёнными плагинами (по ID)
* `@OnTimer` - метод, который обрабатывает события таймера по заданному свойству cron. Позволяет проверять или изменять состояние аггрегатора по событию
В качестве аргумента должен принимать текущее состояние.
* `@OnClientMessage` - метод, который обрабатывает события, поступающие от клиентской части (через веб-сокет) плагина.

**@Aggregate**:
Для аггрегации по конкретному значению, необходимо реализовать класс стратегии. Например:

```java
    public static class Strategy {

        public String byBodyField(@Body MyBody body){
            return body.getField();
        }

        public String byUUID(@Header("uuid") String uuid) {
            return uuid;
        }
    }

```
Чтобы использовать этот класс как кастомную стратегию, нужно указать в качестве атрибута `clazz` в аннотации @Aggregate
значение `Strategy.class`, а в качестве атрибута `method` нужно использовать либо `"byBodyField"` либо `"byUUID"`.

**@Split**:
Для правильной аггрегации, часто необходимо сперва разбить сообщения на группы, для этого можно использовать аннотацию `@Split`:

```java
   @Split(clazz = Splitter.class, method = "byChildren")
```
`clazz` и `method` в этом случае также определяют класс и метод сплиттера.
Метод-сплиттер должен возвращать список сообщений, которые в итоге поступят на вход аггрегатора:

```java
public class Splitter {
    public List<Child> byChildren(@Body Parent parent) {
        return parent.getChildren();
    }
}
```
В этом случае аггрегатор получит набор сообщений типа `Child`.

### 5. Реализовать ресурсный класс (для получения сообщений или отображения состояния в xml/json):

```java
    @Path("/count-by-day")
    public class TestsCountByDayResource {

        @Repository
        private AggregatorRepository aggregatorRepository;

        @MainInput
        EventProducer input;

        @GET
        @Path("{day}")
        @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
        public Response get(@PathParam("day") String day) {
            return Response.ok(aggregatorRepository.get(day)).build();
        }

        @PUT
        @Path("/events")
        @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public Response sendMessage(TestMessage message) {
            input.produce(TestMessage);
            return Response.ok("ok").build();
        }
    }
```

Здесь:

* `@Repository` - свойство типа `AggregatorRepository`. Данный интерфейс имеет 2 метода: keys() и get().
Метод `keys` возвращает список всех ключей аггрегации. Метод `get` возвращает текущее состояние (state) по ключу.

Также в ресурсном классе допустимо внедрение  `@Output`, `@MainInput`, `@Input`, `@ClientSender`, `@PluginStorage`, `@Plugin`, `@Plugins`

### 6. Вместо аггрегатора можно реализовать процессор (обработчик без состояния):

```java
    public class TestBrokenToStringProcessor {

        @Processor(bodyType = TestBroken.class)
        public String process(@Body TestBroken event) {
            return event.getClass().getName();
        }

        @FallbackProcessor
        public Object process(@Body Object event) {
            return event;
        }
    }
```

Здесь:

* `@Processor` - метод-обработчик событий определённого типа. Тип указывается при помощи свойства `bodyType`.
* `@FallbackProcessor` - метод-обработчик событий всех остальных типов (для которых не указан `@Processor`). Может быть только один.

### 7. Реализовать тест аггрегатора или процессора плагина:

Предположим, что у нас есть аггрегатор и процессор и мы хотим протестировать как они работают последовательно друг другу.

Сперва подключим terra-test к проекту (не нужно, если вы использовали архетип):

```xml
<dependency>
    <groupId>ru.yandex.qatools</groupId>
    <artifactId>terra-test</artifactId>
    <version>1.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

Теперь необходимо добавить конфигурацию плагинов в src/test/resources/plugins-config.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<plugins-config xmlns="urn:config.terra.qatools.yandex.ru">
    <source>
        <artifact>test</artifact>
        <plugin id="test-processor">
            <processor>ru.yandex.qatools.terra.test.TestProcessor</processor>
        </plugin>
        <plugin id="test-aggregator" source="test-processor">
            <aggregator>ru.yandex.qatools.terra.test.TestAggregator</aggregator>
        </plugin>
    </source>
</plugins-config>
```

Затем реализовать JUnit тест с использованием специального Runner'а и аннотаций для свойств:

```java
    @RunWith(TerraTestRunner.class)
    public class PluginTest {
        @PluginMock("test-aggregator")
        TestAggregator aggMock;

        @PluginMock("test-processor")
        TestProcessor prcMock;

        @Helper
        TestHelper helper;

        @AggregatorState("test-aggregator")
        AggregatorStateStorage aggStates;

        @Test
        public void testRoute() throws Exception {
            helper.send("test", UUID, "uuid");
            verify(prcMock, timeout(3000)).onNodeEvent(eq("test"));
            verify(aggMock, timeout(3000)).onNodeEvent(any(TestState.class), eq("test-processed"));
            TestState state = aggStates.get(TestState.class, "uuid");
            assertNotNull(state);
            assertEquals("test-processed", state.message);
        }
    }
```

Здесь:

* `@PluginMock` - свойство, позволяющее через API Mockito проверить реальные вызовы методов на вашем аггрегаторе/процессоре.
Должно иметь соответсвующий тип, а значение атрибута value в аннотации должно быть равно идентификатору плагина.
* `@Helper` - свойство типа TestHelper, который имеет ряд дополнительных методов. В частности метод send(), позволяющий отправить сообщения
во входную очередь
* `@AggregatorState` - свойство типа AggregatorStateStorage - позволяет проверить состояние аггрегатора после получения сообщений.
Атрибут value соответствует идентификатору плагина, указанному в конфигурации.

### 8. Реализовать презентативную часть плагина (виджет и дашборд).

Для этого необходимо создать в директории src/main/resources/{полный путь к классу с ресурсом/аггрегатором/процессором в плагине}/{имя класса-ресурса/аггрегатора/процессора}/ следующий
набор файлов:

* dashboard.html (Можно также использовать шаблонизатор .jade и .mustache) - шаблон дашборда (страницы с информацией) плагина.
* widget.html (Можно также использовать .jade и .mustache) - шаблон виджета (который появится на дашборде терры).
* Любые файлы с расширением .js (Можно также использовать .coffee) - скрипт-файлы, подключаемые к странице терры.
* styles.css (Можно также испольовать .less) - файл со стилями. Допускается использовать @import для ссылки на другие файлы со стилями,
а также ссылки на различные ресурсы внутри данной директории (например, картинки) через url(путь_относительно_текущей_директории).

Ресурсные файлы должны лежать в директории с именем класса-ресурса, аггрегатора или процессора (в зависимости от типа плагина).
Например, если класс-аггрегатор называется ru.yandex.qatools.TestAggregator, то ресурсы можно положить в директорию src/main/resources/ru/yandex/qatools/TestAggregator

В скрипт-файле можно либо дёргать ручки плагина, используя в качесте базового URL глобальное значение `window.PluginsSystem.contextPath`, например:

```javascript
    $(function () {
        $.ajax({
            url: window.PluginsSystem.contextPath + '/путь-в-ресурсном-классе',
            // ...
        });
    });

```

Можно подписаться на websocket сервера. Для этого сперва необходимо выяснить идентификатор плагина в данной
конфигурации, например, с помощью следующего кода в jade-шаблоне:

```jade
.plugin-name(id="#{id}")
```

Теперь можно использовать этот id и подписаться на серверные события таким образом:
```javascript
    $(function () {
        var pluginId = $(".plugin-name").attr('id');
        window.PluginsSystem.clientReciever.subscribe(pluginId, function (res) {
            // ...
        });
    });
```

В шаблонах допустимо использование следующих переменных:
* `repo` - переменная типа AggregatorRepository, позволяет получить доступ к репозиторию аггрегации.
* `storage` - переменная типа Storage, позволяет получить доступ к репозиторию с настройками плагина.
* `id` - идентификатор плагина (String).
* `plugins` - переменная типа PluginsInterop, позволяет получить доступ к другим плагинам.
* `contextPath` - базовый контекст выполнения приложения (String).
* `helper` - переменная типа ViewHelper. Позволяет, например, отрисовать дополнительные шаблоны.

```jade
!= helper.render("another.jade", {"text": "Hello, World!"})
```

### 9. Использование terra-test-maven-plugin

При разработке плагина удобно использовать специальный Maven-плагин, запускающий сервер с конфигурацией, описанной в plugins-config.xml.
Для этого нужно подключить к проекту с плагином следующий Maven-плагин:

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>ru.yandex.qatools</groupId>
                <artifactId>terra-test-maven-plugin</artifactId>
                <version>1.1-SNAPSHOT</version>
                <configuration>
                    <runForked>true</runForked>
                    <jettyPort>8080</jettyPort>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

После этого в директории с плагином можно выполнить:

```bash
  mvn terra-test:run
```

Это запустит jetty с полным набором плагинов, перечисленным в plugins-config.xml, что позволит намного удобнее
разрабатывать презентативную часть плагина, используя страницу http://localhost:8080/terra-http. Сервер автоматически
подхватывает изменения в презентативной части плагина (достаточно сделать refresh).
