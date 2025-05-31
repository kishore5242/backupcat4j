package org.kapps.backup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackupAgentFactory {

    private final List<BackupAgent> agents;

    @Autowired
    public BackupAgentFactory(List<BackupAgent> agents) {
        this.agents = agents;
    }

    public BackupAgent getAgent(String mimeType) {
        return agents.stream()
                .filter(agent -> agent.supports(mimeType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No agent for MIME type: " + mimeType));
    }
}