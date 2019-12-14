package util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class SequentialMap<K,V> extends HashMap<K,V> {
	int ticket_ = 0;
	TreeSet<K> orderedkeyset_ = null;
	HashMap<K,Integer> order_ = new HashMap<K,Integer>();
	public SequentialMap() {
		super();
		orderedkeyset_ = new TreeSet<K>(new Comparator<K>(){
			@Override
			public int compare(K o1, K o2) {
				return Integer.compare(order_.getOrDefault(o1, 0), order_.getOrDefault(o2, 0));
			}
		});
	}
	@Override
	public Set<K> keySet() {
		return this.orderedkeyset_;
	}
	@Override
	public V put(K key, V val) {
		order_.put(key, ticket_++);
		this.orderedkeyset_.add(key);
		return super.put(key,val);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 4486933409072720034L;

}
