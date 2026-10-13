package com.cashly.cashly_api.reports.inventory.application.ports.out;
import java.io.IOException;
public interface MediaGateway {
    byte[] load(String path) throws IOException;
}