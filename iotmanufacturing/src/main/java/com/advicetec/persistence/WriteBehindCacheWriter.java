package com.advicetec.persistence;

import java.util.ArrayList;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

/**
 * This class writes into cache and inserts elements into the Queue.
 * It has two lists, one for storing inserts and another for deletes.
 *  
 * @author user
 *
 */
public class WriteBehindCacheWriter<K,V>
{

	Cache<K, V> cache;


	public WriteBehindCacheWriter(ArrayList<V> inserts, ArrayList<V> deletes) 
	{
		super();

		cache = Caffeine.newBuilder()
				.initialCapacity(100)
				.maximumSize(50)
				.recordStats()
				.build( );
	}

}