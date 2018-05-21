/**
 * @author 		gaofan_kd7331
 * @email		romy_schneider@163.com
 * @date 		2015-10-13 18:32:42
 * @function	Object pool  
 */

package com.sissi.droidw.utils;

import java.util.ArrayList;
import java.util.Iterator;


public class KDObjectPool {     
    private static final int MAX_OBJ_NUM = 50;
    private ArrayList<Object> objects = null;
    private	boolean	isGrowMode = false; 	//If true, create new ones when there is no more object in the pool. The object type used when do the creation is getting from the field `cls'.
    private Class<?> cls = null;	// The type of the pooled object
  
    /**
     * Create an empty pool with the default pooled-object type Object.
     */
    public KDObjectPool() {
    	objects = new ArrayList<Object>();
    	cls = Object.class;
    	isGrowMode = false;
    }     
    
    /**
     * Create an empty pool with the given pooled-object type.
     */
    public KDObjectPool(Class<?> cls) {
    	objects = new ArrayList<Object>();
        this.cls = cls;
        isGrowMode = false;
    }    
    
    /**
     * Create a given-size pool with the given pooled-object type.
     */
    public KDObjectPool(Class<?> cls, int num) {
    	objects = new ArrayList<Object>();
    	
    	Object obj=null;
    	int actNum = num < MAX_OBJ_NUM ? num : MAX_OBJ_NUM;
        for (int i = 0; i < actNum; ++i) {   
        	try {
				obj = cls.newInstance();  // Create the object by its DEFAULT constructor!
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			objects.add(new PooledObject(obj));    
        }
        
        this.cls = cls;
        isGrowMode = false;
    }
      
    public synchronized void addObject(Object obj){
        if (objects == null) {     
            return;    
        } 
    	objects.add(new PooledObject(obj));
    }
    
    public synchronized void delObject(Object obj){
        if (objects == null) {     
            return;    
        } 
                
        PooledObject po = null;
        Iterator<Object> iter = objects.iterator(); 
        while (iter.hasNext()) { 
        	po = (PooledObject)iter.next();
	        if (po.getObject() == obj) { 
            	if (po.isInUse()) {     
            		// TODO
            	}
	        	iter.remove(); 
	        	break;
	        } 
        }
    }
    
    public synchronized void delAllObject(){
        if (objects == null) {     
            return;    
        } 
        
        objects.clear();
    }
    
    public synchronized Object getObject(){ 
        if (objects == null) {     
            return null;    
        }     
    
        Object obj = null;
        PooledObject po = findFreePoolObject(); 
        if (po != null){
        	obj = po.getObject();
        }else{
        	if (isGrowMode && objects.size() < MAX_OBJ_NUM){
                try {
    				obj = cls.newInstance();
    			} catch (InstantiationException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}catch (IllegalAccessException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                po = new PooledObject(obj);
                po.setIsInUse(true);
                objects.add(po);
        	}
        }  
    
        return obj; 
    }
       
    public synchronized void returnObject(Object obj) {     
        if (objects == null) {     
            return;     
        }     
        
        PooledObject po = null;
        Iterator<Object> iter = objects.iterator(); 
        while (iter.hasNext()) { 
        	po = (PooledObject)iter.next();
	        if (po.getObject() == obj) { 
	        	po.setIsInUse(false);
	        	break;
	        } 
        }
    }
            
    private PooledObject findFreePoolObject(){  
        PooledObject po = null;
        Iterator<Object> iter = objects.iterator(); 
        while (iter.hasNext()) { 
        	po = (PooledObject)iter.next();
    		if (!po.isInUse()) {
    			po.setIsInUse(true);  
    			return po;
    		}
        }
        
        return null;
    }
    
    
    public synchronized int size() {
    	return objects.size();
    }
    
    public synchronized boolean isGrowMode() {
    	return isGrowMode;
    }
    
    public synchronized	void setIsGrowMode(boolean isGrowMode){
    	this.isGrowMode = isGrowMode;
    }
    
   
    class PooledObject {     
    
        private Object obj;	 
        private boolean isInUse;
    
        public PooledObject(Object obj) {     
    
            this.obj = obj;     
            isInUse = false;
        }     

        public Object getObject() {     
            return obj;     
        }     
    
        public void setObject(Object obj) {     
            this.obj = obj;     
        }     
    
        public boolean isInUse() {     
            return isInUse;     
        }     
    
        public void setIsInUse(boolean isInUse) {     
            this.isInUse = isInUse;     
        }     
    }     
}    
