package com.altimetrik.sre.sredemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private final AtomicLong giftCounter = new AtomicLong(); 
    private final AtomicLong fibCounter = new AtomicLong(); 
    private long[] fibSequence = {0, 1}; 

    private final MeterRegistry registry;

    public GreetingController(MeterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/greeting")
    @Timed(value = "greeting.time", description = "Time taken to return greeting",
            percentiles = {0.5, 0.90})
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, Object> logDetails = new HashMap<>();
        logDetails.put("action", "greeting_request");
        logDetails.put("name", name);

        registry.counter("greetings.counter").increment();
        Greeting greeting = new Greeting(counter.incrementAndGet(), String.format(template, name));

        logDetails.put("greeting", greeting);

        logAsJson("Greeting request processed", logDetails);
        return greeting;
    }

    @PostMapping("/gift")
    @Timed(value = "gift.request.time", description = "Time taken to process gift request",
            percentiles = {0.5, 0.90})
    public GiftResponse sendGift(@RequestBody GiftRequest giftRequest) {
        Map<String, Object> logDetails = new HashMap<>();
        logDetails.put("action", "send_gift");
        logDetails.put("giftRequest", giftRequest);

        giftCounter.incrementAndGet();
        GiftResponse response = new GiftResponse("Gift sent successfully!", giftCounter.get());

        logDetails.put("response", response);
        logAsJson("Gift request processed", logDetails);
        return response;
    }

    @GetMapping("/fib")
    @Timed(value = "fib.time", description = "Time taken to generate Fibonacci sequence",
            percentiles = {0.5, 0.90})
    public FibResponse generateFib() {
        long nextFib = fibSequence[fibSequence.length - 1] + fibSequence[fibSequence.length - 2];
        fibSequence = addElement(fibSequence, nextFib);
        fibCounter.incrementAndGet();

        FibResponse response = new FibResponse(fibCounter.get(), nextFib);
        Map<String, Object> logDetails = new HashMap<>();
        logDetails.put("action", "generate_fib");
        logDetails.put("response", response);

        logAsJson("Fibonacci sequence updated", logDetails);
        return response;
    }

    @GetMapping("/svrerr")
    @Timed(value = "svrerr.time", description = "Time taken to return server error",
            percentiles = {0.5, 0.90})
    public void serverError() {
        Map<String, Object> logDetails = new HashMap<>();
        logDetails.put("action", "simulate_server_error");
        logAsJson("Simulating server error", logDetails);
        throw new RuntimeException("Simulated server error");
    }

    private long[] addElement(long[] originalArray, long newElement) {
        long[] newArray = new long[originalArray.length + 1];
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        newArray[newArray.length - 1] = newElement;
        return newArray;
    }

    private void logAsJson(String message, Map<String, Object> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            logger.info("{} - {}", message, json);
        } catch (JsonProcessingException e) {
            logger.error("Error logging JSON", e);
        }
    }

    public static class GiftRequest {
        private String recipientName;
        private String giftName;

        public String getRecipientName() {
            return recipientName;
        }

        public void setRecipientName(String recipientName) {
            this.recipientName = recipientName;
        }

        public String getGiftName() {
            return giftName;
        }

        public void setGiftName(String giftName) {
            this.giftName = giftName;
        }
    }

    public static class GiftResponse {
        private String message;
        private long giftCount;

        public GiftResponse(String message, long giftCount) {
            this.message = message;
            this.giftCount = giftCount;
        }

        public String getMessage() {
            return message;
        }

        public long getGiftCount() {
            return giftCount;
        }
    }

    public static class FibResponse {
        private long fibCount;
        private long fibValue;

        public FibResponse(long fibCount, long fibValue) {
            this.fibCount = fibCount;
            this.fibValue = fibValue;
        }

        public long getFibCount() {
            return fibCount;
        }

        public long getFibValue() {
            return fibValue;
        }
    }
}
