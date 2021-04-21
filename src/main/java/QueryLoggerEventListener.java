import io.prestosql.spi.eventlistener.EventListener;
import io.prestosql.spi.eventlistener.QueryCompletedEvent;
import io.prestosql.spi.eventlistener.QueryCreatedEvent;
import io.prestosql.spi.eventlistener.SplitCompletedEvent;
import net.sf.jsqlparser.statement.Statement;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.JSQLParserException;

public class QueryLoggerEventListener implements EventListener {

  public QueryLoggerEventListener(Map<String, String> config){}




  /**
   * 监听创建查询事件
   */
  @Override
  public void queryCreated(QueryCreatedEvent queryCreatedEvent) {
    String queryId = queryCreatedEvent.getMetadata().getQueryId();
    String query = queryCreatedEvent.getMetadata().getQuery();
    String user = queryCreatedEvent.getContext().getUser();
//    KafkaUtils.send("queryCreated::"+queryId+", "+query+", "+user);

  }
  /**
   * 监听查询完成事件
   */
  @Override
  public void queryCompleted(QueryCompletedEvent queryCompletedEvent) {
    String queryId = queryCompletedEvent.getMetadata().getQueryId();
    String queryUser = queryCompletedEvent.getContext().getUser();
    String querySql = queryCompletedEvent.getMetadata().getQuery();

    queryCompletedEvent.getFailureInfo().ifPresent(queryFailureInfo -> {
      int errCode = queryFailureInfo.getErrorCode().getCode();
      String failureType = queryFailureInfo.getFailureType().orElse("").toUpperCase();
      String failureHost = queryFailureInfo.getFailureHost().orElse("");
      String failureMessage = queryFailureInfo.getFailureMessage().orElse("");
    });
    KafkaUtils.send("queryCompleted::"+queryId+", "+queryCompletedEvent.getMetadata().getTables()+", "+querySql);


    //以下为解析sql的基础模型  未完成 发送到kafka 做血缘
    SqlParserHandler sqlParserHandler = new SqlParserHandler();

    Statement statement = null;
    try {
      statement = sqlParserHandler.getStatement(querySql);
    } catch (JSQLParserException e) {
      e.printStackTrace();
    }
    List<String> tableList = sqlParserHandler.getTableList(statement);
    String sqlType = null;
    try {
      sqlType = sqlParserHandler.getSqlType(querySql);
    } catch (JSQLParserException e) {
      e.printStackTrace();
    }
    System.out.println(sqlType);
    for (String s : tableList) {
      System.out.println(s);
    }



  }

  /**
   * 监听split完成事件
   */
  @Override
  public void splitCompleted(SplitCompletedEvent splitCompletedEvent) {
    long createTime = splitCompletedEvent.getCreateTime().toEpochMilli();
    long endTime = splitCompletedEvent.getEndTime().orElse(Instant.MAX).toEpochMilli();
    String queryId = splitCompletedEvent.getQueryId();
    String stageId = splitCompletedEvent.getStageId();
    String taskId = splitCompletedEvent.getTaskId();
//    KafkaUtils.send("queryCompleted::"+queryId+", "+createTime+", "+taskId);


  }

}
