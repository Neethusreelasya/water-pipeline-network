package core;

import model.Pipeline;
import java.util.*;

public class WaterNetworkGraph {
    private Map<String, List<Pipeline>> adjList = new HashMap<>();

    public void addNode(String id) {
        adjList.putIfAbsent(id, new ArrayList<>());
    }

    public void addPipeline(Pipeline p) {
        addNode(p.getFromId());
        addNode(p.getToId());
        adjList.get(p.getFromId()).add(p);
    }

    public List<Pipeline> getNeighbors(String id) {
        List<Pipeline> result = new ArrayList<>();
        for (Pipeline p : adjList.getOrDefault(id, new ArrayList<>())) {
            if (p.isActive()) result.add(p);
        }
        return result;
    }

    // BFS: find areas unreachable from the tank
    public List<String> findDisconnectedAreas(String sourceTankId, List<String> allAreaIds) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(sourceTankId);
        visited.add(sourceTankId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Pipeline p : getNeighbors(current)) {
                if (!visited.contains(p.getToId())) {
                    visited.add(p.getToId());
                    queue.add(p.getToId());
                }
            }
        }

        List<String> disconnected = new ArrayList<>();
        for (String areaId : allAreaIds) {
            if (!visited.contains(areaId)) disconnected.add(areaId);
        }
        return disconnected;
    }

    // Dijkstra: shortest weighted path
    public List<String> findShortestRoute(String sourceId, String targetId) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq =
            new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

        for (String node : adjList.keySet()) dist.put(node, Double.MAX_VALUE);
        dist.put(sourceId, 0.0);
        pq.add(Map.entry(sourceId, 0.0));

        while (!pq.isEmpty()) {
            String current = pq.poll().getKey();
            if (current.equals(targetId)) break;

            for (Pipeline p : getNeighbors(current)) {
                double newDist = dist.get(current) + p.getWeight();
                if (newDist < dist.getOrDefault(p.getToId(), Double.MAX_VALUE)) {
                    dist.put(p.getToId(), newDist);
                    prev.put(p.getToId(), current);
                    pq.add(Map.entry(p.getToId(), newDist));
                }
            }
        }

        if (!dist.containsKey(targetId) || dist.get(targetId) == Double.MAX_VALUE) {
            return new ArrayList<>(); // unreachable
        }

        List<String> path = new LinkedList<>();
        String step = targetId;
        while (step != null) {
            path.add(0, step);
            step = prev.get(step);
        }
        return path;
    }

    // Find alternative route by disabling a failed pipeline temporarily
    public List<String> findAlternativeRoute(String sourceId, String targetId, String failedFromId, String failedToId) {
        Pipeline failed = null;
        for (Pipeline p : adjList.getOrDefault(failedFromId, new ArrayList<>())) {
            if (p.getToId().equals(failedToId)) { failed = p; break; }
        }
        if (failed != null) failed.markFailed();
        List<String> altPath = findShortestRoute(sourceId, targetId);
        if (failed != null) failed.markActive();
        return altPath;
    }
}