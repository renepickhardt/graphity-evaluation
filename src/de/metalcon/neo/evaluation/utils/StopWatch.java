/**
 * Accurate stopwatch for time measurements
 * 
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation.utils;

import java.util.ArrayList;
import java.util.LinkedList;

public class StopWatch {
	ArrayList<Long> processedHistory = new ArrayList<Long>();
	ArrayList<Long> updateTimeHistory = new ArrayList<Long>();
	double lastUpdateRate = 0;
	LinkedList<Long> stopedtimes = new LinkedList<Long>();

	long startTime = System.nanoTime();
	
	public StopWatch(){
		updateRate(0);
	}

	public void restart() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		synchronized (stopedtimes) {
			stopedtimes.add(getNanosPassed());
		}
	}

	public long getAverageNanos() {
		long sum = 0;
		synchronized (stopedtimes) {
			for (Long l : stopedtimes) {
				sum += l;
			}
		}
		if(stopedtimes.size()==0){
			return 0;
		}
		return sum / stopedtimes.size();
	}

	public long getNanosPassed() {
		return (System.nanoTime() - startTime);
	}

	public long getMicrosPassed() {
		return (System.nanoTime() - startTime) / 1000;
	}

	public long getMillisPassed() {
		return (System.nanoTime() - startTime) / 1000000;
	}

	public double getSecondsPassed() {
		return (System.nanoTime() - startTime) / 1000000000;
	}

	public void updateRate(long processedSoFar) {
		synchronized (processedHistory) {
			processedHistory.add(processedSoFar);
		}
		
		synchronized (updateTimeHistory) {
			updateTimeHistory.add(getNanosPassed());
		}
	}

	/**
	 * 
	 * @return The rate giben by the last processed items given to updateRate()
	 *         divided by the total time passed
	 */
	public long getTotalRate() {
		if (processedHistory.size() < 2)
			return 0;
		return processedHistory.get(processedHistory.size() - 1) * 1000000000
				/ getNanosPassed();
	}

	/**
	 * 
	 * @return The rate during the last 'lastEntries' updates
	 */
	public long getRate(int lastEntries) {
		int size = processedHistory.size();
		if (size < 2) {
			return 0;
		}

		if (size <= lastEntries) {
			return getTotalRate();
		}
		
		long delta;
		synchronized (processedHistory) {
			delta = processedHistory.get(size - 1)
					- processedHistory.get(size - lastEntries-1);
		}
		
		long deltat;
		synchronized (updateTimeHistory) {
			deltat = updateTimeHistory.get(size - 1)
					- updateTimeHistory.get(size - lastEntries-1);
		}
		
		return delta * 1000000000 / deltat;
	}
}
