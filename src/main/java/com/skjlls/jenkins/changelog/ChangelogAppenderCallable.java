package com.skjlls.jenkins.changelog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.remoting.RoleChecker;

import hudson.FilePath;
import hudson.remoting.Callable;

public class ChangelogAppenderCallable implements Callable<String, IOException> {

	private String version;
	private String outfile;
	private FilePath workspace;
	private List<String> changes;
	
	public void checkRoles(RoleChecker arg0) throws SecurityException {
	}

	public String call() throws IOException {
		
		File f = new File(workspace+"/"+outfile);
		String s = "";
		if(f.exists()) {
			s = FileUtils.readFileToString(f); 
		}
		
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		sb.append("== "+version+" "+sdf.format(new Date())+"\n");
		if(changes.size()==0) {
			sb.append("*no changes*\r\n");
		}
		for(String c : changes) {
			sb.append("* "+c+"\r\n");
		}
		sb.append("\r\n");
		sb.append(s);
		
		FileUtils.write(f, sb.toString());
		
		return null;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOutfile() {
		return outfile;
	}

	public void setOutfile(String outfile) {
		this.outfile = outfile;
	}

	public FilePath getWorkspace() {
		return workspace;
	}

	public void setWorkspace(FilePath workspace) {
		this.workspace = workspace;
	}

	public List<String> getChanges() {
		return changes;
	}

	public void setChanges(List<String> changes) {
		this.changes = changes;
	}

}
