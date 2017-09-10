package com.advicetec.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapUtils {

	static Logger logger = LogManager.getLogger(MapUtilsTest.class.getName());
	
	public static <K, V> Collector<Map.Entry<K, V>, ?, List<Map<K, V>>> mapSize(int limit) {
	    return Collector.of(ArrayList::new,
	            (l, e) -> {
	                if (l.isEmpty() || l.get(l.size() - 1).size() == limit) {
	                    l.add(new HashMap<>());
	                }
	                l.get(l.size() - 1).put(e.getKey(), e.getValue());
	            },
	            (l1, l2) -> {
	                if (l1.isEmpty()) {
	                    return l2;
	                }
	                if (l2.isEmpty()) {
	                    return l1;
	                }
	                if (l1.get(l1.size() - 1).size() < limit) {
	                    Map<K, V> map = l1.get(l1.size() - 1);
	                    ListIterator<Map<K, V>> mapsIte = l2.listIterator(l2.size());
	                    while (mapsIte.hasPrevious() && map.size() < limit) {
	                        Iterator<Map.Entry<K, V>> ite = mapsIte.previous().entrySet().iterator();
	                        while (ite.hasNext() && map.size() < limit) {
	                            Map.Entry<K, V> entry = ite.next();
	                            map.put(entry.getKey(), entry.getValue());
	                            ite.remove();
	                        }
	                        if (!ite.hasNext()) {
	                            mapsIte.remove();
	                        }
	                    }
	                }
	                l1.addAll(l2);
	                return l1;
	            }
	    );
	}
	
}
