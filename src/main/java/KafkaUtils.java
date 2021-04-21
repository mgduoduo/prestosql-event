import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaUtils {


  public static void send(String value){
    //配置信息
    Properties props = new Properties();
    //kafka服务器地址
    props.put("bootstrap.servers", "datanode1:9092,datanode2:9092,datanode3:9092,datanode4:9092,datanode5:9092,datanode6:9092");
    //设置数据key和value的序列化处理类
    props.put("key.serializer", StringSerializer.class);
    props.put("value.serializer", StringSerializer.class);
    //创建生产者实例
    KafkaProducer<String,String> producer = new KafkaProducer<>(props);
    ProducerRecord record = new ProducerRecord<String, String>("plink_canal",  value);
    //发送记录
    producer.send(record);
    producer.close();
  }

  public static void main(String[] args) {
    send("ttt");
  }
}
