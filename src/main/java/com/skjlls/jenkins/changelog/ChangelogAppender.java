package com.skjlls.jenkins.changelog;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

public class ChangelogAppender extends Builder implements Serializable {
	
	private static final long serialVersionUID = 9063935120209133900L;

	public final String outputfile;
	public final String versionEnv;
	
    @DataBoundConstructor
    public ChangelogAppender(String outputfile, String versionEnv) {
    	this.outputfile = outputfile;
    	this.versionEnv = versionEnv;
	}

	@Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        final PrintStream logger = listener.getLogger();
        
        List<String> changes = new LinkedList<String>();
		logger.println("collecting changes ... ");
        
        AbstractBuild b = build;

        while(true) {
        	
        	for(Object o : b.getChangeSet().getItems()) {
				try {
					Method m = o.getClass().getMethod("getComment");
					String comment = "" + (String)m.invoke(o, new Object[0]);
					comment = comment.replaceAll("[\\r\\n]+", "\n\t");
					changes.add(comment);
				} catch (Exception e) {
					logger.println("unable to determine commit message for comment "+o+", "+e.getMessage());
				}
        	}
        	
        	b = b.getPreviousBuild();
        	
        	if(b==null || b.getResult().isBetterOrEqualTo(Result.SUCCESS)) {
        		break;
        	}
        	
        }
        logger.println("collecting changes ... "+changes.size()+" changes found!");

        String version = build.getEnvironment(TaskListener.NULL).get(versionEnv);
        if(version == null) {
            logger.println("no version found in environment, using build number");
        	version = "#"+build.getNumber();
        } else {
            logger.println("found a version in environment, using: "+version);
        }

        FilePath workspace = build.getWorkspace();
        
        String outfile = "CHANGELOG.md";
        if(outputfile!=null && outputfile.trim().length()>0) {
        	outfile = outputfile;
        }

        logger.println("writing changelog to "+outfile);
        ChangelogAppenderCallable cac = new ChangelogAppenderCallable();
        cac.setChanges(changes);
        cac.setVersion(version);
        cac.setWorkspace(workspace);
        cac.setOutfile(outfile);
    	launcher.getChannel().call(cac);
        logger.println("writing changelog to "+outfile+" ... done!");
        
        
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
        
        public String getDisplayName() {
            return "Changelog Appender";
        }
       
    }

	
}
