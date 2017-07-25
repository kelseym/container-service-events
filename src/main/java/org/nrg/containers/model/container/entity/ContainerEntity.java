package org.nrg.containers.model.container.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;
import org.nrg.containers.model.command.auto.ResolvedCommand;
import org.nrg.containers.model.command.auto.ResolvedCommand.ResolvedCommandMount;
import org.nrg.containers.model.command.auto.ResolvedCommand.ResolvedCommandOutput;
import org.nrg.containers.model.container.ContainerInputType;
import org.nrg.containers.model.container.auto.Container;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@Audited
public class ContainerEntity extends AbstractHibernateEntity {
    @JsonProperty("command-id") private long commandId;
    @JsonProperty("xnat-command-wrapper-id") private long xnatCommandWrapperId;
    @JsonProperty("docker-image") private String dockerImage;
    @JsonProperty("command-line") private String commandLine;
    @JsonProperty("working-directory") private String workingDirectory;
    @JsonProperty("env") private Map<String, String> environmentVariables = Maps.newHashMap();
    @JsonProperty("mounts") private List<ContainerEntityMount> mounts = Lists.newArrayList();
    @JsonProperty("container-id") private String containerId;
    @JsonProperty("user-id") private String userId;
    private List<ContainerEntityInput> inputs;
    private List<ContainerEntityOutput> outputs;
    private List<ContainerEntityHistory> history = Lists.newArrayList();
    @JsonProperty("log-paths") private List<String> logPaths;

    public ContainerEntity() {}

    public ContainerEntity(final ResolvedCommand resolvedCommand,
                           final String containerId,
                           final String userId) {
        this.containerId = containerId;
        this.userId = userId;

        this.commandId = resolvedCommand.commandId();
        this.xnatCommandWrapperId = resolvedCommand.wrapperId();
        this.dockerImage = resolvedCommand.image();
        this.commandLine = resolvedCommand.commandLine();
        this.workingDirectory = resolvedCommand.workingDirectory();
        setEnvironmentVariables(resolvedCommand.environmentVariables());
        setMounts(Lists.newArrayList(
                Lists.transform(resolvedCommand.mounts(), new Function<ResolvedCommandMount, ContainerEntityMount>() {
                    @Override
                    public ContainerEntityMount apply(final ResolvedCommandMount resolvedCommandMount) {
                        return new ContainerEntityMount(resolvedCommandMount);
                    }
                })
        ));
        addRawInputs(resolvedCommand.rawInputValues());
        addWrapperInputs(resolvedCommand.wrapperInputValues());
        addCommandInputs(resolvedCommand.commandInputValues());
        setOutputs(Lists.newArrayList(
                Lists.transform(resolvedCommand.outputs(), new Function<ResolvedCommandOutput, ContainerEntityOutput>() {
                    @Override
                    public ContainerEntityOutput apply(final ResolvedCommandOutput resolvedCommandOutput) {
                        return new ContainerEntityOutput(resolvedCommandOutput);
                    }
                })
        ));
        setLogPaths(null);
    }

    public static ContainerEntity fromPojo(final Container containerPojo) {
        final ContainerEntity containerEntity = new ContainerEntity();
        containerEntity.update(containerPojo);
        return containerEntity;
    }

    public ContainerEntity update(final Container containerPojo) {
        this.setId(containerPojo.databaseId());
        this.setCommandId(containerPojo.commandId());
        this.setXnatCommandWrapperId(containerPojo.wrapperId());
        this.setContainerId(containerPojo.containerId());
        this.setUserId(containerPojo.userId());
        this.setDockerImage(containerPojo.dockerImage());
        this.setCommandLine(containerPojo.commandLine());
        this.setWorkingDirectory(containerPojo.workingDirectory());
        this.setEnvironmentVariables(containerPojo.environmentVariables());
        this.setLogPaths(containerPojo.logPaths());
        this.setMounts(Lists.newArrayList(Lists.transform(
                containerPojo.mounts(), new Function<Container.ContainerMount, ContainerEntityMount>() {
                    @Override
                    public ContainerEntityMount apply(final Container.ContainerMount input) {
                        return ContainerEntityMount.fromPojo(input);
                    }
                }))
        );
        this.setInputs(Lists.newArrayList(Lists.transform(
                containerPojo.inputs(), new Function<Container.ContainerInput, ContainerEntityInput>() {
                    @Override
                    public ContainerEntityInput apply(final Container.ContainerInput input) {
                        return ContainerEntityInput.fromPojo(input);
                    }
                }))
        );
        this.setOutputs(Lists.newArrayList(Lists.transform(
                containerPojo.outputs(), new Function<Container.ContainerOutput, ContainerEntityOutput>() {
                    @Override
                    public ContainerEntityOutput apply(final Container.ContainerOutput input) {
                        return ContainerEntityOutput.fromPojo(input);
                    }
                }))
        );
        this.setHistory(Lists.newArrayList(Lists.transform(
                containerPojo.history(), new Function<Container.ContainerHistory, ContainerEntityHistory>() {
                    @Override
                    public ContainerEntityHistory apply(final Container.ContainerHistory input) {
                        return ContainerEntityHistory.fromPojo(input);
                    }
                }))
        );

        return this;
    }

    public long getCommandId() {
        return commandId;
    }

    public void setCommandId(final long commandId) {
        this.commandId = commandId;
    }

    public long getXnatCommandWrapperId() {
        return xnatCommandWrapperId;
    }

    public void setXnatCommandWrapperId(final long xnatCommandWrapperId) {
        this.xnatCommandWrapperId = xnatCommandWrapperId;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(final String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(final String commandLine) {
        this.commandLine = commandLine;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(final String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @ElementCollection
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(final Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables == null ?
                Maps.<String, String>newHashMap() :
                environmentVariables;
    }

    @OneToMany(mappedBy = "containerEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContainerEntityMount> getMounts() {
        return mounts;
    }

    public void setMounts(final List<ContainerEntityMount> mounts) {
        this.mounts = mounts == null ?
                Lists.<ContainerEntityMount>newArrayList() :
                mounts;
        for (final ContainerEntityMount mount : this.mounts) {
            mount.setContainerEntity(this);
        }
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(final String containerId) {
        this.containerId = containerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String user) {
        this.userId = user;
    }

    @OneToMany(mappedBy = "containerEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContainerEntityInput> getInputs() {
        return inputs;
    }

    public void setInputs(final List<ContainerEntityInput> inputs) {
        this.inputs = inputs == null ?
                Lists.<ContainerEntityInput>newArrayList() :
                inputs;
        for (final ContainerEntityInput input : this.inputs) {
            input.setContainerEntity(this);
        }
    }

    public void addInput(final ContainerEntityInput input) {
        if (input == null) {
            return;
        }
        input.setContainerEntity(this);

        if (this.inputs == null) {
            this.inputs = Lists.newArrayList();
        }
        this.inputs.add(input);
    }

    @Transient
    public Map<String, String> getRawInputs() {
        return getInputs(ContainerInputType.RAW);
    }

    public void addRawInputs(final Map<String, String> rawInputValues) {
        addInputs(ContainerInputType.RAW, rawInputValues);
    }

    @Transient
    public Map<String, String> getWrapperInputs() {
        return getInputs(ContainerInputType.WRAPPER);
    }

    public void addWrapperInputs(final Map<String, String> xnatInputValues) {
        addInputs(ContainerInputType.WRAPPER, xnatInputValues);
    }

    @Transient
    public Map<String, String> getCommandInputs() {
        return getInputs(ContainerInputType.COMMAND);
    }

    public void addCommandInputs(final Map<String, String> commandInputValues) {
        addInputs(ContainerInputType.COMMAND, commandInputValues);
    }

    private Map<String, String> getInputs(final ContainerInputType type) {
        if (this.inputs == null) {
            return null;
        }
        final Map<String, String> inputs = Maps.newHashMap();
        for (final ContainerEntityInput input : this.inputs) {
            if (input.getType() == type) {
                inputs.put(input.getName(), input.getValue());
            }
        }
        return inputs;
    }

    private void addInputs(final ContainerInputType type,
                           final Map<String, String> inputs) {
        if (inputs == null) {
            return;
        }
        for (final Map.Entry<String, String> inputEntry : inputs.entrySet()) {
            switch (type) {
                case RAW:
                    addInput(ContainerEntityInput.raw(inputEntry.getKey(), inputEntry.getValue()));
                    break;
                case WRAPPER:
                    addInput(ContainerEntityInput.wrapper(inputEntry.getKey(), inputEntry.getValue()));
                    break;
                case COMMAND:
                    addInput(ContainerEntityInput.command(inputEntry.getKey(), inputEntry.getValue()));
                    break;
            }
        }
    }

    @OneToMany(mappedBy = "containerEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContainerEntityOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(final List<ContainerEntityOutput> outputs) {
        this.outputs = outputs == null ?
                Lists.<ContainerEntityOutput>newArrayList() :
                outputs;
        for (final ContainerEntityOutput output : this.outputs) {
            output.setContainerEntity(this);
        }
    }

    @OneToMany(mappedBy = "containerEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ContainerEntityHistory> getHistory() {
        return history;
    }

    public void setHistory(final List<ContainerEntityHistory> history) {
        this.history = history == null ?
                Lists.<ContainerEntityHistory>newArrayList() :
                history;
        for (final ContainerEntityHistory historyItem : this.history) {
            historyItem.setContainerEntity(this);
        }
    }

    @Transient
    public void addToHistory(final ContainerEntityHistory historyItem) {
        if (historyItem == null) {
            return;
        }
        historyItem.setContainerEntity(this);
        if (this.history == null) {
            this.history = Lists.newArrayList();
        }
        this.history.add(historyItem);
    }

    @Transient
    public boolean isItemInHistory(final ContainerEntityHistory historyItem) {
        return this.history != null && this.history.contains(historyItem);

    }

    @ElementCollection
    public List<String> getLogPaths() {
        return logPaths;
    }

    public void setLogPaths(final List<String> logPaths) {
        this.logPaths = logPaths;
    }

    @Transient
    public void addLogPath(final String logPath) {
        if (StringUtils.isBlank(logPath)) {
            return;
        }

        if (this.logPaths == null) {
            this.logPaths = Lists.newArrayList();
        }
        this.logPaths.add(logPath);
    }

    @Transient
    public void addLogPaths(final Set<String> logPaths) {
        if (logPaths == null || logPaths.isEmpty()) {
            return;
        }

        for (final String logPath : logPaths) {
            addLogPath(logPath);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ContainerEntity that = (ContainerEntity) o;
        return Objects.equals(this.containerId, that.containerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containerId", containerId)
                .add("commandId", commandId)
                .add("wrapperId", xnatCommandWrapperId)
                .add("dockerImage", dockerImage)
                .add("commandLine", commandLine)
                .add("environmentVariables", environmentVariables)
                .add("mounts", mounts)
                .add("userId", userId)
                .add("inputs", inputs)
                .add("outputs", outputs)
                .add("history", history)
                .add("logPaths", logPaths)
                .toString();
    }
}