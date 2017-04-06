package com.advicetec.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.advicetec.core.MeasuredAttributeValue;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class MeasureAttributeValueStore {

	private static MeasureAttributeValueStore instance = null;
	Connection conn  = null; 
	PreparedStatement pst = null;

	static Cache<String, MeasuredAttributeValue> cache;
	PreparedStatement preparedStatement;

	protected MeasureAttributeValueStore(){
	}

	public static MeasureAttributeValueStore getInstance(){
		if(instance == null){
			instance = new MeasureAttributeValueStore();

			cache = Caffeine.newBuilder()
					.initialCapacity(100)
					.maximumSize(50)
					.recordStats()
					.build();
		}
		return instance;
	}

	public Cache<String, MeasuredAttributeValue> getCache(){
		return cache; 
	}



	public void cacheStore(MeasuredAttributeValue mav){
		cache.put(mav.getKey(), mav);
	}

	/**
	 * Given a key, it returns an element from the cache if it is there.
	 * If the value is not in the cache, this method returns NULL.
	 * @param key 
	 * @return
	 */
	public MeasuredAttributeValue getFromCache(String key){
		MeasuredAttributeValue fromCache = cache.getIfPresent(key);
		if(fromCache != null)
			return fromCache;
		return null;
	}

	public void commit(MeasuredAttributeValue value){
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/iotajover", "iotajover", "iotajover");
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(value.getPreparedInsertText());
			value.dbInsert(pst);
			pst.executeBatch();
			conn.commit();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			if(pst!=null)
			{
				try
				{
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if(conn!=null) 
			{
				try
				{
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
