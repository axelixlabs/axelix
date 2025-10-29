package com.nucleonforge.axile.sbs.spring.details;

import org.springframework.boot.info.GitProperties;

import com.nucleonforge.axile.sbs.spring.master.CommitIdPluginGitInformationProvider;

public class GitInformation extends CommitIdPluginGitInformationProvider {
    public GitInformation(GitProperties gitProperties) {
        super(gitProperties);
    }
}
