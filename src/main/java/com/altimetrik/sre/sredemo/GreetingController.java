package com.altimetrik.sre.sredemo;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private final AtomicLong giftCounter = new AtomicLong(); // New gift counter
    private final AtomicLong fibCounter = new AtomicLong(); // New Fibonacci counter
    private long[] fibSequence = {0, 1}; // Initialize Fibonacci sequence

    private final MeterRegistry registry;

    /**
     * We inject the MeterRegistry into this class
     */
    public GreetingController(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * The @Timed annotation adds timing support, so we can see how long
     * it takes to execute in Prometheus
     * percentiles
     */
    @GetMapping("/greeting")
    @Timed(value = "greeting.time", description = "Time taken to return greeting",
            percentiles = {0.5, 0.90})
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        registry.counter("greetings.counter").increment();
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    /**
     * New POST method to handle gift requests
     */
    @PostMapping("/gift")
    @Timed(value = "gift.request.time", description = "Time taken to process gift request",
            percentiles = {0.5, 0.90})
    public GiftResponse sendGift(@RequestBody GiftRequest giftRequest) {
        // Process the gift request
        // For demonstration purposes, we'll just return a success message
        giftCounter.incrementAndGet(); // Increment the gift counter
        return new GiftResponse("Gift sent successfully!", giftCounter.get());
    }

    /**
     * New GET method to handle Fibonacci sequence
     */
    @GetMapping("/fib")
    @Timed(value = "fib.time", description = "Time taken to generate Fibonacci sequence",
            percentiles = {0.5, 0.90})
    public FibResponse generateFib() {
        // Generate next Fibonacci number
        long nextFib = fibSequence[fibSequence.length - 1] + fibSequence[fibSequence.length - 2];
        fibSequence = addElement(fibSequence, nextFib);
        fibCounter.incrementAndGet(); // Increment the Fibonacci counter
        return new FibResponse(fibCounter.get(), nextFib);
    }
    // Simulate Server error
    @GetMapping("/svrerr")
    @Timed(value = "svrerr.time", description = "Time taken to return server error",
            percentiles = {0.5, 0.90})
    public void serverError() {
        throw new RuntimeException("Simulated server error");
    }

    // Helper method to add an element to an array
    private long[] addElement(long[] originalArray, long newElement) {
        long[] newArray = new long[originalArray.length + 1];
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        newArray[newArray.length - 1] = newElement;
        return newArray;
    }

    // Define the GiftRequest class to hold the payload
    public static class GiftRequest {
        private String recipientName;
        private String giftName;

        // Getters and setters
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

    // Define the GiftResponse class to hold the response
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

    // Define the FibResponse class to hold the Fibonacci response
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