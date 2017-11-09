package edu.gvsu.cis.cis656.clock;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class VectorClock implements Clock {

    private Map<String, Integer> clock = new Hashtable<String, Integer>();

    @Override
    public void update(Clock other) {
        for (Map.Entry<String, Integer> entry : clock.entrySet()) {
            try {
                Integer otherTime = other.getTime(Integer.parseInt(entry.getKey()));
                clock.put(entry.getKey(), Math.max(entry.getValue(), otherTime));
            } catch (Exception e) {
                // other.getTime could go wrong, this is lazy.
            }
        }

        // See if key in other are not in clock, add them
        JSONObject otherClockAsJson = new JSONObject(other.toString());
        Map<String, Object> otherClockMap = otherClockAsJson.toMap();
        for (Map.Entry<String, Object> entry : otherClockMap.entrySet()) {
            if (!clock.containsKey(entry.getKey())) {
                clock.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
            }
        }
    }

    @Override
    public void setClock(Clock other) {
        setClockFromString(other.toString());
    }

    @Override
    public void tick(Integer pid) {
        Integer currentTime;
        if (clock.containsKey(pid.toString())) {
            currentTime = clock.get(pid.toString());
            currentTime += 1;
            clock.put(pid.toString(), currentTime);
        }
    }

    @Override
    public boolean happenedBefore(Clock other) {
        JSONObject clockAsJson = new JSONObject(other.toString());
        Map<String, Object> newJsonClock = clockAsJson.toMap();
        for (Map.Entry<String, Object> entry : newJsonClock.entrySet()) {
            if (this.clock.containsKey(entry.getKey())) {
                if (Integer.parseInt(entry.getValue().toString()) < this.clock.get(entry.getKey())) {
                    return false;
                }
            }
        }
        return true;
    }

    public String toString() {
        JSONObject clockAsJson = new JSONObject(clock);
        return clockAsJson.toString();
    }

    @Override
    public void setClockFromString(String stringClock) {

        JSONObject clockAsJson = new JSONObject(stringClock);
        Map<String, Object> newJsonClock = clockAsJson.toMap();
        Map<String, Integer> newClock = new HashMap<>();

        try {
            for (Map.Entry<String, Object> entry : newJsonClock.entrySet()) {
                newClock.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
            }
            clock = newClock;

        } catch (NumberFormatException e) {
            // Do not set the clock if any number conversions went awry
        }
    }

    @Override
    public int getTime(int p) {
        int currentTime = 0;
        String pid = Integer.toString(p);
        if (clock.containsKey(pid)) {
            currentTime = clock.get(pid);
        }
        return currentTime;
    }

    @Override
    public void addProcess(int p, int c) {
        String pid = Integer.toString(p);
        if (!clock.containsKey(pid)) {
            clock.put(pid, c);
        }
    }
}
