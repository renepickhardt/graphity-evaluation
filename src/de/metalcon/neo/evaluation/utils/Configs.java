package de.metalcon.neo.evaluation.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * This is an interface class to the Config file for this project. For each
 * class field on java property must be defined int config.txt. The fields will
 * be automatically filled! Allowed Types are String, int, String[] and long[]
 * where arrays are defined by semicolon-separated Strings like "array=a;b;c"
 * 
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */
public class Configs extends Properties {
	public String wikiTransactionsFile;
	public String SortedWikiTransactionsFile;
	public String CleanWikiTransactionsFile;

	public String SimulateEventsPrefix;
	public int numberOfEventsToSimulate;

	public boolean MetalconRun;
	public boolean CreateMetalconFiles;

	public boolean IgnoreSmallDegreeNodes;
	public int MinimumNodeDegree;

	public String MemDir;
	public boolean RunOnMemory;
	public boolean WarmumpDB;

	public String StarDBDirPrefix;
	public String FFDBDirPrefix;
	public String CleanFriendDBPrefix;
	public String BlouDBDirPrefix;
	public String GraphityDBDirPrefix;
	public String MetalconDB;

	public String CleanWikiSnapshotUpdatePrefix;
	public String CleanWikiSnapshotFriendPrefix;
	public String MetalconUpdates;
	public String MetalconUpdatesSorted;

	public String SamplePrefix;
	public int SampleTimestamp;
	public int[] SampleStartDegrees;
	public int SampleDegrees;
	public int SampleSize;
	public int SampleRepeatRuns;

	public long[] StarSnapshotTimestamps;

	public String WikiFullIDList;
	public String MetalconFullIDList;
	public String SnapshotIDListPrefix;

	public String DegreeMapPrefix;
	public String LargeDegreeNodesPrefix;
	public String DegreeDistributionPrefix;
	public String MetalconDegreeDistribution;

	public boolean SortWikiTransactions;
	public boolean CleanWikiTransactions;
	public boolean SplitCleanDumps;
	public boolean GenerateSnapshotIDLists;

	public boolean CreateStarDBs;
	public boolean AddAllEntitiesToAllSnapshots;
	public boolean GenerateDegreeList;
	public boolean GenerateDegreeSamples;

	public int k;
	public int runs;

	public boolean BaseLineUpdateEvaluatorInsertUpdates;
	public boolean BaseLineUpdateEvaluatorEvaluate;

	public boolean BlouUpdateEvaluatorInsertUpdates;
	public boolean BlouUpdateEvaluatorEvaluate;
	public boolean BlouUpdateEvaluatorEvaluateDegree;
	public boolean BlouUpdateEvaluatorSimulate;

	public boolean FlatFileUpdateEvaluatorInsertUpdates;
	public boolean FlatFileUpdateEvaluatorEvaluate;

	public boolean ResetGraphity;
	public boolean BuildGraphity;
	public boolean ReadGraphityStreams;
	public boolean ReadGraphityStreamsDegree;
	public boolean SimulateGraphity;

	public String use_memory_mapped_buffers;
	public String cache_type;

	private static final long serialVersionUID = -4439565094382127683L;

	static Configs instance = null;

	public Configs() {
		String file = "config.txt";
		try {
			BufferedInputStream stream = new BufferedInputStream(
					new FileInputStream(file));
			load(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			initialize();
			if (MetalconRun) {
				StarSnapshotTimestamps = new long[] { -666 };
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fills all fields with the data defined in the config file.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void initialize() throws IllegalArgumentException,
			IllegalAccessException {
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (getProperty(f.getName()) == null) {
				System.err.print("Property '" + f.getName()
						+ "' not defined in config file");
			}
			if (f.getType().equals(String.class)) {
				f.set(this, getProperty(f.getName()));
			} else if (f.getType().equals(long.class)) {
				f.setLong(this, Long.valueOf(getProperty(f.getName())));
			} else if (f.getType().equals(int.class)) {
				f.setInt(this, Integer.valueOf(getProperty(f.getName())));
			} else if (f.getType().equals(boolean.class)) {
				f.setBoolean(this, Boolean.valueOf(getProperty(f.getName())));
			} else if (f.getType().equals(String[].class)) {
				f.set(this, getProperty(f.getName()).split(";"));
			} else if (f.getType().equals(int[].class)) {
				String[] tmp = getProperty(f.getName()).split(";");
				int[] ints = new int[tmp.length];
				for (int i = 0; i < tmp.length; i++) {
					ints[i] = Integer.parseInt(tmp[i]);
				}
				f.set(this, ints);
			} else if (f.getType().equals(long[].class)) {
				String[] tmp = getProperty(f.getName()).split(";");
				long[] longs = new long[tmp.length];
				for (int i = 0; i < tmp.length; i++) {
					longs[i] = Long.parseLong(tmp[i]);
				}
				f.set(this, longs);
			}
		}
	}

	public static Configs get() {
		if (instance == null)
			instance = new Configs();
		return instance;
	}
}
