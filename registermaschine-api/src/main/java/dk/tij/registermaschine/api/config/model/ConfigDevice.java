package dk.tij.registermaschine.api.config.model;

import java.util.List;

public record ConfigDevice(String deviceHandler, long size, List<ConfigMemoryMapping> mappings) {}
