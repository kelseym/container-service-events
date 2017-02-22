package org.nrg.containers.rest;

import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.nrg.containers.exceptions.BadRequestException;
import org.nrg.containers.exceptions.CommandInputResolutionException;
import org.nrg.containers.exceptions.CommandResolutionException;
import org.nrg.containers.exceptions.CommandValidationException;
import org.nrg.containers.exceptions.ContainerMountResolutionException;
import org.nrg.containers.exceptions.DockerServerException;
import org.nrg.containers.exceptions.NoServerPrefException;
import org.nrg.containers.exceptions.NotFoundException;
import org.nrg.containers.model.CommandEntity;
import org.nrg.containers.model.ContainerExecution;
import org.nrg.containers.model.ResolvedDockerCommand;
import org.nrg.containers.model.auto.Command;
import org.nrg.containers.services.CommandService;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NrgRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.rest.AbstractXapiRestController;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@XapiRestController
@RequestMapping("/commands")
@Api("Command API for XNAT Container service")
public class CommandRestApi extends AbstractXapiRestController {
    private static final Logger log = LoggerFactory.getLogger(CommandRestApi.class);
    private static final String JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;
    private static final String TEXT = MediaType.TEXT_PLAIN_VALUE;
    private static final String FORM = MediaType.APPLICATION_FORM_URLENCODED_VALUE;

    private CommandService commandService;

    @Autowired
    public CommandRestApi(final CommandService commandService,
                          final UserManagementServiceI userManagementService,
                          final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.commandService = commandService;
    }

    /*
    GET COMMANDS
     */
    @RequestMapping(value = {}, params = {"!name", "!version", "!image"}, method = GET)
    @ApiOperation(value = "Get all Commands")
    @ResponseBody
    public List<Command> getCommands() {
        return commandService.getAllCommands();
    }

    @RequestMapping(value = {}, method = GET)
    @ApiOperation(value = "Get Commands by criteria")
    @ResponseBody
    public List<Command> getCommands(final @RequestParam(required = false) String name,
                                           final @RequestParam(required = false) String version,
                                           final @RequestParam(required = false) String image) throws BadRequestException {


        if (StringUtils.isBlank(name) && StringUtils.isBlank(version) && StringUtils.isBlank(image)) {
            return getCommands();
        }

        if (StringUtils.isBlank(name) && StringUtils.isNotBlank(version)) {
            throw new BadRequestException("If \"version\" is specified, must specify \"name\" as well.");
        }

        final Map<String, Object> properties = Maps.newHashMap();
        if (StringUtils.isNotBlank(name)) {
            properties.put("name", name);
        }
        if (StringUtils.isNotBlank(version)) {
            properties.put("version", version);
        }
        if (StringUtils.isNotBlank(image)) {
            properties.put("image", image);
        }

        return commandService.findCommandByProperties(properties);
    }

    @RequestMapping(value = {"/{id}"}, method = GET)
    @ApiOperation(value = "Get a Command by ID")
    @ResponseBody
    public Command retrieveCommand(final @PathVariable Long id) throws NotFoundException {
        return commandService.getCommand(id);
    }

    /*
    CREATE COMMANDS
     */
    @RequestMapping(value = {}, method = POST, produces = JSON)
    @ApiOperation(value = "Create a Command", code = 201)
    public ResponseEntity<Long> createCommand(final @RequestBody Command command)
            throws BadRequestException, CommandValidationException {

        try {
            final Command created = commandService.create(command);
            if (created == null) {
                return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(created.id(), HttpStatus.CREATED);
        } catch (NrgRuntimeException e) {
            throw new BadRequestException(e);
        }
    }

    /*
    UPDATE COMMANDS
     */
    @RequestMapping(value = {"/{id}"}, method = POST)
    @ApiOperation(value = "Update a Command")
    @ResponseBody
    public Command updateCommand(final @RequestBody Command command,
                                 final @PathVariable Long id,
                                 final @RequestParam(value = "ignore-null", defaultValue = "true")
                                             Boolean ignoreNull) throws NotFoundException, CommandValidationException {
        return commandService.update(id, command, ignoreNull);
    }

    /*
    DELETE COMMANDS
     */
    @RequestMapping(value = {"/{id}"}, method = DELETE)
    @ApiOperation(value = "Delete a Command", code = 204)
    public ResponseEntity<String> deleteCommand(final @PathVariable Long id) {
        commandService.delete(id);
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }

    /*
    LAUNCH COMMANDS
     */
    @RequestMapping(value = {"/launch"}, method = POST)
    @ApiOperation(value = "Launch a container from a resolved command")
    @ResponseBody
    public String launchCommand(final @RequestBody ResolvedDockerCommand resolvedDockerCommand)
            throws NoServerPrefException, DockerServerException, ContainerMountResolutionException {
        final UserI userI = XDAT.getUserDetails();
        final ContainerExecution executed = commandService.launchResolvedDockerCommand(resolvedDockerCommand, userI);
        return executed.getContainerId();
    }

    @RequestMapping(value = {"/{id}/launch"}, method = POST)
    @ApiIgnore // Swagger UI does not correctly show this API endpoint
    @ResponseBody
    public String launchCommandWQueryParams(final @PathVariable Long id,
                                            final @RequestParam Map<String, String> allRequestParams)
            throws NoServerPrefException, DockerServerException, NotFoundException, BadRequestException, CommandResolutionException {
        log.info("Launch requested for command id " + String.valueOf(id));
        final ContainerExecution executed = launchCommand(id, allRequestParams);
        return executed.getContainerId();
    }

    @RequestMapping(value = {"/{id}/launch"}, method = POST, consumes = {JSON})
    @ApiOperation(value = "Resolve a command from the variable values in the request body, and launch it")
    @ResponseBody
    public String launchCommandWJsonBody(final @PathVariable Long id,
                                         final @RequestBody Map<String, String> allRequestParams)
            throws NoServerPrefException, DockerServerException, NotFoundException, BadRequestException, CommandResolutionException {
        log.info("Launch requested for command id " + String.valueOf(id));
        final ContainerExecution executed = launchCommand(id, allRequestParams);
        return executed.getContainerId();
    }

    private ContainerExecution launchCommand(final @PathVariable Long id, final @RequestParam Map<String, String> allRequestParams)
            throws NoServerPrefException, DockerServerException, NotFoundException, CommandResolutionException, BadRequestException {
        final UserI userI = XDAT.getUserDetails();
        try {
            final ContainerExecution containerExecution = commandService.resolveAndLaunchCommand(id, allRequestParams, userI);
            if (log.isInfoEnabled()) {
                log.info(String.format("Launched command id %d. Produced container %d.", id,
                        containerExecution != null ? containerExecution.getId() : null));
                if (log.isDebugEnabled()) {
                    log.debug(containerExecution != null ? containerExecution.toString() : "Container execution object is null.");
                }
            }
            return containerExecution;
        } catch (CommandInputResolutionException e) {
            throw new BadRequestException("Must provide value for variable " + e.getInput().getName() + ".", e);
        }
    }

    /*
    EXCEPTION HANDLING
     */
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {NotFoundException.class})
    public String handleNotFound(final Exception e) {
        final String message = e.getMessage();
        log.debug(message);
        return message;
    }

    @ResponseStatus(value = HttpStatus.FAILED_DEPENDENCY)
    @ExceptionHandler(value = {NoServerPrefException.class})
    public String handleFailedDependency(final Exception ignored) {
        final String message = "Set up Docker server before using this REST endpoint.";
        log.debug(message);
        return message;
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {DockerServerException.class})
    public String handleDockerServerError(final Exception e) {
        final String message = "The Docker server returned an error:\n" + e.getMessage();
        log.debug(message);
        return message;
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {CommandResolutionException.class})
    public String handleCommandResolutionException(final CommandResolutionException e) {
        final String message = "The command could not be resolved.\n" + e.getMessage();
        log.debug(message);
        return message;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {BadRequestException.class})
    public String handleBadRequest(final Exception e) {
        final String message = "Bad request:\n" + e.getMessage();
        log.debug(message);
        return message;
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {CommandValidationException.class})
    public String handleBadCommand(final CommandValidationException e) {
        String message = "Invalid command";
        if (e != null && e.getErrors() != null && !e.getErrors().isEmpty()) {
            message += ":\n\t";
            message += StringUtils.join(e.getErrors(), "\n\t");
        }
        log.debug(message);
        return message;
    }
}
