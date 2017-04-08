package com.advicetec;

import com.advicetec.monitorAdapter.AdapterManager;
import com.advicetec.persistence.StatusStore;

public class Init {

	public static void main(String[] args) {
		
		// caches initiation
		//		StatusStore.getInstance();
		
		AdapterManager adapterManager = AdapterManager.getInstance();
		Thread managerThread = new Thread(); 
	}

}
