package com.gmoon.cacheserializer.test;

import java.io.Serializable;

public record CachedItem(String sku, int qty) implements Serializable {
}
