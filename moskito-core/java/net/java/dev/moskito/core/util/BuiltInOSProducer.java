package net.java.dev.moskito.core.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import net.java.dev.moskito.core.predefined.OSStats;
import net.java.dev.moskito.core.producers.IStats;
import net.java.dev.moskito.core.producers.IStatsProducer;
import net.java.dev.moskito.core.registry.ProducerRegistryFactory;

import org.apache.log4j.Logger;

/**
 * Builtin producer for values supplied by jmx for the operation system.
 * @author lrosenberg
 */
public class BuiltInOSProducer extends AbstractBuiltInProducer implements IStatsProducer, BuiltInProducer{
	/**
	 * Associated stats.
	 */
	private OSStats stats;
	/**
	 * Stats container
	 */
	private List<IStats> statsList;

	/**
	 * The monitored pool.
	 */
	private OperatingSystemMXBean mxBean;

	/**
	 * Name of the mxbean class.
	 */
	private static final String clazzname = "com.sun.management.UnixOperatingSystemMXBean";

	/**
	 * Resolved class of the mx bean.
	 */
	private Class<?> clazz;

	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(BuiltInOSProducer.class);
	
	/**
	 * Creates a new producers object for a given pool.
	 * @param aPool
	 */
	public BuiltInOSProducer(){
		mxBean = ManagementFactory.getOperatingSystemMXBean();
		statsList = new ArrayList<IStats>(1);
		stats = new OSStats();
		statsList.add(stats);
		
		try{
			clazz = Class.forName(clazzname);
		}catch(ClassNotFoundException e){
			log.warn("Couldn't find unix version of os class: "+clazzname+", osstats won't operate properly.");
		}
		
		BuiltinUpdater.addTask(new TimerTask() {
			@Override
			public void run() {
				readMbean();
			}});
		
		ProducerRegistryFactory.getProducerRegistryInstance().registerProducer(this);
	}
	
	@Override
	public String getCategory() {
		return "os";
	}

	@Override
	public String getProducerId() {
		return "OS";
	}

	@Override
	public List<IStats> getStats() {
		return statsList;
	}

	@Override
	public String getSubsystem() {
		return SUBSYSTEM_BUILTIN;
	}
	
	private void readMbean() {
		if (clazz==null){
			return;
		}
		
		
		try{
			long openFiles = getValue("OpenFileDescriptorCount");
			long maxOpenFiles = getValue("MaxFileDescriptorCount");
			
			long freePhysicalMemorySize = getValue("FreePhysicalMemorySize");
			long totalPhysicalMemorySize = getValue("TotalPhysicalMemorySize");
			
			long processTime = getValue("ProcessCpuTime");
			
			long processors = getValue("AvailableProcessors");
			
			stats.update((int)openFiles, (int)maxOpenFiles, freePhysicalMemorySize, totalPhysicalMemorySize, processTime, (int)processors);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private long getValue(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{
		Method m = clazz.getMethod("get"+name);
		if (name.equals("AvailableProcessors"))
			return (Integer)m.invoke(mxBean);
		Long result = (Long)m.invoke(mxBean);
		return result;
	}
	
	public static void main(String a[]){
		new BuiltInOSProducer();
	}

}