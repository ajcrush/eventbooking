package com.eventbooking.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class RazorpayService {

    private RazorpayClient client;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String secretKey;

    @PostConstruct
    public void init() throws Exception {
        this.client = new RazorpayClient(keyId, secretKey);
    }

    public Order createOrder(int amountInPaise) throws Exception {
        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("payment_capture", 1);
        return client.orders.create(options);
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String data = orderId + "|" + paymentId;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);

            byte[] hash = sha256_HMAC.doFinal(data.getBytes());
            String generatedSignature = Base64.getEncoder().encodeToString(hash);

            return generatedSignature.equals(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
