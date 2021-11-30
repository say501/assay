package com.lgcns.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunManager {

	public List<String> gData;
	public Map<String, String> gMap;
	public int gAlert;
	public int gMaxValue;
	public int gPrevValue;
	
	public RunManager() {
		this.gData = new ArrayList<String>();
		this.gMap = new HashMap<String, String>();
		this.gAlert = 0;
		this.gMaxValue = 0;
		this.gPrevValue = 0;
	}

	public List<String> readMonitoringData(String path) throws IOException {
		List<String> data = Files.readAllLines(Paths.get(path));
		
		boolean t = this.gData.containsAll(data);
		if (t) {
			throw new RuntimeException();
		}
		
		this.gData.clear();
		this.gData.addAll(data);
		
		return data;
	}
	
	public String process(List<String> data, int threshold) {
		String result = "";
		
		List<Integer> list = new ArrayList<Integer>();
		
		for (String line : data) {
			String[] all = line.split("#");
			String[] sys = all[1].split(":");			
			
			list.add(Integer.parseInt(sys[1]));
			if (list.size() > 3) {
				list.remove(0);
			}
			int sum = 0;
			for (int l : list) {
				sum += l;
			}
			int avg = sum / list.size();
			
			line += "#" + (Integer.parseInt(sys[1]) > threshold ? "Y" : "N") + "#" + String.format("%03d", avg);
			result += line + "\n";
			
			if (!this.gMap.containsKey(all[0])) {
				// new record
				this.gMap.put(all[0], line);
				
				// swap max value
				this.gMaxValue = Math.max(this.gMaxValue, avg);
				
				if (avg > threshold) {
					this.gAlert += 1;
				} else {
					this.gAlert = 0;
				}
				
				if (this.gAlert > 5 && (this.gMaxValue - avg <= 3)) {
					try {
						System.out.println("ALERT: " + line);
						Runtime.getRuntime().exec("./SUPPORT/ALERT.EXE " + line);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			this.gPrevValue = avg;
		}
		
		return result;
	}
	
	public static int getInputData() {
		String indata = "";
		try {
			int key;
			while ((key = System.in.read()) != 13) {
				indata += Character.toString((char) key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		int th = Integer.parseInt(indata);
		
		return th;
	}
	
	public static void main(String[] args) {
		RunManager rm = new RunManager();
		
		// get threshold
		int th = getInputData();
				
		
		for (int i=0; i<100000; i++) {
			try {
				Thread.sleep(100);
				
				// read monitoring data
				List<String> data = rm.readMonitoringData("./INPUT/MONITORING.TXT");
				
				// processing
				rm.process(data, th);
				
			} catch (InterruptedException e) {
			} catch (IOException e) {
			} catch (Exception e) {
			}
		}
	}

}
