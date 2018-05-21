package com.sissi.droidw.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

//import com.kedacom.truelinktv.app.TrueLinkTVApplication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

// TODO 考虑一个应用包含多个进程的情形,是否仍适用?
public class SystemMonitor {

	private SystemMonitor(){
		
	}

    private static boolean isStarted = false;
    private static RandomAccessFile reader;
    private static RandomAccessFile reader2;

	private static final int  INTERVAL = 1000; // 单位：毫秒
	
    private static CpuStat csOld;
    private static CpuStat cs;
    private static MemoryStat ms;
    private static ProcStat psOld;
    private static ProcStat ps;
    private static Timer timer;

	private static int savedCpuUsage;
	private static int savedSelfCpuUsage;
    
    public static synchronized void start(){
    	if (isStarted){
    		return;
    	}
    	
    	try {
    		reader = new RandomAccessFile("/proc/stat", "r");
    		reader2 = new RandomAccessFile("/proc/self/stat", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

    	csOld = new CpuStat();
    	cs = new CpuStat();
        ms = new MemoryStat();
        psOld = new ProcStat();
        ps = new ProcStat();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			public void run() {
				synchronized (SystemMonitor.class){
					capCpuStat();
					capProcStat();
					capMemStat();
				}
			}
		}, 0, INTERVAL);
    	
    	isStarted = true;
    }
    
    public static synchronized void stop(){
    	if (!isStarted){
    		return;
    	}
    	
    	isStarted = false;
    	
		timer.cancel();
		timer = null;
    	
    	try {
			reader.close();
	    	reader2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	csOld = null;
    	cs = null;
        ms = null;
        psOld = null;
        ps = null;
    }

	// usage%
    public static synchronized int getCpuUsage(){
    	if (!isStarted){
    		return -1;
    	}
    	
    	long deltaWork = cs.work()-csOld.work();
    	long deltaTotal = cs.total()-csOld.total();
    	if (deltaWork<=0 || deltaTotal<=0){
			return savedCpuUsage;
		}
		int cpuUsage = (int) ((100*deltaWork) / deltaTotal);
		if (100<cpuUsage){
			return savedCpuUsage;
		}
		return savedCpuUsage = cpuUsage;
    }
    
    
    public static synchronized int getSelfCpuUsage(){
    	if (!isStarted){
    		return -1;
    	}
    	
    	long deltaSelf = ps.cpu()-psOld.cpu();
    	long deltaTotal = cs.total()-csOld.total();
    	if (deltaSelf<=0 || deltaTotal<=0){
			return savedSelfCpuUsage;
		}
		int cpuUsage = (int) ((100*deltaSelf) / deltaTotal);
		if (100<cpuUsage){
			return savedSelfCpuUsage;
		}
		return savedSelfCpuUsage = cpuUsage;
    }

	public static synchronized int getMemUsage(){
		if (!isStarted){
			return -1;
		}

		return ms.usage();
	}
    
    public static synchronized int getSelfMemUsage(){
    	if (!isStarted){
    		return -1;
    	}
    	
    	return (int) (ps.memory()*100/ms.total());
    }

	// mem(K)
	public static synchronized long getTotalMem(){
		if (!isStarted){
			return -1;
		}

		return ms.total();
	}

	public static synchronized long getAvailMem(){
		if (!isStarted){
			return -1;
		}

		return ms.avail();
	}

	public static synchronized long getUsedMem(){
		if (!isStarted){
			return -1;
		}

		return ms.used();
	}

	public static synchronized long getSelfUsedMem(){
		if (!isStarted){
			return -1;
		}

		return ps.memory();
	}

	public static synchronized Overview getOverview(){
		if (!isStarted){
			return null;
		}
		int cpuUsage = getCpuUsage();
		int selfCpuUsage = getSelfCpuUsage();
		long totalMem = getTotalMem();
		long usedMem = getUsedMem();
		long selfUsedMem = getSelfUsedMem();

		cpuUsage = selfCpuUsage>cpuUsage ? selfCpuUsage : cpuUsage;
		usedMem = usedMem>totalMem ? totalMem : usedMem;
		selfUsedMem = selfUsedMem>usedMem ? usedMem : selfUsedMem;

		return new Overview(cpuUsage, selfCpuUsage, totalMem, usedMem, selfUsedMem);
	}

    private static void capCpuStat(){
    	String line = null;
		try {
			reader.seek(0);
			line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	String tok[] = line.trim().split("\\s+");
    	csOld.copy(cs);
    	cs.user = Long.parseLong(tok[1]);
    	cs.nice = Long.parseLong(tok[2]);
    	cs.system = Long.parseLong(tok[3]);
    	cs.idle = Long.parseLong(tok[4]);
    	cs.iowait = Long.parseLong(tok[5]);
    	cs.irq = Long.parseLong(tok[6]);
    	cs.softirq = Long.parseLong(tok[7]);
    	cs.steal = Long.parseLong(tok[8]);
//    	cs.guest = Long.parseLong(tok[9]);
//    	cs.guest_nice = Long.parseLong(tok[10]);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static void capMemStat(){
//    	ActivityManager activityMgr = (ActivityManager) TrueLinkTVApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
//        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
//        activityMgr.getMemoryInfo(memoryInfo);
//        ms.memTotal = memoryInfo.totalMem;
//        ms.memAvail = memoryInfo.availMem;
    }

    private static void capProcStat(){
    	String line = null;
		try {
			reader2.seek(0);
			line = reader2.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	String tok[] = line.trim().split("\\s+");
    	psOld.copy(ps);
    	ps.utime = Long.parseLong(tok[13]);
    	ps.stime = Long.parseLong(tok[14]);
    	ps.cutime = Long.parseLong(tok[15]);
    	ps.cstime = Long.parseLong(tok[16]);
    	ps.vmrss = Long.parseLong(tok[23]); // 单位：页
    }
    
    private static class CpuStat{
    	/*各字段解释可参考：http://man7.org/linux/man-pages/man5/proc.5.html*/
    	private long user;
    	private long nice;
    	private long system;
    	private long idle;
    	private long iowait;
    	private long irq;
    	private long softirq;
    	private long steal;
    	private long guest;
    	private long guest_nice;
    	
    	public long total(){
    		return user+nice+system+idle+iowait+irq+softirq+steal+guest+guest_nice;
    	}
    	
    	public long idle(){
    		return idle;
    	}

    	public long work(){
    		return user+nice+system+iowait+irq+softirq+steal+guest+guest_nice;
    	}
    	
    	public void copy(CpuStat cs){
    		user = cs.user;
    		nice = cs.nice;
    		system = cs.system;
    		idle = cs.idle;
    		iowait = cs.iowait;
    		irq = cs.irq;
    		softirq = cs.softirq;
    		steal = cs.steal;
    		guest = cs.guest;
    		guest_nice = cs.guest_nice;
    	}
    }
    
    private static class MemoryStat{
    	private long memTotal; // 单位：Byte
    	private long memAvail;
    	
    	public long total(){
    		return memTotal/1024;
    	}
    	
    	public long avail(){
    		return memAvail/1024;
    	}

		public long used(){
			return (memTotal-memAvail)/1024;
		}

		// 内存使用率（%）
		public int usage(){
			return (int) ((memTotal-memAvail)*100/memTotal);
		}
    }
    
    private static class ProcStat{
    	/*各字段解释可参考：http://man7.org/linux/man-pages/man5/proc.5.html*/
    	private long utime;
    	private long stime;
    	private long cutime;
    	private long cstime;
    	
    	private long vmrss; // 内存占用，单位：页
    	
    	public long cpu(){
    		return utime+stime+cutime+cstime;
    	}
    	
    	public long memory(){
    		return vmrss * 4; // 默认的页大小4K
    	}
    	
    	public void copy(ProcStat ps){
    		utime = ps.utime;
    		stime = ps.stime;
    		cutime = ps.cutime;
    		cstime = ps.cstime;
    	}
    }
    
    /**系统状态概览*/
	public static class Overview{
		public int cpuUsage;   	// 总的CPU使用率。百分比分成，如30意为30%
		public int selfCpuUsage;// 本进程CPU使用率
		public long totalMem;	// 总内存。单位：KB
		public long usedMem;	// 已消耗内存
		public long selfUsedMem;// 本进程消耗内存
		public Overview(int cu, int scu, long tm, long um, long sum){
			cpuUsage = cu;
			selfCpuUsage = scu;
			totalMem = tm;
			usedMem = um;
			selfUsedMem = sum;
		}
	}
    
    
    
	class ProcStatInfo{
		public ProcStatInfo(String cpuUsage, String memoryUsage){
			this.cpuUsage = cpuUsage;
			this.memUsage = memoryUsage;
		}
		String cpuUsage;
		String memUsage;
	}
	
	/** 获取进程的CPU及内存使用情况（基于top命令）。
	 *
	 * ！需要注意的问题：
	 * 依赖于top、grep命令（及其输出格式）；
	 * 此函数会阻塞当前线程几秒钟（取决于top|grep执行时间），故建议在非主线程内执行；
	 * 效率较低。此实现方式是启用新的进程来执行top命令，并获取其输出，然后对输出进行解析，在一次执行中需要经历进程的创建销毁，故不建议频繁调用该函数。
	 * */

	@Deprecated
    public ProcStatInfo getProcStatInfo(String procName) {
    	if (null == procName || procName.trim().isEmpty()){
    		return null;
    	}
    	
    	String filter = "|grep " + procName.trim();
    	String[] cmd = {"sh", "-c", "top -n 1"+filter};
        String Result = null;
        Process proc = null;
        BufferedReader br;
        try {
			proc = Runtime.getRuntime().exec(cmd);
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			Result = br.readLine();
			br.close();
			proc.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if (null != Result) {
        	Result = Result.trim();
        	String cpuUsage = null;
        	String memUsage = null;
        	boolean jumpedVss =false;
        	String[] cols = Result.split("\\s+");
        	/***********************************
        	 * 解析top命令的输出。
        	 * 该解析严格依赖于top命令的输出格式，
        	 * 该解析在top命令输出格式为如下形式时验证通过：
        	 * 
        	 *   PID PR CPU% S  #THR     VSS     RSS PCY UID      Name
 				7947  1   5% S    74 676948K  83740K  fg u0_a1    com.kedacom.truetouch.sky
        	 * 
        	 * *******************************************************************************/
        	for (int i=0; i<cols.length; ++i){
        		if (cols[i].endsWith("%")){
        			cpuUsage = cols[i]; // 找到CPU%字段（更准确可靠的方式可以先解析top输出的头部，找到CPU%字段及RSS字段对应的列序号，再根据列序号定位相应的字段）
        		}else if (cols[i].endsWith("K")){
        			if (jumpedVss){
        				memUsage=cols[i]; // 找到RSS字段
        				break;  // RSS字段在CPU%字段之后，故当我们找到RSS字段我们认为CPU%字段亦已经找到，查找完成。
        			}else{
        				jumpedVss = true; // 跳过VSS字段
        			}
        		}
        	}
        	return new ProcStatInfo(cpuUsage, memUsage); 
        }
        
        return null;
    }
}
