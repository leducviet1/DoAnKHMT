package com.example.librarymanage_be.utils;

import java.util.Optional;

public class EntityUtils {
    public static <T> T getOrThrow(Optional<T> optional, String message) {
        return optional.orElseThrow(() -> new RuntimeException(message));
    }
}
