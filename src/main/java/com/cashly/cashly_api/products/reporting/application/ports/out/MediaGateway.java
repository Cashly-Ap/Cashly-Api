package com.cashly.cashly_api.products.reporting.application.ports.out;
import java.io.IOException;
public interface MediaGateway {
    byte[] load(String path) throws IOException;
}