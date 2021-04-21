import io.prestosql.spi.eventlistener.EventListener;
import io.prestosql.spi.eventlistener.EventListenerFactory;

import java.util.Map;

public class QueryLoggerEventListenerFactory implements
  EventListenerFactory {
  public String getName() {
    return "event-logger";
  }

  public EventListener create(Map<String, String> config) {
    return new QueryLoggerEventListener(config);
  }
}
