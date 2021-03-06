package de.hpi.bpStormcrawler;

import io.latent.storm.rabbitmq.TupleToMessage;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.validation.ConfigValidationAnnotations;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/*
* A message Theme for the parsed Pages for the RabbitMQ
* @author jonaspohlmann
* */
@Getter (AccessLevel.PRIVATE)

public class TupleToMessageCrawler extends TupleToMessage {

     private final String exchangeName;
     private final String routingName;
     private transient Logger logger;

    TupleToMessageCrawler (String exchangeName, String routingName) {
        this.exchangeName = exchangeName;
        this.routingName = routingName;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**This method translates a tuple to the message which will be send to RabbitMQ
     * It builds the JSON Object by taking the tuple name as name for the field in JSON and its value as value
     * The value of each part of a tuple should be convertable to a string
     *
     * @param tuple the Tuple to translate
     * @return a serialized byte array representing a JSON Object as string
     */
    @Override
    protected byte[] extractBody(Tuple tuple) {

        if(tuple == null){
            getLogger().error("Tuple was NULL");
            return null;
        }

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
            for (String element : tuple.getFields()){
                builder.field(element,tuple.getValueByField(element));
            }
            builder.endObject();
            return builder.string().getBytes();
        } catch (IOException e) {
            getLogger().error("Could not convert tuple to a JSON String");
            return null;
        }
    }

    @Override
    protected String specifyContentType(Tuple input) {
        return "application/json";
    }

    @Override
    protected String determineExchangeName(Tuple tuple) {
        return getExchangeName();
    }

    @Override
    protected String determineRoutingKey(Tuple input) {
        return getRoutingName(); // rabbitmq java client library treats "" as no routing key
    }

    @Override
    protected boolean specifyMessagePersistence(Tuple input) {
        return true;
    }


}
