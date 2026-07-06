package com.efh.parts.task;

import com.efh.parts.service.PartsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutTask {

    @Autowired
    private PartsOrderService partsOrderService;

    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        partsOrderService.cancelTimeoutOrders();
    }
}
