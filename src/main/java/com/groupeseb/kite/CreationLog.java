package com.groupeseb.kite;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class CreationLog {
    private final Map<String, String> uuids = new HashMap<>();
    private final Map<String, String> locations = new HashMap<>();

    public void extend(CreationLog creationLog) {
        this.uuids.putAll(creationLog.uuids);
        this.locations.putAll(creationLog.locations);
    }

    public Map<String, String> getUUIDs() {
        return uuids;
    }

    public Map<String, String> getLocations() {
        return locations;
    }

    public void addLocation(String name, String location) {
        locations.put(name, location);
    }

    public void addUUIDs(Map<String, String> uuids) {
        this.uuids.putAll(uuids);
    }
}
