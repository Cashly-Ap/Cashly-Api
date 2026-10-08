package com.cashly.cashly_api.reports.inventory.application.contracts;
import java.io.IOException;
public interface MediaGateway {
    byte[] load(String path) throws IOException;
}