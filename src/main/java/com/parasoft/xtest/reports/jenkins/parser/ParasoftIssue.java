package com.parasoft.xtest.reports.jenkins.parser;

import java.io.Serializable;

import edu.hm.hafner.analysis.Issue;

public class ParasoftIssue extends Issue {
	
	private static final long serialVersionUID = 3156626814406033385L;
	private String _author;
	private String _revision;

	public ParasoftIssue(Issue generalIssue) {
		super(generalIssue);
		Serializable additionalProperties = generalIssue.getAdditionalProperties();
		
        if (additionalProperties instanceof ParasoftIssueAdditionalProperties) {
        	ParasoftIssueAdditionalProperties parasoftAdditionalProperties = (ParasoftIssueAdditionalProperties) additionalProperties;
        	_author = parasoftAdditionalProperties.getAuthor();
        	_revision = parasoftAdditionalProperties.getRevision();
        }
	}

	public String getAuthor() {
		return _author;
	}

	public String getRevision() {
		return _revision;
	}
	
	@Override
	public boolean equals(final Object o) {
		if(!super.equals(o)) {
			return false;
		}
		if(!(o instanceof ParasoftIssue)){
			return false;
		}
		ParasoftIssue objectAsParasoftIssue = (ParasoftIssue) o;
		if(this._author != null ? !this._author.equals(objectAsParasoftIssue.getAuthor()) : objectAsParasoftIssue.getAuthor() != null){
			return false;
		}
		if(this._revision != null ? !this._revision.equals(objectAsParasoftIssue.getRevision()) : objectAsParasoftIssue.getRevision() != null){
			return false;
		}		
		return true;
	}
	
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (_author == null ? 0 : _revision.hashCode());
        result = 31 * result + (_revision == null ? 0 : _revision.hashCode());
        return result;
    }
}
