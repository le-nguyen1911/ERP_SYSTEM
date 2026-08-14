package com.ERP_SYSTEM.inventory.scheduler;

// Scheduled Job riêng, đặt trong 1 class mới nhỏ gọn:

import com.ERP_SYSTEM.inventory.dto.response.ProductStockResponse;
import com.ERP_SYSTEM.inventory.event.LowStockDetectedEvent;
import com.ERP_SYSTEM.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LowStockAlertScheduler {

    private final StockService stockService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 * * * *") // mỗi giờ đúng phút 0
    public void checkLowStockPeriodically() {
        List<ProductStockResponse> lowStocks = stockService.getLowStockAlerts();
        for (ProductStockResponse stock : lowStocks) {
            eventPublisher.publishEvent(new LowStockDetectedEvent(
                    stock.product().id(), stock.product().name(),
                    stock.warehouse().id(), stock.quantity(), stock.minQuantity()
            ));
        }
    }
}