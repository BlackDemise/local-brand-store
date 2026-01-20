package wandererpi.lbs.service;

import wandererpi.lbs.dto.request.SepayWebhookRequest;

public interface PaymentService {
    void processSepayWebhook(SepayWebhookRequest request, String signature);
}
