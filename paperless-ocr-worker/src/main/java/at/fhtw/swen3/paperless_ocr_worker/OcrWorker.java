package at.fhtw.swen3.paperless_ocr_worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OcrWorker {
    private static final Logger logger = LoggerFactory.getLogger(OcrWorker.class);

    @RabbitListener(queuesToDeclare = @Queue(value = "${paperless.ocr.queue}", durable = "true"))
    public void process(Message message) {
        var properties = message.getMessageProperties();
        logger.debug("OCR message received: document {}, message {}, redelivered={}",
                properties.getHeaders().get("documentId"), properties.getMessageId(),
                properties.isRedelivered());
        // Sprint 3 placeholder: receive the file, but do not run OCR yet.
        logger.info("Empty OCR worker processed document {} ({}, {} bytes, type {})",
                properties.getHeaders().get("documentId"),
                properties.getHeaders().get("filename"),
                message.getBody().length,
                properties.getContentType());
        // Spring acknowledges the message after this method returns successfully.
    }
}
