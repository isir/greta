package hmi.flipper2.debugger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import hmi.flipper2.TemplateController;

public class CpuMemoryUsage {

	enum Channel {
		JS, CE, LOG
	};

	CpuMemoryUsage() {
		this.rt = Runtime.getRuntime();
		//
	}

	com.sun.management.OperatingSystemMXBean operatingSystemMXBean;
	RuntimeMXBean runtimeMXBean;
	int availableProcessors;
	long prevUpTime;
	long prevProcessCpuTime;
	long timeMarker;
	Runtime rt;
	
	public void startCpuTimer() {
		this.operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		this.availableProcessors = operatingSystemMXBean.getAvailableProcessors();
		this.prevUpTime = runtimeMXBean.getUptime();
		this.prevProcessCpuTime = operatingSystemMXBean.getProcessCpuTime();
		//
		this.timeMarker = System.currentTimeMillis();
	}

	public double getCpuTimer() {
		this.operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		long upTime = runtimeMXBean.getUptime();
		long processCpuTime = operatingSystemMXBean.getProcessCpuTime();
		long elapsedCpu = processCpuTime - prevProcessCpuTime;
		long elapsedTime = upTime - prevUpTime;

		return Math.min(99F, elapsedCpu / (elapsedTime * 10000F * availableProcessors));
	}
	
	public long elapsedTime() {
		return System.currentTimeMillis() - this.timeMarker;
	}

	public double getProcessCpuLoad() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

			if (list.isEmpty())
				return Double.NaN;

			Attribute att = (Attribute) list.get(0);
			Double value = (Double) att.getValue();

			// usually takes a couple of seconds before we get real values
			if (value == -1.0)
				return Double.NaN;
			// returns a percentage value with 1 decimal point precision
			return ((int) (value * 1000) / 10.0);
		} catch (Exception e) {
			System.out.println("EXCEPTION: " + e);
			return 0.0;
		}
	}
	
	public long maxMB() {
		return this.rt.maxMemory() / 1000000;
	}
	
	public long totalMB() {
		return this.rt.totalMemory() / 1000000;
	}
	
	public long freeMB() {
		return this.rt.freeMemory() / 1000000;
	}
	
	public long useMB() {
		return (this.rt.totalMemory() - this.rt.freeMemory()) / 1000000;
	}
	
}
