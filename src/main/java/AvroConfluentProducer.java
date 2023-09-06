import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class AvroConfluentProducer {
    public AvroConfluentProducer() throws IOException {
        Properties props = loadConfig("/Users/yassine/kafka/config/client.properties");

        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);

        String schemaString =
                "{\"namespace\": \"customer.avro\"," +
                        "\"type\": \"record\", " +
                        "\"name\": \"Customer\"," +
                        "\"fields\": [" +
                        "{\"name\": \"id\", \"type\": \"int\"}," +
                        "{\"name\": \"first_name\", \"type\": \"string\"}," +
                        "{\"name\": \"last_name\", \"type\": \"string\"}," +
                        "{\"name\": \"age\", \"type\": \"int\"}," +
                        "{\"name\": \"gender\", \"type\": \"string\"}," +
                        "{\"name\": \"salary\", \"type\": \"double\"}" +
                        "]}";

        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(schemaString);

        String topic = "customers";

        for (int i = 0; i < 1000000; i++) {
            Customer customer = new Customer(i,
                    "first_name_"+i,
                    "last_name_"+i,
                    (int)new Random().nextGaussian()*(60-18+1)+18,
                    (new Random().nextBoolean()) ?'F':'M',
                    (new Random().nextDouble()) * (50000 - 2600 + 1) + 2600
            );

            String key = UUID.randomUUID().toString();

            GenericRecord genericRecord = new GenericData.Record(schema);
            genericRecord.put("id", customer.getId());
            genericRecord.put("first_name", customer.getFirstName());
            genericRecord.put("last_name", customer.getLastName());
            genericRecord.put("age", customer.getAge());
            genericRecord.put("gender", customer.getGender() + "");
            genericRecord.put("salary", customer.getSalary());

            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, key, genericRecord);
            producer.send(record);
        }

    }

    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }

    public static void main(String[] args) throws IOException {
        new AvroConfluentProducer();
    }
}
