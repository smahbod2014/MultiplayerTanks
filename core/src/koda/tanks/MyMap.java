package koda.tanks;

import java.util.ArrayList;

public class MyMap<K, V> {

	private ArrayList<Key> keys;
	private ArrayList<Value> values;
	
	public MyMap() {
		keys = new ArrayList<Key>();
		values = new ArrayList<Value>();
	}
	
	public void put(K k, V v) {
		Key key = new Key();
		key.key = k;
//		if (keys.contains(key))
//		map.add(new MapObj<K, V>(k, v));
	}
	
	private class Key<K> {
		K key;
		Value pointer;
		@Override
		public boolean equals(Object o) {
			Key k = (Key) o;
			return key.equals(k);
		}
	}
	
	private class Value<V> {
		V value;
	}
}
